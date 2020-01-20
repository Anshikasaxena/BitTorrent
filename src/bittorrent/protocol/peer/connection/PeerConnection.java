package bittorrent.protocol.peer.connection;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import bittorrent.BitTorrent;
import bittorrent.config.PeerInfo;
import bittorrent.logging.Logger;
import bittorrent.protocol.messages.ActualMessage;
import bittorrent.protocol.messages.HandshakeMessage;
import bittorrent.protocol.messages.MessageType;
import bittorrent.protocol.peer.PeerProcess;

/**
 * Represents a connection between two peers.
 * 
 * Each peer maintains its own connection with the other peer. In other words,
 * each TCP connection will generate two instances of this class: one for each
 * peer.
 */
public class PeerConnection implements CompletionHandler<Integer, ByteBuffer>
{
	private AsynchronousSocketChannel peerChannel;
	private PeerProcess parent;
	private boolean initiatedConnection;
	private boolean isConnected;
	private boolean isPreferred;
	private boolean isChoked;
	private boolean isOptimisticallyUnchoked;
	private boolean isInterested;
	private boolean receivedBitfield;
	private int peerID;
	private int downloadRate;
	private RequestPieceProcess requestor;

	/**
	 * Creates a peer connection.
	 * 
	 * This constructor assumes that the peer channel is already established. If the
	 * peer channel is not established, make a connection using the
	 * PeerConnection.connectWithPeer function.
	 * 
	 * @param parent The parent process
	 * @param peerChannel The connected peer channel
	 */
	public PeerConnection(PeerProcess parent, AsynchronousSocketChannel peerChannel)
	{
		this.parent = parent;
		this.peerChannel = peerChannel;
		this.isConnected = false;
		this.initiatedConnection = false;
		this.isChoked = true;
		this.isInterested = false;
		this.receivedBitfield = false;
		this.isOptimisticallyUnchoked = false;
		try
		{
			int bufferSize = this.parent.getCommonConfig().getPieceSize() * BitTorrent.BUFFER_SCALE;
			this.peerChannel.setOption(StandardSocketOptions.SO_RCVBUF, bufferSize);
			this.peerChannel.setOption(StandardSocketOptions.SO_SNDBUF, bufferSize);
			this.peerChannel.setOption(StandardSocketOptions.SO_REUSEADDR, true);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		this.downloadRate = 0;
	}

	/**
	 * Request a connection with the identified peer
	 * 
	 * @param parent The parent process
	 * @param peerInfo The peer information
	 * @return A connection handler
	 * @throws IOException
	 */
	public static PeerConnection connectWithPeer(PeerProcess parent, PeerInfo peerInfo) throws IOException
	{
		AsynchronousSocketChannel peer = AsynchronousSocketChannel.open();
		InetSocketAddress hostAddress = new InetSocketAddress(peerInfo.getHostName(), peerInfo.getPort());
		try
		{
			peer.connect(hostAddress).get();
		}
		catch (InterruptedException | ExecutionException e)
		{
			StringBuilder errorMessage = new StringBuilder("Error: Unable to connect to peer ");
			errorMessage.append(peerInfo.getPeerID());
			throw new IOException(errorMessage.toString());
		}
		PeerConnection connection = new PeerConnection(parent, peer);
		connection.initiatedConnection = true;
		ByteBuffer buffer = ByteBuffer.allocate(parent.getCommonConfig().getPieceSize() * 4);
		peer.read(buffer, buffer, connection);
		return connection;
	}

	/**
	 * Returns the parent logger
	 * 
	 * @return The parent logger
	 */
	public Logger getLogger()
	{
		return this.parent.getLogger();
	}

	/**
	 * Returns the peer ID of the parent peer process
	 * 
	 * @return The peer ID of the parent peer process
	 */
	public int getMyID()
	{
		return this.parent.getPeerID();
	}

	public PeerProcess getParent()
	{
		return this.parent;
	}

	/**
	 * Returns the peer ID of the connected peer process
	 * 
	 * @return The peer ID of the connected peer process
	 */
	public int getPeerID()
	{
		return this.peerID;
	}

	/**
	 * Returns if the peer with this connection prefers the other peer
	 * 
	 * The relationship indicates that the other peer is preferred by this peer.
	 * 
	 * @param isPreferred
	 */
	public void setPreferred(boolean isPreferred)
	{
		this.isPreferred = isPreferred;
	}

	/**
	 * Gets if the peer with this connection prefers the other peer
	 * 
	 * @param isPreferred
	 */
	public boolean getPreferred()
	{
		return this.isPreferred;
	}

	/**
	 * Returns the parent process of this connection
	 * 
	 * @return The parent process of this connection
	 */
	public PeerProcess getProcess()
	{
		return this.parent;
	}

	/**
	 * Set Choked to true if Choke message sent
	 * 
	 * The relationship indicates that the other peer is preferred by this peer.
	 * 
	 * @param isChoked True if the other peer should be choked
	 */
	public void setChoked(boolean isChoked)
	{
		this.isChoked = isChoked;
	}

	/**
	 * Returns true if the other peer is chocked by this peer
	 * 
	 * The relationship indicates that the other peer is preferred by this peer.
	 * 
	 * @return True if the other peer is choked by this peer
	 */
	public boolean getChoked()
	{
		return this.isChoked;
	}

	/**
	 * Sets if this peer is connected
	 * 
	 * @param connected
	 */
	public synchronized void setConnected(boolean connected)
	{
		this.isConnected = connected;
	}
	
	/**
	 * Sets if the other peer has any interesting pieces
	 * 
	 * @param isChoked
	 */
	public void setInterested(boolean isChoked)
	{
		this.isInterested = isChoked;
	}

	/**
	 * Returns true if the other peer has any interesting pieces
	 * 
	 * @return True if the other peer has any interesting pieces
	 */
	public boolean getInterested()
	{
		return this.isInterested;
	}

	/**
	 * Sets if the other peer is optimistically unchoked by this peer
	 * 
	 * @param isOptimisticallyUnchoked
	 */
	public void setOptimisticallyUnchoked(boolean isOptimisticallyUnchoked)
	{
		this.isOptimisticallyUnchoked = isOptimisticallyUnchoked;
	}

	/**
	 * Indicates if the other peer is optimistically unchoked by this peer
	 * 
	 * @return True if the other peer is optimistically unchoked by this peer
	 */
	public boolean getOptimisticallyUnchoked()
	{
		return this.isOptimisticallyUnchoked;
	}

	/**
	 * Determines if the bitfield message has been received from the other peer.
	 * 
	 * @return True if the bitfield message has been received from the other peer
	 */
	public boolean receivedBitfield()
	{
		return this.receivedBitfield;
	}

	/**
	 * Resets the download rate
	 */
	public void resetDownloadRate()
	{
		this.downloadRate = 0;
	}

	/**
	 * Returns the download rate
	 * 
	 * @return The download rate
	 */
	public int getDownloads()
	{
		return this.downloadRate;
	}

	/**
	 * Returns if this connections is connected with the other peer
	 * 
	 * A connection is established after the bit torrent handshake
	 * 
	 * @return True if the connections are connected
	 */
	public boolean isConnected()
	{
		return this.isConnected;
	}

	/**
	 * Sends a bitfield associated with the peer process' pieces
	 */
	public void sendBitfield()
	{
		Bitfield bitfield = new Bitfield(this.parent);
		byte[] bits = bitfield.getBitfield();
		ActualMessage bitfieldMessage = ActualMessage.CreateBitfieldMessage(bits);
		this.sendMessage(bitfieldMessage);
	}

	/**
	 * Sends a handshake message to the connected peer and bitfield
	 */
	public void sendHandshake()
	{
		HandshakeMessage message = HandshakeMessage.createHandshakeMessage(this.getMyID());
		sendMessage(ByteBuffer.wrap(message.getPayload()));
	}

	/**
	 * Sends isInterested message to the connected peer
	 */
	public void sendInterested()
	{
		ActualMessage message = ActualMessage.CreateInterestedMessage();
		sendMessage(message);
	}

	/**
	 * Sends notInterested message to the connected peer
	 */
	public void sendNotInterested()
	{
		ActualMessage NotInterestedMessage = ActualMessage.CreateNotInterestedMessage();
		this.sendMessage(NotInterestedMessage);
	}

	/**
	 * Sends a message string to the connected peer
	 * 
	 * @param message The message string to send to the connected peer
	 */
	public void sendMessage(String message)
	{
		byte[] byteMessage = message.getBytes();
		ByteBuffer buffer = ByteBuffer.wrap(byteMessage);
		this.sendMessage(buffer);
	}

	/**
	 * Sends a message to the connected peer
	 * 
	 * @param message The message to send to the connected peer
	 */
	public synchronized void sendMessage(ByteBuffer message)
	{
		Future<Integer> writeResult = this.peerChannel.write(message);
		if (this.peerChannel.isOpen())
		{
			try
			{
				writeResult.get();
			}
			catch (InterruptedException | ExecutionException e)
			{
				
			}
		}
	}

	/**
	 * Sends a message to the connected peer
	 * 
	 * @param message The message to send to the connected peer
	 */
	public void sendMessage(ActualMessage message)
	{
		byte[] byteMessage = message.serialize();
		this.sendMessage(ByteBuffer.wrap(byteMessage));
	}

	/**
	 * Closes the connection with the peer.
	 */
	public void closeConnection()
	{
		try
		{
			if (this.peerChannel.isOpen())
			{
				this.peerChannel.close();
			}
			this.setConnected(false);
			if (this.requestor != null)
			{
				this.requestor.cancel();
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Handler for when a message is received.
	 */
	@Override
	public void completed(Integer result, ByteBuffer buffer)
	{
		buffer.flip();
		try
		{
			while (buffer.hasRemaining())
			{
				if (!this.isConnected)
				{
					this.receivedHandshake(buffer);
				}
				else
				{
					int length = buffer.getInt();
					if (length == 0)
					{
						buffer.clear();
					}
					else
					{
						MessageType type = MessageType.parse(buffer.get());
						byte[] payload = new byte[length - 1];
						buffer.get(payload, 0, payload.length);
						ByteBuffer payloadBuffer = ByteBuffer.wrap(payload);
						switch (type)
						{
						case BITFIELD:
							this.receivedBitfield(payloadBuffer);
							break;
						case CHOKE:
							this.receivedChoke(payloadBuffer);
							break;
						case HAVE:
							this.receivedHave(payloadBuffer);
							break;
						case INTERESTED:
							this.receivedInterested(payloadBuffer);
							break;
						case NOT_INTERESTED:
							this.receivedNotInterested(payloadBuffer);
							break;
						case PIECE:
							this.receivedPiece(payloadBuffer);
							break;
						case REQUEST:
							this.recievedRequest(payloadBuffer);
							break;
						case UNCHOKE:
							this.receivedUnchoke(payloadBuffer);
							break;
						default:
							this.receiveUnknown(payloadBuffer);
							break;
						}
					}
				}
			}
		}
		catch (IOException | BufferUnderflowException e)
		{
			
		}
		finally
		{
			buffer.clear();
			this.peerChannel.read(buffer, buffer, this);
		}
	}

	private void receivedHandshake(ByteBuffer payload) throws IOException
	{
		// Verify that header is correct
		byte[] header = new byte[18];
		payload.get(header);
		if (!new String(HandshakeMessage.HEADER).equals(new String(header)))
		{
			throw new IOException("Error: Handshake header incorrect.");
		}

		// Verify that zerobits is correct
		for (int index = 0; index < 10; ++index)
		{
			byte zero = payload.get();
			if (zero != 0)
			{
				throw new IOException("Error: Handshake zerobits incorrect.");
			}
		}
		// TODO verify that the peer ID is the expected one
		this.peerID = payload.getInt();

		this.setConnected(true);
		if (!this.initiatedConnection)
		{
			this.sendHandshake();
			this.parent.getLogger().connectFrom(this.peerID);

		}
		else
		{
			this.parent.getLogger().connectTo(this.peerID);
		}
		// Always send a bitfield after a handshake (we do not check if it has pieces)
		this.sendBitfield();
	}

	private void receivedChoke(ByteBuffer payload)
	{
		this.getLogger().receiveChoke(this.peerID);
		this.requestor.stopProcess();
	}

	private void receivedUnchoke(ByteBuffer payload)
	{
		this.getLogger().receiveUnchoke(this.peerID);

		if (this.requestor == null)
		{
			int pieceToRequest = this.parent.determinePieceRequest(this);
			this.requestor = new RequestPieceProcess(this, pieceToRequest, BitTorrent.REQUEST_TIMEOUT * 1000);
			this.requestor.startProcess();
		}
	}

	private void receivedInterested(ByteBuffer payload)
	{
		this.getLogger().receiveInterested(this.peerID);
		this.isInterested = true;
	}

	private void receivedNotInterested(ByteBuffer payload)
	{
		this.getLogger().receiveNotInterested(this.peerID);
		this.isInterested = false;
	}

	private void receivedHave(ByteBuffer payload)
	{
		int index = payload.getInt();
		this.getLogger().receiveHave(this.peerID, index);

		// Update the peers list of pieces it has
		this.parent.getAllbitfields().updateBitfield(this, index);

		// Check if you have the piece and send interested accordingly.
		if (!(this.parent.getPieces().containsKey(index)))
		{
			this.sendInterested();
		}
	}

	private void receivedBitfield(ByteBuffer payload)
	{
//		StringBuilder builder = new StringBuilder();
//		builder.append(this.getMyID());
//		builder.append(" received a bitfield message from ");
//		builder.append(this.peerID);
//		this.getLogger().writeLog(builder.toString());

		int numBytes = this.parent.getCommonConfig().getBitfieldPieceCount();
		byte[] bits = new byte[numBytes];
		payload.get(bits);
		Bitfield bitfield = new Bitfield(this.parent, bits);
		this.parent.addBitfield(this, bitfield);
		if (this.parent.isInterested(this))
		{
			this.sendInterested();
		}
		else
		{
			this.sendNotInterested();
		}
		this.receivedBitfield = true;
	}

	private void recievedRequest(ByteBuffer payload)
	{
		int index = payload.getInt();
		if (this.parent.getPieces().containsKey(index))
		{
			// If the parent has the key, create the piece message and send it
			byte[] content = this.parent.getPieces().get(index);
			ActualMessage message = ActualMessage.CreatePieceMessage(index, content);
			this.sendMessage(message);
		}
	}

	private void receivedPiece(ByteBuffer payload)
	{
		// Adds the piece to the map of keys
		int index = payload.getInt();
		byte[] piece = new byte[payload.remaining()];
		payload.get(piece);
		this.parent.addPiece(piece, index);
		this.parent.removePendingRequest(index);
		int pieceCount = this.parent.getPieceCount();
		this.getLogger().receivePiece(this.peerID, index, pieceCount);
		this.downloadRate = this.downloadRate + piece.length;

		// Trigger the have message and Check Not Interested status
		this.parent.broadcastHave(index);
		this.parent.checkNotInterested();

		// Check if this is the last piece this peer needed
		this.requestor.stopProcess();
		if (this.parent.hasAllPieces())
		{
			this.parent.completeDownload();
		}
		else
		{
			int pieceToRequest = this.parent.determinePieceRequest(this);
			this.requestor = new RequestPieceProcess(this, pieceToRequest, BitTorrent.REQUEST_TIMEOUT * 1000);
			this.requestor.startProcess();
		}
	}

	private void receiveUnknown(ByteBuffer message)
	{
		String receivedMessage = new String(message.array());
		StringBuilder fullMessage = new StringBuilder("Peer ");
		fullMessage.append(this.parent.getPeerID());
		fullMessage.append(" received the message: ");
		fullMessage.append(receivedMessage);
		System.out.println(fullMessage);
	}

	/**
	 * Handler for when a message is received incorrectly.
	 */
	@Override
	public void failed(Throwable exc, ByteBuffer message)
	{

	}
}

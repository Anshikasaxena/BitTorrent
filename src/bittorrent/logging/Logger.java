package bittorrent.logging;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;

import bittorrent.protocol.peer.connection.PeerConnection;

/**
 * A class that provides standard message logging for the BitTorrent Project
 * 
 * A logger is associated with a peer ID. Each log message is prepended with the
 * time and the peer ID. The time follows the format:
 * 
 * YYYY-MM-DDTHH:MM.MMM
 * 
 * The log file is automatically opened after construction; however, the file
 * requires manual closing.
 */
public class Logger
{
	private static String FILENAME = "Peer ";

	private boolean isWriteToOut;
	private int peerID;
	private BufferedWriter writer;

	/**
	 * Creates a logger associated with the provided peer ID
	 * 
	 * A new file associated with the peer ID is created.
	 * 
	 * @param peerID The ID of the associated peer
	 * @throws IOException If there is an error creating the log file
	 */
	public static Logger createLogger(int peerID) throws IOException
	{
		Logger logger = new Logger();
		logger.peerID = peerID;
		logger.isWriteToOut = false;
		String fileName = FILENAME + String.valueOf(logger.peerID) + ".log";
		Path logFile = Paths.get(fileName);
		logger.writer = Files.newBufferedWriter(logFile);
		return logger;
	}

	/**
	 * Toggles whether the log output should be written to the standard output
	 * 
	 * When enabled, the log will not be written to the log file.
	 * 
	 * @param isWriteToOut True if the log should be written to standard output
	 */
	public void setIsWriteToOut(boolean isWriteToOut)
	{
		this.isWriteToOut = isWriteToOut;
	}

	/**
	 * Gets the if the log output should be written to the standard output
	 * 
	 * @return IsWriteToOut
	 */
	public boolean getIsWriteToOut()
	{
		return this.isWriteToOut;
	}

	/**
	 * Gets the peer ID
	 * 
	 * @return The peer ID
	 */
	public int getPeerID()
	{
		return this.peerID;
	}

	/**
	 * Closes the log file writter
	 */
	public void closeLog()
	{
		try
		{
			this.writer.close();
		}
		catch (IOException e)
		{
			System.err.println("Error: could not close the log file.");
		}
	}

	/**
	 * Indicates that this peer has connected with another peer.
	 * 
	 * This should be called when this peer connects with the provided pair.
	 * 
	 * @param connectedPeer The peer connected with
	 */
	public void connectTo(int connectedPeer)
	{
		StringBuilder builder = createBuilder();
		builder.append(" makes a connection to Peer ");
		builder.append(connectedPeer);
		builder.append(".");
		writeLog(builder.toString());
	}

	/**
	 * Indicates that this peer has connected with another peer.
	 * 
	 * This should be called when the provided pair makes the connection with this
	 * peer.
	 * 
	 * @param connectedPeer The peer connected with
	 */
	public void connectFrom(int connectedPeer)
	{
		StringBuilder builder = createBuilder();
		builder.append(" is connected from Peer ");
		builder.append(connectedPeer);
		builder.append(".");
		writeLog(builder.toString());
	}

	/**
	 * Indicates that this peer has changed its preferred neighbors
	 * 
	 * @param peers A list of peer IDs
	 */
	public void changePreferredNeighborsIDs(List<Integer> peers)
	{
		StringBuilder builder = createBuilder();
		builder.append(" has the preferred neighbors ");
		if (peers.size() > 0)
		{
			builder.append(peers.get(0));
		}
		for (int peerIndex = 1; peerIndex < peers.size(); ++peerIndex)
		{
			builder.append(", ");
			builder.append(peers.get(peerIndex));
		}
		builder.append(".");
		writeLog(builder.toString());
	}

	/**
	 * Indicates that this peer has changed its preferred neighbors
	 * 
	 * @param peers A list of peer IDs
	 */
	public void changePreferredNeighbors(List<PeerConnection> peers)
	{
		StringBuilder builder = createBuilder();
		builder.append(" has the preferred neighbors ");
		if (peers.size() > 0)
		{
			builder.append(peers.get(0).getPeerID());
		}
		for (int peerIndex = 1; peerIndex < peers.size(); ++peerIndex)
		{
			builder.append(", ");
			builder.append(peers.get(peerIndex).getPeerID());
		}
		builder.append(".");
		writeLog(builder.toString());
	}

	/**
	 * Indicates that a new optimistically unchoked neighbor was selected
	 * 
	 * @param peer The new optimistically unchoked neighbor
	 */
	public void changeOptimisticallyUnchokedID(int peerID)
	{
		StringBuilder builder = createBuilder();
		builder.append(" has the optimistically unchoked neighbor ");
		builder.append(peerID);
		builder.append(".");
		writeLog(builder.toString());
	}

	/**
	 * Indicates that a new optimistically unchoked neighbor was selected
	 * 
	 * @param peer The new optimistically unchoked neighbor
	 */
	public void changeOptimisticallyUnchoked(PeerConnection peer)
	{
		StringBuilder builder = createBuilder();
		builder.append(" has the optimistically unchoked neighbor ");
		builder.append(peer.getPeerID());
		builder.append(".");
		writeLog(builder.toString());
	}

	/**
	 * Indicates that a peer is unchoked by a neighbor.
	 * 
	 * This should be called when a peer receives the "unchoke" message.
	 * 
	 * @param peer The neighbor who unchokes
	 */
	public void receiveUnchoke(int peer)
	{
		StringBuilder builder = createBuilder();
		builder.append(" is unchoked by ");
		builder.append(peer);
		builder.append(".");
		writeLog(builder.toString());
	}

	/**
	 * Indicates that a peer is choked by a neighbor
	 * 
	 * This should be called when a peer receives the "choke" message
	 * 
	 * @param peer The neighbor who chokes
	 */
	public void receiveChoke(int peer)
	{
		StringBuilder builder = createBuilder();
		builder.append(" is choked by ");
		builder.append(peer);
		builder.append(".");
		writeLog(builder.toString());
	}

	/**
	 * Indicates that this peer received the "have" message for a given piece
	 * 
	 * @param peer The peer who sent the message
	 * @param piece The piece index contained in the message
	 */
	public void receiveHave(int peer, int piece)
	{
		StringBuilder builder = createBuilder();
		builder.append(" received the 'have' message from ");
		builder.append(peer);
		builder.append(" for the piece ");
		builder.append(piece);
		builder.append(".");
		writeLog(builder.toString());
	}

	/**
	 * Indicates that this peer received an "interested" message
	 * 
	 * @param peer The peer who sent the "interested" message
	 */
	public void receiveInterested(int peer)
	{
		StringBuilder builder = createBuilder();
		builder.append(" received the 'interested' message from ");
		builder.append(peer);
		builder.append(".");
		writeLog(builder.toString());
	}

	/**
	 * Indicates that this peer received an "not interested" message
	 * 
	 * @param peer The peer who sent the "not interested" message
	 */
	public void receiveNotInterested(int peer)
	{
		StringBuilder builder = createBuilder();
		builder.append(" received the 'not interested' message from ");
		builder.append(peer);
		builder.append(".");
		writeLog(builder.toString());
	}

	/**
	 * Indicates that this peer has downloaded a piece and the total piece count
	 * 
	 * @param peer The peer who sent the piece
	 * @param pieceIndex The piece index the peer has downloaded
	 * @param pieceCount The number of pieces the peer currently has
	 */
	public void receivePiece(int peer, int pieceIndex, int pieceCount)
	{
		StringBuilder builder = createBuilder();
		builder.append(" has downloaded the piece ");
		builder.append(pieceIndex);
		builder.append(" from ");
		builder.append(peer);
		builder.append(". Now the number of pieces it has is ");
		builder.append(pieceCount);
		writeLog(builder.toString());
	}

	/**
	 * Indicates that this peer has downloaded a piece and the total piece count
	 * 
	 * @param peer The peer who sent the piece
	 * @param pieceIndex The piece index the peer has downloaded
	 * @param pieceCount The number of pieces the peer currently has
	 */
	public void downloadCompleted()
	{
		StringBuilder builder = createBuilder();
		builder.append(" has downloaded the complete file. ");
		writeLog(builder.toString());
	}

	/**
	 * Write the message to the log
	 * 
	 * @param message The message to write
	 */
	public void writeLog(String message)
	{
		if (this.isWriteToOut)
		{
			System.out.println(message);
		}
		else
		{
			try
			{
				this.writer.write(message);
				this.writer.newLine();
				this.writer.flush();
			}
			catch (IOException e)
			{
				System.err.println("Error: could not write to the log file.");
			}
		}
	}

	private StringBuilder createBuilder()
	{
		StringBuilder builder = new StringBuilder("[");
		builder.append(LocalDateTime.now().toString());
		builder.append("] Peer ");
		builder.append(this.peerID);
		return builder;
	}
}

package bittorrent.protocol.peer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import bittorrent.BitTorrent;
import bittorrent.app.BitTorrentSettings;
import bittorrent.config.CommonConfig;
import bittorrent.config.PeerInfo;
import bittorrent.config.PeerInfoConfig;
import bittorrent.logging.Logger;
import bittorrent.protocol.messages.ActualMessage;
import bittorrent.protocol.peer.connection.AllBitfields;
import bittorrent.protocol.peer.connection.Bitfield;
import bittorrent.protocol.peer.connection.ChokingProcess;
import bittorrent.protocol.peer.connection.OptimisticallyUnchokedProcess;
import bittorrent.protocol.peer.connection.PeerConnection;
import bittorrent.utility.FileHandling;

//@formatter:off
/**
 * The main peer process that executed the BitTorrent protocol.
 * 
 * This class performs multiple operations upon startup:
 * 	- Initializes state based upon the config files
 * 	- Creates a peer directory if it does not exist
 * 	- Initializes a log file for the peer
 * 	- If peer is supposed to have a copy of the shared file, copy it over to the peer directory
 *  - Split the shared file into pieces, it is is suppose dto have the shared file
 *  - Request connections to each peer it is supposed to connected to
 * 
 * After startup, the runProtocol function can be called. When the protocol is 
 * run, several operations are performed. 
 * 	- The choking/preferred neighbors process is started
 * 	- The optimistically unchoking process is started.
 * 	- Enter protocol until node termination conditions is reached.
 * 
 * A peer process contains multiple peer connections, where each peer connection
 * represents the TCP connection between another peer.
 */
//@formatter:on
public class PeerProcess
{
	private int peerID;
	private int port;
	private String host;
	private List<PeerConnection> peerConnections;
	private AsynchronousServerSocketChannel serverChannel;
	private Path peerDirectory;
	private CommonConfig commonConfig;
	private PeerInfoConfig peerConfig;
	private Random random;
	private Logger logger;
	private ChokingProcess chokingProcess;
	private OptimisticallyUnchokedProcess optimisticallyProcess;
	private boolean processStarted;

	private AllBitfields allbitfields;
	private HashMap<Integer, byte[]> pieces;
	private ArrayList<Integer> pendingRequests;

	public PeerProcess(BitTorrentSettings settings)
	{
		this.peerID = settings.getStartPeerID();
		this.port = settings.getStartPortNumber();
		this.peerConnections = new ArrayList<PeerConnection>();
		this.pieces = new HashMap<Integer, byte[]>();
		this.pendingRequests = new ArrayList<Integer>();
		this.random = new Random();
		this.allbitfields = new AllBitfields();

		try
		{
			createPeerDirectory();
			createLogger();
			this.commonConfig = CommonConfig.setCommonConfig(settings);
			this.peerConfig = PeerInfoConfig.setPeerInfo(settings);
			splitFile();
			acceptConnections();
		}
		catch (Exception e)
		{
			StringBuilder errorMessage = new StringBuilder("Error: ");
			errorMessage.append(this.peerID);
			errorMessage.append(" encountered an error. ");
			errorMessage.append(e.getMessage());
			System.err.println(errorMessage.toString());
		}
	}

	/**
	 * Create a peer process with the provided peer ID and port number
	 * 
	 * The peer ID is not checked for uniqueness
	 * 
	 * @param peerID The ID of the peer
	 * @param port The port number to accept connections
	 */
	public PeerProcess(int peerID, int port)
	{
		this.peerID = peerID;
		this.port = port;
		this.peerConnections = new ArrayList<PeerConnection>();
		this.pieces = new HashMap<Integer, byte[]>();
		this.pendingRequests = new ArrayList<Integer>();
		this.random = new Random();
		this.allbitfields = new AllBitfields();

		try
		{
			createPeerDirectory();
			createLogger();
			readConfigFiles();
			splitFile();
			acceptConnections();
		}
		catch (Exception e)
		{
			StringBuilder errorMessage = new StringBuilder("Error: ");
			errorMessage.append(this.peerID);
			errorMessage.append(" encountered an error. ");
			errorMessage.append(e.getMessage());
			System.err.println(errorMessage.toString());
		}
	}

	/**
	 * Returns the peer ID for this process
	 * 
	 * @return The peer ID for this process
	 */
	public int getPeerID()
	{
		return this.peerID;
	}

	/**
	 * Returns the map of pieces
	 * 
	 * @return The map of pieces
	 */
	public HashMap<Integer, byte[]> getPieces()
	{
		return this.pieces;
	}

	/**
	 * Returns the number of pieces this peer has
	 * 
	 * @return The number of pieces this peer has
	 */
	public int getPieceCount()
	{
		return this.pieces.size();
	}

	/**
	 * Adds the piece to the corresponding index
	 * 
	 * @param piece The piece to add
	 * @param index The index of the piece
	 */
	public synchronized void addPiece(byte[] piece, int index)
	{
		if (!this.pieces.containsKey(index))
		{
			this.pieces.put(index, piece);
		}
	}

	/**
	 * Determine a random piece to request available from the peer
	 * 
	 * The requested piece is random amongst the total available piece indices that
	 * the peer has, but this peer does not have. Additionally, the pending pieces
	 * that have been requested are removed from the choice of pieces.
	 * 
	 * @param connection The peer connection to request a piece from
	 * @return The piece index to request or -1 if there are no available pieces
	 */
	public int determinePieceRequest(PeerConnection connection)
	{
		Bitfield myBitfield = new Bitfield(this);
		Bitfield peerBitfield = this.allbitfields.getBitfield(connection);

		int randomPieceIndex = -1;
		if (peerBitfield != null)
		{
			// Determine what new pieces the other peer can provide
			List<Integer> requiredPieces = myBitfield.getRequiredPieces(peerBitfield);

			if (requiredPieces.size() > 0)
			{
				// Remove all pieces already requested
				requiredPieces.removeAll(this.pendingRequests);
				if (requiredPieces.size() > 0)
				{
					int randomListIndex = this.random.nextInt(requiredPieces.size());
					randomPieceIndex = requiredPieces.get(randomListIndex);
				}
			}
			else
			{
			}
		}
		return randomPieceIndex;
	}

	/**
	 * Checks if this peer has at least one piece
	 * 
	 * @return True if the peer has at least one piece
	 */
	public boolean hasPieces()
	{
		return this.pieces.size() > 0;
	}

	/**
	 * Checks if this peer has all of the pieces
	 * 
	 * @return True if the peer has all of the pieces
	 */
	public boolean hasAllPieces()
	{
		int configPieceCount = this.commonConfig.getNumberOfPieces();
		int actualPieceCount = this.pieces.size();
		return configPieceCount == actualPieceCount;
	}

	/**
	 * Checks if the peer has all of the pieces
	 * 
	 * This function checks all peers to see if any are interested
	 * 
	 * @return True if all neighbors have the pieces
	 */
	public boolean doAllPeersHavePieces()
	{
		boolean allPieces = true;
		for (PeerConnection connection : this.peerConnections)
		{
			Bitfield bitfield = this.allbitfields.getBitfield(connection);
			if (bitfield != null)
			{
				boolean hasAllPieces = bitfield.hasAllPieces();
				if (!connection.receivedBitfield() || !hasAllPieces)
				{
					String baseDirectory = "peer_";
					Path peerFile = Paths.get(baseDirectory + connection.getPeerID(), this.commonConfig.getFileName());
					if (!Files.exists(peerFile))
					{
						allPieces = false;
						break;
					}
				}
			}
			else
			{
				String baseDirectory = "peer_";
				Path peerFile = Paths.get(baseDirectory + connection.getPeerID(), this.commonConfig.getFileName());
				if (!Files.exists(peerFile))
				{
					allPieces = false;
					break;
				}
			}
		}
		return allPieces;
	}

	public boolean hasMadeAllConnections()
	{
		int connectionCount = this.peerConnections.size();
		int requiredCount = this.peerConfig.getPeers().size() - 1;
		return connectionCount == requiredCount;
	}
	
	/**
	 * Returns the bitfields of all peers
	 * 
	 * @return The bitfields of all peers
	 */
	public AllBitfields getAllbitfields()
	{
		return this.allbitfields;
	}

	/**
	 * Adds the bitfield with the corresponding connection to the list of connection
	 * 
	 * @param connection The peer connection assitioated with the bitfield
	 * @param bitfield The bitfield corresponding to the peer connection
	 */
	public void addBitfield(PeerConnection connection, Bitfield bitfield)
	{
		this.allbitfields.addBitfield(connection, bitfield);
	}

	/**
	 * Checks if the provided peer connection has anything of interest
	 * 
	 * A piece is "interesting" if the other connection has a piece that this peer
	 * does not.
	 * 
	 * @param connection The peer to check if there are any interesting pieces.
	 * @return True if the peer has any interesting pieces.
	 */
	public boolean isInterested(PeerConnection connection)
	{
		Bitfield myBitfield = new Bitfield(this);
		Bitfield peerBitfield = this.allbitfields.getBitfield(connection);
		return myBitfield.isInterested(peerBitfield);
	}

	/**
	 * Check the bitfields of all the neighbors to see if they have a piece that
	 * this peer is interested in
	 */
	public void checkNotInterested()
	{
		Bitfield myBitfield = new Bitfield(this);
		HashMap<PeerConnection, Bitfield> bitfields = this.allbitfields.getBitfields();
		for (HashMap.Entry<PeerConnection, Bitfield> entry : bitfields.entrySet())
		{
			PeerConnection connection = entry.getKey();
			Bitfield peerBitfield = entry.getValue();

			// Send an uninterested message if the connection was previously of interest,
			// but it no longer is
			if (!myBitfield.isInterested(peerBitfield) && connection.getInterested())
			{
				connection.sendNotInterested();
			}
		}
	}

	/**
	 * Returns the logger
	 * 
	 * @return The logger
	 */
	public Logger getLogger()
	{
		return this.logger;
	}

	/**
	 * Returns the common config file settings
	 * 
	 * @return The common config file settings
	 */
	public CommonConfig getCommonConfig()
	{
		return this.commonConfig;
	}

	/**
	 * Returns the list of peer connections
	 * 
	 * @return The list peer connections
	 */
	public List<PeerConnection> getPeerConnections()
	{
		return this.peerConnections;
	}

	/**
	 * Requests connections to each peer identified in the peer info list
	 * 
	 * If a request is denied, it is repeated up to CONNECTION_ATTEMPTS times. A one
	 * second interval is added between each attempt.
	 * 
	 * @param peerInfoList
	 */
	public void makeConnections(List<PeerInfo> peerInfoList)
	{
		for (PeerInfo peerInfo : peerInfoList)
		{
			// Repeatedly attempt to connect with the peer
			boolean madeConnection = false;
			for (int attempt = 0; attempt < BitTorrent.CONNECTION_ATTEMPTS; ++attempt)
			{
				try
				{
					PeerConnection peerConnection = PeerConnection.connectWithPeer(this, peerInfo);
					peerConnections.add(peerConnection);
					peerConnection.sendHandshake();
					madeConnection = true;
					break;
				}
				catch (IOException e)
				{
					try
					{
						TimeUnit.SECONDS.sleep(1);
					}
					catch (InterruptedException e1)
					{
						e1.printStackTrace();
					}
				}
			}
			if (!madeConnection)
			{
				StringBuilder errorMessage = new StringBuilder("Error: peer ");
				errorMessage.append(this.getPeerID());
				errorMessage.append(" was unable to connect to");
				errorMessage.append(peerInfo.getPeerID());
				errorMessage.append(".");
				System.err.print(errorMessage.toString());
			}
		}
	}

	/**
	 * Requests connections to each peer identified in the peer config file
	 * 
	 * If a request is denied, it is repeated up to CONNECTION_ATTEMPTS times. A one
	 * second interval is added between each attempt.
	 * 
	 * @param peerInfoList
	 */
	public void makeConnections()
	{
		// Initiate connection request to all peers after itself
		List<PeerInfo> peers = this.peerConfig.getPeers();
		List<PeerInfo> peerToConnect = new ArrayList<PeerInfo>();
		boolean foundSelf = false;
		for (PeerInfo peer : peers)
		{
			if (foundSelf)
			{
				peerToConnect.add(peer);
			}
			else if (peer.getPeerID() == this.peerID)
			{
				foundSelf = true;
			}
		}
		this.makeConnections(peerToConnect);
	}

	/**
	 * Broadcasts the have message to all peers
	 * 
	 * @param piece The index of the piece to broadcast to all connections
	 */
	public void broadcastHave(int index)
	{
		ActualMessage HaveMessage = ActualMessage.CreateHaveMessage(index);
		List<PeerConnection> peerConnections = this.getPeerConnections();
		for (PeerConnection peer : peerConnections)
		{
			peer.sendMessage(HaveMessage);
		}
	}

	/**
	 * Adds a piece index for a request
	 * 
	 * @param requestedIndex The requested piece index
	 */
	public synchronized void addPendingRequest(Integer requestedIndex)
	{
		if (!this.pendingRequests.contains(requestedIndex))
		{
			this.pendingRequests.add(requestedIndex);
		}
	}

	/**
	 * Removes the requested piece index
	 * 
	 * @param requestedIndex The requested piece index
	 */
	public synchronized void removePendingRequest(Integer requestedIndex)
	{
		while (this.pendingRequests.remove(requestedIndex))
		{

		}
	}

	/**
	 * Clears the requested pieces
	 */
	public synchronized void clearPendingRequests()
	{
		this.pendingRequests.clear();
	}

	/**
	 * Completes the download of all the pieces
	 * 
	 * A message indicating that the download has completed is logged. Additionally,
	 * the merged file is written to the file system.
	 */
	public void completeDownload()
	{
		this.getLogger().downloadCompleted();

		Path outputFile = this.peerDirectory.resolve(this.commonConfig.getFileName());
		FileHandling.mergeFile(outputFile, this.pieces);
	}

	/**
	 * Runs the BitTorrent protocol
	 * 
	 * The choking and optimistically unchoking process is started. The function
	 * continues until the node termination condition is met. Specifically, the
	 * protocol continues to run until this peer has all the pieces and all the
	 * peers have all the peers.
	 */
	public void runProtocol()
	{
		long chokingInterval = this.commonConfig.getUnchokingInterval().getSeconds() * 1000;
		// @formatter:off
		this.chokingProcess = new ChokingProcess(
				this,
				this.logger,
				this.peerConnections, 
				commonConfig.getNumberOfPreferredNeightbors(),
				chokingInterval,
				hasAllPieces());
		// @formatter:on

		long optimisticallyInterval = this.commonConfig.getOptimisticUnchokingInterval().getSeconds() * 1000;
		// @formatter:off
		this.optimisticallyProcess = new OptimisticallyUnchokedProcess(
				this.logger,
				this.peerConnections,
				optimisticallyInterval);
		// @formatter:on

		// Terminate when this peer has all the pieces and all of its connected peers
		// have all pieces.
		while (!this.hasAllPieces() || !this.doAllPeersHavePieces() || !this.hasMadeAllConnections())
		{
			if (!this.processStarted)
			{
				this.initializeProcesses();
			}
			try
			{
				TimeUnit.SECONDS.sleep(1);
			}
			catch (InterruptedException e)
			{

			}
		}
		StringBuilder message = new StringBuilder("Peer ");
		message.append(this.peerID);
		message.append(" has terminated.");
		this.logger.writeLog(message.toString());

		this.optimisticallyProcess.stopProcess();
		this.chokingProcess.stopProcess();
		this.closeConnections();
		this.closeLog();
	}

	protected void initializeProcesses()
	{
		// Starts the choking and optimistically unchoking processes
		boolean shouldStart = true;
		for (PeerConnection connection : this.peerConnections)
		{
			if (!connection.receivedBitfield())
			{
				shouldStart = false;
				return;
			}
		}
		if (shouldStart)
		{
			this.chokingProcess.startProcess();
			this.optimisticallyProcess.startProcess();
			this.processStarted = true;
		}
	}

	protected void closeConnections()
	{
		// Closes the connection to each peer.
		for (PeerConnection peer : this.peerConnections)
		{
			peer.closeConnection();
		}
	}

	protected void closeLog()
	{
		// Closes the log file
		this.logger.closeLog();
	}

	protected void createPeerDirectory() throws IOException
	{
		StringBuilder peerDirectoryStr = new StringBuilder("peer_");
		peerDirectoryStr.append(this.peerID);

		// Check if peer directory exists. If not, create one.
		this.peerDirectory = Paths.get(peerDirectoryStr.toString());
		try
		{
			if (!Files.exists(this.peerDirectory) || !Files.isDirectory(this.peerDirectory))
			{
				Files.createDirectory(this.peerDirectory);
			}
		}
		catch (IOException e)
		{
			StringBuilder errorMessage = new StringBuilder("Error: unable to create peer directory ");
			errorMessage.append(this.peerDirectory.toAbsolutePath());
			errorMessage.append(". ");
			throw new IOException(errorMessage.toString());
		}
	}

	protected void createLogger() throws IOException
	{
		this.logger = Logger.createLogger(this.peerID);
//		this.logger.setIsWriteToOut(true);
	}

	protected void readConfigFiles() throws IOException
	{
		Path commonConfigPath = BitTorrent.COMMON_CONFIG_FILE;
		this.commonConfig = CommonConfig.readCommonConfig(commonConfigPath);

		Path peerConfigPath = BitTorrent.PEER_CONFIG_FILE;
		this.peerConfig = PeerInfoConfig.ReadPeerInfoConfig(peerConfigPath);
		this.host = this.peerConfig.getPeerFromId(this.peerID).getHostName();
	}

	protected void splitFile() throws IOException
	{
		// Check if this peer has the full file according to the common config file
		PeerInfo info = this.peerConfig.getPeerFromId(this.peerID);
		if (info != null)
		{
			String sharedFile = this.commonConfig.getFileName();
			Path localFileCopy = this.peerDirectory.resolve(sharedFile);
			if (info.getHasFile())
			{
				this.pieces = FileHandling.splitFile(this.commonConfig);

				// Add the file to the local peer directory;
				if (Files.notExists(localFileCopy))
				{
					Files.copy(this.commonConfig.getPath(), localFileCopy);
				}
			}
			else
			{
				Files.deleteIfExists(localFileCopy);
			}
		}
	}

	protected void acceptConnections() throws IOException
	{
		// Create an asynchronous server socket channel and bind it to a local address.
		this.serverChannel = AsynchronousServerSocketChannel.open();
		this.serverChannel.bind(new InetSocketAddress(this.host, this.port));

		PeerProcess parent = this;
		this.serverChannel.accept(null, new CompletionHandler<AsynchronousSocketChannel, Object>()
		{
			/**
			 * Connection request is received.
			 * 
			 * Establish a peer connection with the requester and delagate all future
			 * messages to the peer connection handler.
			 */
			@Override
			public void completed(AsynchronousSocketChannel peerChannel, Object attachment)
			{
				if (serverChannel.isOpen())
				{
					// Continue to accept requests
					serverChannel.accept(null, this);
				}
				if (peerChannel != null && peerChannel.isOpen())
				{
					PeerConnection connection = new PeerConnection(parent, peerChannel);
					peerConnections.add(connection);
					ByteBuffer buffer = ByteBuffer.allocate(commonConfig.getPieceSize() * BitTorrent.BUFFER_SCALE);
					peerChannel.read(buffer, buffer, connection);
				}
			}

			@Override
			public void failed(Throwable exc, Object attachment)
			{

			}
		});
	}

	/**
	 * Launches all peers as threads from a single process.
	 * 
	 * The peer information is obtained from the PeerInfo.cfg file. This is
	 * primarily used as a developer and tester function, and is not intended to be
	 * called under normal operations of this BitTorrent implementation.
	 */
	public static void createNodeThreads()
	{
		Path peerConfigPath = BitTorrent.PEER_CONFIG_FILE;
		try
		{
			PeerInfoConfig peerConfig = PeerInfoConfig.ReadPeerInfoConfig(peerConfigPath);
			List<PeerProcess> peerProcesses = new ArrayList<PeerProcess>();
			for (PeerInfo peer : peerConfig.getPeers())
			{
				PeerProcess peerProcess = new PeerProcess(peer.getPeerID(), peer.getPort());
				peerProcesses.add(peerProcess);
			}
			for (PeerProcess peerProcess : peerProcesses)
			{
				peerProcess.makeConnections();

				Runnable runnable = () ->
				{
					peerProcess.runProtocol();
				};
				Thread t = new Thread(runnable);
				t.start();
			}
			TimeUnit.SECONDS.sleep(1);
		}
		catch (IOException | InterruptedException e1)
		{
			e1.printStackTrace();
		}
	}

	/**
	 * Launches a single peer thread with the provied peer ID.
	 * 
	 * This function is intended to be the primary method to launching the
	 * application. Specifically, this function launches the peer process in the
	 * specification provided in the project description.
	 * 
	 * @param id The ID of the peer.
	 */
	public static void createNodeProcess(int id)
	{
		Path peerConfigPath = BitTorrent.PEER_CONFIG_FILE;
		PeerInfoConfig peerConfig;
		try
		{
			peerConfig = PeerInfoConfig.ReadPeerInfoConfig(peerConfigPath);
			PeerInfo peer = peerConfig.getPeerFromId(id);
			PeerProcess peerProcess = new PeerProcess(peer.getPeerID(), peer.getPort());
			peerProcess.makeConnections();
			peerProcess.runProtocol();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static void createNodeProcess(BitTorrentSettings settings)
	{
		PeerProcess peerProcess = new PeerProcess(settings);
		peerProcess.makeConnections();
		peerProcess.runProtocol();
	}
	
	public static void main(String[] args)
	{
		try
		{
			if (args.length > 2)
			{
				BitTorrentSettings settings = BitTorrentSettings.initializeSettings(args);
				createNodeProcess(settings);

			}
			if (args.length == 2 && Boolean.parseBoolean(args[1]))
			{
				createNodeThreads();
			}
			else if (args.length == 1)
			{
				int peerID = Integer.parseInt(args[0]);
				createNodeProcess(peerID);
			}
			else
			{
				throw new InvalidParameterException("Error: PeerProcess.jar takes at least 1 argument");
			}
		}
		catch (Exception e)
		{
			StringBuilder message = new StringBuilder(e.toString());
			message.append("\n\nUsage: \n\n");
			message.append("java -jar PeerProcess.jar [process ID] (launch all peers)\n");
			message.append("where,\n");
			message.append("    - process ID: the ID of the peer to launch.\n");
			message.append("    - launch all peers: boolean indicated if all peers should be ");
			message.append("launched at once according to the config file. The peer ID does not ");
			message.append("matter when this flag is used.");
			System.err.println(message);
		}
	}
}

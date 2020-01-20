package bittorrent.app;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map.Entry;

import bittorrent.BitTorrent;
import bittorrent.config.CommonConfig;
import bittorrent.config.PeerInfoConfig;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Settings for the bit torrent configuration application.
 * 
 * Bindings for each of the configuration parameters are provided
 * 
 * Note: this class is only used for the GUI and model bindings
 */
public class BitTorrentSettings
{
	// Peer Info Settings
	private final BooleanProperty usePeerConfigFile = new SimpleBooleanProperty();
	private final ObjectProperty<Path> peerConfigFile = new SimpleObjectProperty<Path>();
	private final StringProperty commonServer = new SimpleStringProperty();
	private final ObjectProperty<Integer> numberOfPeers = new SimpleObjectProperty<Integer>();
	private final ObjectProperty<Integer> startPortNumber = new SimpleObjectProperty<Integer>();
	private final ObjectProperty<Integer> startPeerID = new SimpleObjectProperty<Integer>();

	// Common Info Settings
	private final BooleanProperty useCommonConfigFile = new SimpleBooleanProperty();
	private final ObjectProperty<Path> commonConfigFile = new SimpleObjectProperty<Path>();
	private final ObjectProperty<Path> sharedFile = new SimpleObjectProperty<Path>();
	private final ObjectProperty<Integer> numberOfPreferredNeighbors = new SimpleObjectProperty<Integer>();
	private final ObjectProperty<Integer> unchokingInterval = new SimpleObjectProperty<Integer>();
	private final ObjectProperty<Integer> optimisticallyUnchokednterval = new SimpleObjectProperty<Integer>();
	private final ObjectProperty<Integer> pieceSize = new SimpleObjectProperty<Integer>();

	// Miscellaneous Settings
	private final BooleanProperty allNeighbors = new SimpleBooleanProperty();
	private final ObjectProperty<Integer> numberOfNeighbors = new SimpleObjectProperty<Integer>();
	
	private final DoubleProperty progressProperty = new SimpleDoubleProperty();
	private final StringProperty logTextProperty = new SimpleStringProperty();
	
	/**
	 * Initializes a settings the the value specified in the config file.
	 * 
	 * @param commonConfigFile Path to the common config file
	 * @param peerConfigFile Path to the peer config file
	 * @return A settings initilized to the values specified in the common config
	 * file and the peer config file.
	 * @throws IOException If there is an error reading either the common or peer
	 * config files.
	 */
	public static BitTorrentSettings initializeSettings(Path commonConfigFile, Path peerConfigFile)
	{
		BitTorrentSettings settings = new BitTorrentSettings();
		try
		{
			PeerInfoConfig peerConfig = PeerInfoConfig.ReadPeerInfoConfig(peerConfigFile);
			settings.usePeerConfigFile.setValue(true);
			settings.peerConfigFile.setValue(peerConfigFile);
			settings.numberOfPeers.setValue(peerConfig.getPeers().size());
			settings.startPortNumber.setValue(peerConfig.getPeers().get(0).getPort());
			settings.startPeerID.setValue(peerConfig.getPeers().get(0).getPeerID());
			settings.numberOfNeighbors.setValue(peerConfig.getPeers().size() - 1);
			settings.commonServer.setValue(peerConfig.getPeers().get(0).getHostName());
		}
		catch (IOException e)
		{

		}
		try
		{
			CommonConfig commonConfig = CommonConfig.readCommonConfig(commonConfigFile);
			settings.useCommonConfigFile.setValue(true);
			settings.commonConfigFile.setValue(commonConfigFile);
			settings.sharedFile.setValue(Paths.get(commonConfig.getFileName()));
			settings.numberOfPreferredNeighbors.setValue(commonConfig.getNumberOfPreferredNeightbors());
			settings.unchokingInterval.setValue((int) commonConfig.getUnchokingInterval().getSeconds());
			settings.optimisticallyUnchokednterval
					.setValue((int) commonConfig.getOptimisticUnchokingInterval().getSeconds());
			settings.pieceSize.setValue(commonConfig.getPieceSize());
		}
		catch (IOException e)
		{

		}
		settings.progressProperty.setValue(0);
		settings.logTextProperty().setValue("");
		return settings;
	}

	public static BitTorrentSettings initializeSettings(String[] args)
	{
		Parser parser = Parser.parser(args);
		HashMap<BitTorrentOption, String> map = parser.getOptionsMap();
		String commonValue = map.get(BitTorrentOption.COMMON);
		Path commonPath = BitTorrent.COMMON_CONFIG_FILE;
		if (commonValue != null)
		{
			commonPath = Paths.get(commonValue);
		}
		String peerValue = map.get(BitTorrentOption.PEERS);
		Path peerPath = BitTorrent.PEER_CONFIG_FILE;
		if (peerValue != null)
		{
			peerPath = Paths.get(peerValue);
		}

		// Initialize settings with the parameters specified in the peer files.
		BitTorrentSettings settings = BitTorrentSettings.initializeSettings(commonPath, peerPath);
		for (Entry<BitTorrentOption, String> option : parser.getOptionsMap().entrySet())
		{
			switch (option.getKey())
			{
			case NUM_PEERS:
				settings.setNumberOfPeers(Integer.getInteger(option.getValue()));
				break;
			case NUM_PREF_NEIGHBORS:
				settings.setNumberOfPreferredNeighbors(Integer.getInteger(option.getValue()));
				break;
			case OPT_UNCHOKE_INT:
				settings.setOptimisticallyUnchokednterval(Integer.getInteger(option.getValue()));
				break;
			case PIECE_SIZE:
				settings.setPieceSize(Integer.getInteger(option.getValue()));
				break;
			case SERVER:
				settings.setCommonServer(option.getValue());
				break;
			case SHARED:
				Path sharedFile = Paths.get(option.getValue());
				settings.setSharedFile(sharedFile);
				break;
			case START_PEER:
				settings.setStartPeerID(Integer.getInteger(option.getValue()));
				break;
			case START_PORT:
				settings.setStartPortNumber(Integer.getInteger(option.getValue()));
				break;
			case UNCHOKE_INT:
				settings.setUnchokingIntervalProperty(Integer.getInteger(option.getValue()));
				break;
			default:
				break;

			}
		}
		return settings;
	}

	public boolean getAllNeighbors()
	{
		return this.allNeighbors.getValue();
	}

	public void setAllNeighbors(boolean value)
	{
		this.allNeighbors.setValue(value);
	}

	public BooleanProperty allNeighborsProperty()
	{
		return this.allNeighbors;
	}

	public String getLogText()
	{
		return this.logTextProperty.getValue();
	}

	public void setLogText(String value)
	{
		this.logTextProperty.setValue(value);
	}

	public StringProperty logTextProperty()
	{
		return this.logTextProperty;
	}

	public String getCommonServer()
	{
		return this.commonServer.getValue();
	}

	public void setCommonServer(String value)
	{
		this.commonServer.setValue(value);
	}

	public StringProperty commonServerProperty()
	{
		return this.commonServer;
	}

	public int getNumberOfNeighbors()
	{
		return this.numberOfNeighbors.getValue();
	}

	public void setNumberOfNeighbors(int value)
	{
		this.numberOfNeighbors.setValue(value);
	}

	public ObjectProperty<Integer> numberOfNeighborsProperty()
	{
		return this.numberOfNeighbors;
	}

	public int getPieceSize()
	{
		return this.pieceSize.getValue();
	}

	public void setPieceSize(int value)
	{
		this.pieceSize.setValue(value);
	}

	public ObjectProperty<Integer> pieceSizeProperty()
	{
		return this.pieceSize;
	}

	public double getProgress()
	{
		return this.progressProperty.getValue();
	}

	public void setProgress(double value)
	{
		this.progressProperty.setValue(value);
	}

	public DoubleProperty progressProperty()
	{
		return this.progressProperty;
	}

	public int getOptimisticallyUnchokednterval()
	{
		return this.optimisticallyUnchokednterval.getValue();
	}

	public void setOptimisticallyUnchokednterval(int value)
	{
		this.optimisticallyUnchokednterval.setValue(value);
	}

	public ObjectProperty<Integer> optimisticallyUnchokedntervalProperty()
	{
		return this.optimisticallyUnchokednterval;
	}

	public int getUnchokingInterval()
	{
		return this.unchokingInterval.getValue();
	}

	public void setUnchokingIntervalProperty(int value)
	{
		this.unchokingInterval.setValue(value);
	}

	public ObjectProperty<Integer> unchokingIntervalProperty()
	{
		return this.unchokingInterval;
	}

	public int getNumberOfPreferredNeighbors()
	{
		return this.numberOfPreferredNeighbors.getValue();
	}

	public void setNumberOfPreferredNeighbors(int value)
	{
		this.numberOfPreferredNeighbors.setValue(value);
	}

	public ObjectProperty<Integer> numberOfPreferredNeighborsProperty()
	{
		return this.numberOfPreferredNeighbors;
	}

	public Path getSharedFile()
	{
		return this.sharedFile.getValue();
	}

	public void setSharedFile(Path value)
	{
		this.sharedFile.setValue(value);
	}

	public ObjectProperty<Path> sharedFileProperty()
	{
		return this.sharedFile;
	}

	public Path getCommonConfigFile()
	{
		return this.commonConfigFile.getValue();
	}

	public void setCommonConfigFile(Path value)
	{
		this.commonConfigFile.setValue(value);
	}

	public ObjectProperty<Path> commonConfigFileProperty()
	{
		return this.commonConfigFile;
	}

	public Path getPeerConfigFile()
	{
		return this.peerConfigFile.getValue();
	}

	public void setPeerConfigFile(Path value)
	{
		this.peerConfigFile.setValue(value);
	}

	public ObjectProperty<Path> peerConfigFileProperty()
	{
		return this.peerConfigFile;
	}

	public int getStartPeerID()
	{
		return this.startPeerID.getValue();
	}

	public void setStartPeerID(int value)
	{
		this.startPeerID.setValue(value);
	}

	public ObjectProperty<Integer> startPeerIDProperty()
	{
		return this.startPeerID;
	}

	public int getStartPortNumber()
	{
		return this.startPortNumber.getValue();
	}

	public void setStartPortNumber(int value)
	{
		this.startPortNumber.setValue(value);
	}

	public ObjectProperty<Integer> startPortNumberProperty()
	{
		return this.startPortNumber;
	}

	public int getNumberOfPeers()
	{
		return this.numberOfPeers.getValue();
	}

	public void setNumberOfPeers(int value)
	{
		this.numberOfPeers.setValue(value);
	}

	public ObjectProperty<Integer> numberOfPeersProperty()
	{
		return this.numberOfPeers;
	}

	public boolean getUsePeerConfigFile()
	{
		return this.usePeerConfigFile.getValue();
	}

	public void setUsePeerConfigFile(boolean value)
	{
		this.usePeerConfigFile.setValue(value);
	}

	public BooleanProperty usePeerConfigFileProperty()
	{
		return this.usePeerConfigFile;
	}

	public boolean getUseCommonConfigFile()
	{
		return this.useCommonConfigFile.getValue();
	}

	public void setUseCommonConfigFile(boolean value)
	{
		this.useCommonConfigFile.setValue(value);
	}

	public BooleanProperty useCommonConfigFileProperty()
	{
		return this.useCommonConfigFile;
	}

	/**
	 * Returns the size of the shared file in bytes
	 * 
	 * @return The size of the shared file in bytes
	 */
	public long getSharedFileSize()
	{
		return this.commonConfigFile.getValue().toFile().length();
	}

	@Override
	public String toString()
	{
		StringBuilder string = new StringBuilder("usePeerConfigFile: ");
		string.append(this.usePeerConfigFile.getValue());
		string.append("\npeerConfigFile: ");
		string.append(this.peerConfigFile.getValue());
		string.append("\ncommonServer: ");
		string.append(this.commonServer.getValue());
		string.append("\nnumberOfPeers: ");
		string.append(this.numberOfPeers.getValue());
		string.append("\nstartPortNumber: ");
		string.append(this.startPortNumber.getValue());
		string.append("\nstartPeerID: ");
		string.append(this.startPeerID.getValue());

		string.append("\nuseCommonConfigFile: ");
		string.append(this.useCommonConfigFile.getValue());
		string.append("\ncommonConfigFile: ");
		string.append(this.commonConfigFile.getValue());
		string.append("\nsharedFile: ");
		string.append(this.sharedFile.getValue());
		string.append("\nnumberOfPreferredNeighbors: ");
		string.append(this.numberOfPreferredNeighbors.getValue());
		string.append("\nunchokingInterval: ");
		string.append(this.unchokingInterval.getValue());
		string.append("\noptimisticallyUnchokednterval: ");
		string.append(this.optimisticallyUnchokednterval.getValue());
		string.append("\npieceSize: ");
		string.append(this.pieceSize.getValue());

		string.append("\nallNeighbors: ");
		string.append(this.allNeighbors.getValue());
		string.append("\nnumberOfNeighbors: ");
		string.append(this.numberOfNeighbors.getValue());
		string.append("\n\n");
		return string.toString();
	}
}

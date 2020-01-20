package bittorrent;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Contains global configuration parameters for this implementation of
 * BitTorrent
 */
public class BitTorrent
{
	// Path for the peer configuration file
	public static Path PEER_CONFIG_FILE = Paths.get("PeerInfo.cfg");

	// Path for the common configuration file
	public static Path COMMON_CONFIG_FILE = Paths.get("Common.cfg");

	// Number of connection attemps that a peer will make to another peer
	public static int CONNECTION_ATTEMPTS = 30;

	// The number of seconds before a requested piece is timed out.
	public static int REQUEST_TIMEOUT = 3;

	// The default window size of the bit torrent config GUI
	public static final int DEFAULT_WINDOW_WIDTH = 500;
	public static final int DEFAULT_WINDOW_HEIGHT = 300;

	// The default window name of the bit torrent config GUI
	public static final String APPLICATION_NAME = "BitTorrent - COP5106C";

	// The scale to increase the receive and send buffers. This is multipled by the
	// piece count
	public static final int BUFFER_SCALE = 4;
}

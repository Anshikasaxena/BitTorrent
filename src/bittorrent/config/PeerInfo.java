package bittorrent.config;

import java.io.IOException;
import java.util.StringTokenizer;

// @formatter:off
/**
 * Reads a line in the PeerInfo.cfg file
 * 
 * Strings are in the format:
 * 
 * [peer ID] [host name] [listening port] [has file or not]
 *
 * where,
 * 	 - Peer ID is a positive integer number
 *	 - HostName of where the peer is
 *	 - Listening port is the port number which the peer is listening on
 *	 - Has file or not can either be 1 (has complete file) or 0 (does not have 
 *     the file). We do not consider the case where a peer has only some 
 *     pieces. 
 */
// @formatter:on
public class PeerInfo
{
	private static String DELIMITER = " ";

	private int peerID;
	private String hostName;
	private int port;
	private boolean hasFile;

	public PeerInfo(int peerID, String hostName, int port, boolean hasFile)
	{
		this.peerID = peerID;
		this.hostName = hostName;
		this.port = port;
		this.hasFile = hasFile;
	}
	
	// @formatter:off
	/**
	 * Reads a single peer info line.
	 * 
	 * The format of the line is provided in the project description.
	 * Specifically: 
	 * 
	 * [peer ID] [host name] [listening port] [has file or not]
	 *
	 * where,
	 * 	 - Peer ID is a positive integer number
	 *	 - HostName of where the peer is
	 *	 - Listening port is the port number which the peer is listening on
	 *	 - Has file or not can either be 1 (has complete file) or 0 (does not have 
	 *     the file). We do not consider the case where a peer has only some 
	 *     pieces. 
	 *     
	 * @param peerInfo The string following the peer info format 
	 * @throws IOException If the line does not have enough tokens
	 * @throws NumberFormatException If the peer ID or listening port is not a number
	 */
	// @formatter:on
	public static PeerInfo createPeerInfo(String peerInfoLine) throws IOException, NumberFormatException
	{
		StringTokenizer peerInfoTokenizer = new StringTokenizer(peerInfoLine, DELIMITER);
		if (peerInfoTokenizer.countTokens() != 4)
		{
			String errorMessage = "Error: peer info is formatted incorrectly";
			throw new IOException(errorMessage);
		}
		String peerIDStr = peerInfoTokenizer.nextToken();
		int peerID = Integer.parseInt(peerIDStr);

		String hostName = peerInfoTokenizer.nextToken();

		String portStr = peerInfoTokenizer.nextToken();
		int port = Integer.parseInt(portStr);

		String hasFileStr = peerInfoTokenizer.nextToken();
		boolean hasFile = Integer.parseInt(hasFileStr) == 1 ? true : false;
		
		PeerInfo peerInfo = new PeerInfo(peerID, hostName, port, hasFile);
		return peerInfo;
	}

	/**
	 * Returns the peer's ID
	 * 
	 * @return The peer's ID
	 */
	public int getPeerID()
	{
		return this.peerID;
	}

	/**
	 * Returns the peer's hostname
	 * 
	 * @return The peer's hostname
	 */
	public String getHostName()
	{
		return this.hostName;
	}

	/**
	 * Returns the peer's port number
	 * 
	 * @return The peer's port
	 */
	public int getPort()
	{
		return this.port;
	}

	/**
	 * Return true if the peer has the complete file
	 * 
	 * @return True if the peer has the complete file.
	 */
	public boolean getHasFile()
	{
		return this.hasFile;
	}
}

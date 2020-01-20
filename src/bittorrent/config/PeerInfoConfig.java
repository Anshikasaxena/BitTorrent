package bittorrent.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import bittorrent.app.BitTorrentSettings;

/**
 * Reads the PeerInfo.cfg file.
 * 
 * Each peer in the PeerInfo.cfg file can be obtained using getPeerInfo.
 */
public class PeerInfoConfig
{
	private List<PeerInfo> peers;

	public static PeerInfoConfig setPeerInfo(BitTorrentSettings settings)
	{
		PeerInfoConfig config = new PeerInfoConfig();

//		PeerInfo peerInfo = PeerInfo.createPeerInfo(line);
//		config.peers.add(peerInfo);
//		lineNumber++;
		return config;
	}
	
	/**
	 * Reads the provided PeerInfo.cfg file
	 * 
	 * Each peer in the file is added to the list of peers
	 * 
	 * @param peerInfoFile The path to the PeerInfo.cfg file
	 * @throws IOException If there is an error reading the PeerInfo.cfg file
	 */
	public static PeerInfoConfig ReadPeerInfoConfig(Path peerInfoPath) throws IOException
	{
		if (Files.notExists(peerInfoPath))
		{
			StringBuilder errorMessage = new StringBuilder("Error: ");
			errorMessage.append(peerInfoPath.toString());
			errorMessage.append(" does not exist.");
			throw new IOException(errorMessage.toString());
		}
		PeerInfoConfig config = new PeerInfoConfig();
		config.peers = new ArrayList<PeerInfo>();

		String line = "";
		int lineNumber = 0;
		try (BufferedReader reader = Files.newBufferedReader(peerInfoPath))
		{
			while ((line = reader.readLine()) != null)
			{
				PeerInfo peerInfo = PeerInfo.createPeerInfo(line);
				config.peers.add(peerInfo);
				lineNumber++;
			}
		}
		catch (Exception e)
		{
			StringBuilder errorMessage = new StringBuilder("Error: Unable to read peer info on line ");
			errorMessage.append(lineNumber);
			errorMessage.append(". ");
			errorMessage.append(e.getMessage());
			throw new IOException(errorMessage.toString());
		}
		return config;
	}

	/**
	 * Returns the list of peers
	 * 
	 * @return The list of peers
	 */
	public List<PeerInfo> getPeers()
	{
		return this.peers;
	}

	/**
	 * Returns a peer at the provided index.
	 * 
	 * The peer index in the list corresponds to the peer in the file.
	 * 
	 * @param index Index of the peer
	 * @return The peer info at the provided index
	 */
	public PeerInfo getPeerInfo(int index)
	{
		return this.peers.get(index);
	}

	/**
	 * Returns the peer info given the peer ID
	 * 
	 * The function returns null if it is not found
	 * 
	 * @param id The ID of the peer
	 * @return The peer with the corresponding peer ID or null if not found
	 */
	public PeerInfo getPeerFromId(int id)
	{
		return this.peers.stream().filter((peerinfo) -> peerinfo.getPeerID() == id).findFirst().orElse(null);
	}
}

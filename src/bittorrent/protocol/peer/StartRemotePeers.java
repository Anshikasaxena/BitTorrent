package bittorrent.protocol.peer;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import bittorrent.BitTorrent;
import bittorrent.config.PeerInfo;
import bittorrent.config.PeerInfoConfig;

/*
 * This is the program starting remote processes. This program was only tested
 * on CISE linux Environment.
 * 
 * The StartRemotePeers class begins remote peer processes. It reads
 * configuration file PeerInfo.cfg and starts remote peer processes.
 */
public class StartRemotePeers
{
	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		try
		{
			String username, passwd;
			// Take username and password from the user
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			System.out.println("Give your  Username ");
			username = br.readLine();
			System.out.println("Give your Password");
			passwd = br.readLine();

			PeerInfoConfig peerConfig = PeerInfoConfig.ReadPeerInfoConfig(BitTorrent.PEER_CONFIG_FILE);

			// get current path
			String path = System.getProperty("user.dir");

			// start clients at remote hosts
			for (PeerInfo peerInfo : peerConfig.getPeers())
			{
				System.out.println("Start remote peer " + peerInfo.getPeerID() + " at " + peerInfo.getHostName());

				// Execute on remote machine to start remote process
				Runtime.getRuntime().exec("ssh " + peerInfo.getHostName() + "/K" + username + "/K " + passwd + " cd "
						+ path + "; java peerProcess " + peerInfo.getPeerID());

			}
			System.out.println("Starting all remote peers has done.");

		}
		catch (Exception ex)
		{
			System.out.println(ex);
		}
	}
}

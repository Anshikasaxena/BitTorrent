package bittorrent.protocol.peer.connection;

import java.util.Timer;
import java.util.TimerTask;

import bittorrent.logging.Logger;
import bittorrent.protocol.messages.ActualMessage;
import bittorrent.protocol.peer.PeerProcess;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ChokingProcess extends TimerTask
{
	private PeerProcess parent;
	private Logger logger;

	private long periodMs;
	private List<PeerConnection> connections;
	private int maxPreferredNeighbors;
	private Random rn;
	private Timer timerProcess;
	private Boolean hasAllPieces;

	public ChokingProcess(PeerProcess parent, Logger logger, List<PeerConnection> connections, int maxPreferredNeighbors, long periodMs,
			boolean allPieces)
	{
		this.parent = parent;
		this.logger = logger;
		this.periodMs = periodMs;
		this.connections = connections;
		this.timerProcess = new Timer(true);
		this.rn = new Random();
		this.maxPreferredNeighbors = maxPreferredNeighbors;
		this.hasAllPieces = allPieces;
	}

	public void startProcess()
	{
		this.timerProcess.scheduleAtFixedRate(this, 0, this.periodMs);
	}

	public void stopProcess()
	{
		this.timerProcess.cancel();
		this.timerProcess.purge();
	}

	public Timer getTimer()
	{
		return this.timerProcess;
	}

	@Override
	public void run()
	{
		this.calculatePreferredNeighbors();
	}

	protected void calculatePreferredNeighbors()
	{
		for (PeerConnection connection : this.connections)
		{
			connection.setPreferred(false);
			connection.setOptimisticallyUnchoked(false);
		}
		List<PeerConnection> interestedNeighbors = new ArrayList<PeerConnection>();
		for (PeerConnection connection : this.connections)
		{
			if (connection.getInterested())
			{
				interestedNeighbors.add(connection);
			}
		}
		List<PeerConnection> preferredNeighbors = new ArrayList<PeerConnection>();
		int numOfInterested = interestedNeighbors.size();

		// sets preferred connections to true for MaxPreferredNeighbors
		// if MaxPreferred connections > # of connections, sets all to preferred.
		if (this.maxPreferredNeighbors < numOfInterested)
		{
			for (int i = 0; i < this.maxPreferredNeighbors; i++)
			{
				int num;

				// good for complete file
				if (this.hasAllPieces)
				{
					do
					{
						num = rn.nextInt(numOfInterested);
					} while (interestedNeighbors.get(num).getPreferred());
					interestedNeighbors.get(num).setPreferred(true);
					preferredNeighbors.add(interestedNeighbors.get(num));
				}
				// Pick highest downloading speed
				else
				{
					int max = 0;
					num = 0;
					for (int j = 0; j < this.maxPreferredNeighbors - i; j++)
					{
						if (interestedNeighbors.get(j).getDownloads() > max)
						{
							num = j;
							max = interestedNeighbors.get(j).getDownloads();
						}
						else if (interestedNeighbors.get(j).getDownloads() == max)
						{
							// if tied, randomly choose between the 2 if the previous is chosen, we do
							// nothing, so 50% chance this if is called.
							if (rn.nextBoolean())
							{
								num = j;
								max = interestedNeighbors.get(j).getDownloads();
							}
						}
					}
					interestedNeighbors.get(num).setPreferred(true);
					preferredNeighbors.add(interestedNeighbors.get(num));
					interestedNeighbors.remove(num);
				}
			}
		}
		else
		{
			for (int i = 0; i < numOfInterested; i++)
			{
				PeerConnection connection = interestedNeighbors.get(i);
				connection.setPreferred(true);
				preferredNeighbors.add(connection);
			}
		}
		// resets download speeds for next iteration
		for (int i = 0; i < this.connections.size(); i++)
		{
			this.connections.get(i).resetDownloadRate();
		}
		this.parent.clearPendingRequests();
		this.sendChokeUnchoke();

		// Logs new preferred Neighbors
		if (preferredNeighbors.size() > 0)
		{
			this.logger.changePreferredNeighbors(preferredNeighbors);
		}
	}

	protected void sendChokeUnchoke()
	{
		for (PeerConnection connection : this.connections)
		{
			// sets chokes and unchokes and sends messages
			// if choked and preferred send unchoke
			if (connection.getChoked() && connection.getPreferred())
			{
				ActualMessage message = ActualMessage.CreateUnchokeMessage();
				connection.sendMessage(message);
				connection.setChoked(false);
			}
			// if unchoked and not preffered and not optimistically preferred
			else if (!connection.getChoked() && !connection.getPreferred() && !connection.getOptimisticallyUnchoked())
			{
				ActualMessage message = ActualMessage.CreateChokeMessage();
				connection.sendMessage(message);
				connection.setChoked(true);
			}

		}
	}

	public void updateHasAllPieces(boolean allPieces)
	{
		this.hasAllPieces = allPieces;
	}
}

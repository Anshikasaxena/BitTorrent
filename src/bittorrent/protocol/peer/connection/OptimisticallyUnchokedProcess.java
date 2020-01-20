package bittorrent.protocol.peer.connection;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import bittorrent.logging.Logger;
import bittorrent.protocol.messages.ActualMessage;

public class OptimisticallyUnchokedProcess extends TimerTask
{
	private Logger logger;
	private long periodMs;
	private List<PeerConnection> connections;
	private Random rn;
	private Timer timerProcess;

	public OptimisticallyUnchokedProcess(Logger logger, List<PeerConnection> connections, long periodMs)
	{
		this.logger = logger;
		this.periodMs = periodMs;
		this.connections = connections;
		this.timerProcess = new Timer(true);
		this.rn = new Random();
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
		int numOfConnections = this.connections.size();
		List<PeerConnection> ChokedButInterested = new ArrayList<PeerConnection>();
		for (int i = 0; i < numOfConnections; i++)
		{
			if (!this.connections.get(i).getPreferred() && this.connections.get(i).getInterested())
			{
				ChokedButInterested.add(this.connections.get(i));
			}
		}
		PeerConnection peer;
		if (!ChokedButInterested.isEmpty())
		{
			peer = ChokedButInterested.get(rn.nextInt(ChokedButInterested.size()));
			peer.setOptimisticallyUnchoked(true);
			ActualMessage message = ActualMessage.CreateUnchokeMessage();
			peer.sendMessage(message);
			peer.setChoked(false);
			this.logger.changeOptimisticallyUnchoked(peer);
		}
	}
}

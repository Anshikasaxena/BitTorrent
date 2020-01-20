package bittorrent.protocol.peer.connection;

import java.util.Timer;
import java.util.TimerTask;

import bittorrent.protocol.messages.ActualMessage;

/**
 * A threaded process that continually requests pieces from the other
 * connection.
 */
public class RequestPieceProcess extends TimerTask
{
	private long periodMs;
	private Timer timerProcess;
	private int pieceIndex;
	private PeerConnection connection;

	/**
	 * Initializes the request piece process
	 * 
	 * @param connection The connection to request pieces from
	 * @param pieceIndex The index of the piece to request
	 * @param periodMs The rate in milliseconds to request the piece
	 */
	public RequestPieceProcess(PeerConnection connection, int pieceIndex, long periodMs)
	{
		this.pieceIndex = pieceIndex;
		this.connection = connection;
		this.periodMs = periodMs;
		this.timerProcess = new Timer(true);
	}

	/**
	 * Starts the request process
	 */
	public void startProcess()
	{
		this.timerProcess.scheduleAtFixedRate(this, 0, this.periodMs);
	}

	/**
	 * Stops the request process
	 */
	public void stopProcess()
	{
		this.timerProcess.cancel();
		this.timerProcess.purge();
	}

	/**
	 * Set the index of the piece to request
	 * 
	 * @param pieceIndex The index of the piece to request
	 */
	public void setPieceIndex(int pieceIndex)
	{
		this.pieceIndex = pieceIndex;
	}

	/**
	 * Returns the index of the piece being requested
	 * 
	 * @return The index of the piece being requested
	 */
	public int getPieceIndex()
	{
		return this.pieceIndex;
	}

	@Override
	public void run()
	{
		if (this.pieceIndex == -1)
		{
			this.pieceIndex = this.connection.getParent().determinePieceRequest(this.connection);
		}
		if (this.pieceIndex != -1)
		{
			ActualMessage requestMessage = ActualMessage.CreateRequestMessage(this.pieceIndex);
			this.connection.sendMessage(requestMessage);
			this.connection.getProcess().addPendingRequest(this.pieceIndex);
		}
	}
}

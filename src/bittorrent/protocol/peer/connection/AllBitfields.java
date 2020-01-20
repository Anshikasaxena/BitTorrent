package bittorrent.protocol.peer.connection;

import java.util.HashMap;

/**
 * Keeps Record of the bitfields of all the neighbors of a process
 * 
 * Has functions to add a bitfield, update a bitfield, and get a bitfield
 */
public class AllBitfields
{
	private HashMap<PeerConnection, Bitfield> bitfields;

	public AllBitfields()
	{
		this.bitfields = new HashMap<PeerConnection, Bitfield>();
	}

	/**
	 * Adds the Bitfield of a peer to the Map
	 */
	public synchronized void addBitfield(PeerConnection connection, Bitfield bitfield)
	{
		this.bitfields.put(connection, bitfield);
	}

	/**
	 * Adds the Bitfield of a peer to the Map
	 */
	public synchronized void addBitfield(PeerConnection connection, byte[] bitfield)
	{
		Bitfield newBitfield = new Bitfield(connection.getProcess(), bitfield);
		this.bitfields.put(connection, newBitfield);
	}

	/**
	 * Upadtes the Bitfield of a peer after receiving Have Message
	 * 
	 * @param peerId The peerId of the peer whose bitfield needs to be updated
	 * @param index The index of the piece that needs to be updated
	 */
	public synchronized void updateBitfield(PeerConnection connection, int index)
	{
		Bitfield bitfield = this.bitfields.get(connection);
		bitfield.updateBitfield(index);
	}

	/**
	 * Returns all bitfields belonging to each of the registered peer connections
	 * 
	 * @return All bitfields belonging to each of the registered peer connections
	 */
	public HashMap<PeerConnection, Bitfield> getBitfields()
	{
		return this.bitfields;
	}

	/**
	 * Get the bitfield of a peer
	 * 
	 * @param connection The peer connection of the peer whose Bitfield is required
	 */
	public Bitfield getBitfield(PeerConnection connection)
	{
		return this.bitfields.get(connection);
	}
}

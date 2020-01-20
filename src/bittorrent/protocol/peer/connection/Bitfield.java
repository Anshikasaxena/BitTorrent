package bittorrent.protocol.peer.connection;

import java.util.ArrayList;
import java.util.List;

import bittorrent.protocol.peer.PeerProcess;

/**
 * Creates a bitfield indicating what pieces the peer has.
 * 
 * Each bit of the bitfield indicates whether the peer has the corresponding
 * piece. Specifically, the first byte of the bitfield corresponds to piece
 * indices 0 - 7 from the high to low bit. The next byte correpoonds to pieces
 * indices 8 - 15, and so forth.
 * 
 * Additional methods for processing modifications to the bitfield are provided
 */
public class Bitfield
{
	private byte[] bitfield;
	private PeerProcess process;

	/**
	 * Creates a bitfield corresponding to the pieces information
	 * 
	 * @param process The process to create a bitfield from
	 */
	public Bitfield(PeerProcess process)
	{
		this.process = process;

		int numBytes = process.getCommonConfig().getBitfieldPieceCount();
		this.bitfield = new byte[numBytes];
		int totalpieces = process.getCommonConfig().getNumberOfPieces();
		for (int index = 0; index < totalpieces; index++)
		{
			if (process.getPieces().containsKey(index))
			{
				// set that bit location one
				int posByte = index / 8;
				int posBit = index % 8;
				byte oldByte = bitfield[posByte];
				oldByte = (byte) (((0xFF7F >> posBit) & oldByte) & 0x00FF);
				byte newByte = (byte) ((1 << (8 - (posBit + 1))) | oldByte);
				this.bitfield[posByte] = newByte;
			}
		}
	}

	/**
	 * Creates a bitfield from the corresponding process and bitfield
	 * 
	 * @param process The process to create a bitfield from
	 */
	public Bitfield(PeerProcess process, byte[] bitfield)
	{
		this.process = process;
		this.bitfield = bitfield;
	}

	/**
	 * Determines if the peer has all of the pieces
	 * 
	 * @return True if the peer has all of the pieces
	 */
	public boolean hasAllPieces()
	{
		boolean hasAllPieces = true;
		int totalPieceCount = this.process.getPieceCount();

		int currentPieceIndex = 0;
		for (byte b : this.bitfield)
		{
			boolean[] boolArray = new boolean[8];
			boolArray[7] = ((b & 0x01) != 0);
			boolArray[6] = ((b & 0x02) != 0);
			boolArray[5] = ((b & 0x04) != 0);
			boolArray[4] = ((b & 0x08) != 0);
			boolArray[3] = ((b & 0x10) != 0);
			boolArray[2] = ((b & 0x20) != 0);
			boolArray[1] = ((b & 0x40) != 0);
			boolArray[0] = ((b & 0x80) != 0);
			for (boolean isRequired : boolArray)
			{
				if (currentPieceIndex >= totalPieceCount)
				{
					break;
				}
				if (!isRequired)
				{
					hasAllPieces = false;
					break;
				}
				currentPieceIndex++;
			}
			if (!hasAllPieces)
			{
				break;
			}
		}
		return hasAllPieces;
	}

	/**
	 * Gets a list of indices that the peerBitfield has, but this does not
	 */
	public List<Integer> getRequiredPieces(Bitfield peerBitfield)
	{
		List<Integer> requiredIndices = new ArrayList<Integer>();
		int byteCount = this.bitfield.length;

		int pieceIndex = 0;
		for (int index = 0; index < byteCount; ++index)
		{
			byte myByte = this.bitfield[index];
			byte peerByte = peerBitfield.getBitfield()[index];

			byte differentPieces = (byte) (myByte ^ peerByte);
			byte requiredPieces = (byte) (differentPieces & peerByte);

			boolean[] boolArray = new boolean[8];
			boolArray[7] = ((requiredPieces & 0x01) != 0);
			boolArray[6] = ((requiredPieces & 0x02) != 0);
			boolArray[5] = ((requiredPieces & 0x04) != 0);
			boolArray[4] = ((requiredPieces & 0x08) != 0);
			boolArray[3] = ((requiredPieces & 0x10) != 0);
			boolArray[2] = ((requiredPieces & 0x20) != 0);
			boolArray[1] = ((requiredPieces & 0x40) != 0);
			boolArray[0] = ((requiredPieces & 0x80) != 0);
			for (boolean isRequired : boolArray)
			{
				if (isRequired)
				{
					requiredIndices.add(pieceIndex);
				}
				pieceIndex++;
			}
		}
		return requiredIndices;
	}

	/**
	 * Updates the piece index
	 * 
	 * @param index The index of the bit
	 */
	public void updateBitfield(int index)
	{
		// Updating the bit representing the piece
		int posByte = index / 8;
		int posBit = index % 8;
		byte oldByte = bitfield[posByte];
		oldByte = (byte) (((0xFF7F >> posBit) & oldByte) & 0x00FF);
		byte newByte = (byte) ((1 << (8 - (posBit + 1))) | oldByte);
		bitfield[posByte] = newByte;
	}

	/*
	 * Checks if the bitfield of the connected peer has any pieces that the peer
	 * doesn't have
	 * 
	 * @param bitfield The bitfield that contains pieces of the connected peer
	 */
	public boolean isInterested(Bitfield compareBitfield)
	{
		// TODO received a null pointer here
		return this.isInterested(compareBitfield.bitfield);
	}

	/*
	 * Checks if the bitfield of the connected peer has any pieces that the peer
	 * doesn't have
	 * 
	 * @param bitfield The bitfield that contains pieces of the connected peer
	 */
	public boolean isInterested(byte[] compareBitfield)
	{
		for (int i = 0; i < compareBitfield.length; i++)
		{
			byte myByte = this.bitfield[i];
			byte peerbyte = compareBitfield[i];
			byte hasPieces = (byte) (myByte | peerbyte);
			if (hasPieces != myByte)
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns the bitfield array
	 * 
	 * The bitfield array indicates that pieces this peer has using a byte array.
	 * Each bit represents a bit index.
	 * 
	 * @return the bitfield array
	 */
	public byte[] getBitfield()
	{
		return this.bitfield;
	}
}

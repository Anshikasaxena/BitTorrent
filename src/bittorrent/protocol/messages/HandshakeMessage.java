package bittorrent.protocol.messages;

import java.nio.ByteBuffer;

import bittorrent.utility.ByteUtility;

/**
 * The handshake message, as described in the project description
 */
public class HandshakeMessage
{
	public static byte[] HEADER = "P2PFILESHARINGPROJ".getBytes();
	public static byte[] ZEROBITS = new byte[10];

	private byte[] payload;

	private HandshakeMessage(int peerID)
	{
		// Generate payload given peerID
		byte[] peerIDByte = ByteBuffer.allocate(4).putInt(peerID).array();
		this.payload = ByteUtility.concatenateByteArrays(HEADER, ZEROBITS, peerIDByte);
	}

	/**
	 * Returns the payload of the message.
	 * 
	 * @return The payload of the message
	 */
	public byte[] getPayload()
	{
		return payload;
	}

	/**
	 * Creates a handshake message with the provided peer ID
	 * 
	 * @param peerID The peer ID to add to the handshake message
	 * @return A handshake message
	 */
	public static HandshakeMessage createHandshakeMessage(int peerID)
	{
		HandshakeMessage message = new HandshakeMessage(peerID);
		return message;
	}
}
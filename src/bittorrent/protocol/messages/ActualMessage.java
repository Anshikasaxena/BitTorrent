package bittorrent.protocol.messages;

import java.io.IOException;
import java.nio.ByteBuffer;

import bittorrent.utility.ByteUtility;

//@formatter:off
/**
 * This class provides methods for creating and handling "actual messages" 
 * 
 * There are several types of actual messages:
 * 	- Choke
 * 	- Unchoke
 * 	- Interested
 * 	- Not Interested
 * 	- Have
 * 	- Bitfield
 * 	- Request
 * 	- Piece
 * Each of the actual message types have the value type indicated in the 
 * project description. In addition to creating messages, this class provides
 * methods for deserializing and serializing messages to byte arrays. 
 */
//@formatter:on
public class ActualMessage
{
	private MessageType type;
	private ByteBuffer payload;

	private ActualMessage(MessageType type)
	{
		this.type = type;
		this.payload = ByteBuffer.allocate(0);
	}

	private ActualMessage(MessageType type, byte[] payload)
	{
		this.type = type;
		this.payload = ByteBuffer.wrap(payload);
	}

	/**
	 * Creates a choke message.
	 * 
	 * Choke messages contain no payload.
	 * 
	 * @return A choke message
	 */
	public static ActualMessage CreateChokeMessage()
	{
		MessageType type = MessageType.CHOKE;
		ActualMessage message = new ActualMessage(type);
		return message;
	}

	/**
	 * Creates an unchoke message.
	 * 
	 * Unchoke messages contain no payload.
	 * 
	 * @return An unchoke message
	 */
	public static ActualMessage CreateUnchokeMessage()
	{
		MessageType type = MessageType.UNCHOKE;
		ActualMessage message = new ActualMessage(type);
		return message;
	}

	/**
	 * Creates an interested message.
	 * 
	 * Interested messages contain no payload.
	 * 
	 * @return An interested message
	 */
	public static ActualMessage CreateInterestedMessage()
	{
		MessageType type = MessageType.INTERESTED;
		ActualMessage message = new ActualMessage(type);
		return message;
	}

	/**
	 * Creates a not interested message.
	 * 
	 * Not interested messages contain no payload.
	 * 
	 * @return A not interested message
	 */
	public static ActualMessage CreateNotInterestedMessage()
	{
		MessageType type = MessageType.NOT_INTERESTED;
		ActualMessage message = new ActualMessage(type);
		return message;
	}

	/**
	 * Creates a have message.
	 * 
	 * Have messages contain a 4-byte piece index field as its payload.
	 * 
	 * @return A have message
	 */
	public static ActualMessage CreateHaveMessage(int index)
	{
		MessageType type = MessageType.HAVE;
		byte[] payload = ByteUtility.intToByteArray(index);
		ActualMessage message = new ActualMessage(type, payload);
		return message;
	}

	/**
	 * Creates a bitfield message.
	 * 
	 * Bitfield messages contain the bitfield values as its payload
	 * 
	 * @return A bitfield message
	 */
	public static ActualMessage CreateBitfieldMessage(byte[] bitfield)
	{
		MessageType type = MessageType.BITFIELD;
		ActualMessage message = new ActualMessage(type, bitfield);
		return message;
	}

	/**
	 * Creates a request message.
	 * 
	 * Request messages contain a 4-byte piece index field as its payload.
	 * 
	 * @return A request message
	 */
	public static ActualMessage CreateRequestMessage(int index)
	{
		MessageType type = MessageType.REQUEST;
		byte[] payload = ByteUtility.intToByteArray(index);
		ActualMessage message = new ActualMessage(type, payload);
		return message;
	}

	/**
	 * Creates a piece message.
	 * 
	 * Piece messages contain a 4-byte piece index field and a piece of the file as
	 * its payload.
	 * 
	 * @return A piece message
	 */
	public static ActualMessage CreatePieceMessage(int index, byte[] content)
	{
		MessageType type = MessageType.PIECE;
		byte[] indexPayload = ByteUtility.intToByteArray(index);
		byte[] payload = ByteUtility.concatenateByteArrays(indexPayload, content);
		ActualMessage message = new ActualMessage(type, payload);
		return message;
	}

	/**
	 * Converts the byte buffer to an actual message
	 * 
	 * The actual message contains the type, length, and payload.
	 * 
	 * @param data The byte buffer to deserializer
	 * @return The actual message
	 * @throws IOException If there was an error deserializing the data.
	 */
	public static ActualMessage deserialize(ByteBuffer data) throws IOException
	{
		int length = data.getInt();
		MessageType type = MessageType.parse(data.get());
		byte[] payload = new byte[length - 1];

		data.get(payload, 0, payload.length);
		ActualMessage message = new ActualMessage(type, payload);
		return message;
	}

	/**
	 * Converts the byte array to an actual message
	 * 
	 * The actual message contains the type, length, and payload.
	 * 
	 * @param data The byte array to deserializer
	 * @return The actual message
	 * @throws IOException If there was an error deserializing the data.
	 */
	public static ActualMessage deserialize(byte[] data) throws IOException
	{
		return deserialize(ByteBuffer.wrap(data));
	}

	/**
	 * Serializes the message into a byte array
	 * 
	 * @return The message as a byte array.
	 */
	public byte[] serialize()
	{
		byte[] length = ByteUtility.intToByteArray(getLength());
		byte[] typeData = new byte[]
		{
				this.type.getValue()
		};
		return ByteUtility.concatenateByteArrays(length, typeData, this.payload.array());
	}

	/**
	 * Returns this message's type
	 * 
	 * @return This message's type
	 */
	public MessageType getType()
	{
		return this.type;
	}

	/**
	 * Get the length of the message.
	 * 
	 * The length excludes the message length field itself. The messagge type and
	 * message payload are included in the size calculation
	 * 
	 * @return The length of the message
	 */
	public int getLength()
	{
		return payload.limit() + 1;
	}

	/**
	 * Returns the payload of the message
	 * 
	 * The payload excludes the type and length
	 * 
	 * @return the payload of the message
	 */
	public ByteBuffer getPayload()
	{
		return this.payload;
	}
}

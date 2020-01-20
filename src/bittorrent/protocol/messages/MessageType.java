package bittorrent.protocol.messages;

import java.io.IOException;
import java.nio.BufferUnderflowException;

/**
 * The interface should have a method to reconstruct the class from a byte
 * array. The class will have get methods to get the message components. The
 * interface class will have a static method to initialize an instance of the
 * class via the enum. The interface will have a static method to construct an
 * instance of the class from a byte array. The byte array will contain the
 * payload.
 */
public enum MessageType
{
	// @formatter:off
	CHOKE((byte)0), 
	UNCHOKE((byte)1), 
	INTERESTED((byte)2), 
	NOT_INTERESTED((byte)3), 
	HAVE((byte)4), 
	BITFIELD((byte)5), 
	REQUEST((byte)6), 
	PIECE((byte)7),
	UNKNOWN((byte)8);
	// @formatter:on

	private byte value;

	MessageType(byte value)
	{
		this.value = value;
	}

	/**
	 * Gets the message type corresponding to the byte value.
	 * 
	 * @param value The byte value corresponding to the message type
	 * @return The message type corresponding to the byte value
	 * @throws IOException
	 */
	public static MessageType parse(byte value) throws IOException
	{
		// Check if it is an "actual" message
		MessageType configOption = MessageType.UNKNOWN;
		try
		{
			for (MessageType option : MessageType.values())
			{
				if (option.getValue() == value)
				{
					configOption = option;
					break;
				}
			}
		}
		catch (BufferUnderflowException e)
		{
			throw new IOException("Error: Unable to parse message");
		}
		return configOption;
	}

	/**
	 * Returns the value of the message type
	 * 
	 * Values are specified in the project description
	 * 
	 * @return Message type value
	 */
	public byte getValue()
	{
		return value;
	}
}

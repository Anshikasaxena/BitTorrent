package bittorrent.utility;

/**
 * Provides utility functions to handle byte arrays
 */
public class ByteUtility
{
	/**
	 * Converts the integer to a byte array.
	 * 
	 * The integer will be in big endian form
	 * 
	 * @param integer The integer to convert to a byte array
	 * @return The integer as a byte array
	 */
	// @formatter:off
	public static byte[] intToByteArray(int integer)
	{
		return new byte[]
		{
				(byte) (integer >>> 24), 
				(byte) (integer >>> 16), 
				(byte) (integer >>> 8), 
				(byte) (integer),
		};
	}
	// @formatter:on

	/**
	 * Concatenates an arbitrary number of byte arrays.
	 * 
	 * @param arrays The byte arrays to concatenate
	 * @return The concatenated byte array
	 */
	public static byte[] concatenateByteArrays(byte[]... arrays)
	{
		int totalLength = 0;
		for (byte[] array : arrays)
		{
			totalLength += array.length;
		}
		
		byte[] concatenated = new byte[totalLength];
		int position = 0;
		for (byte[] array : arrays)
		{
			System.arraycopy(array, 0, concatenated, position, array.length);
			position += array.length;
		}
		return concatenated;
	}
}

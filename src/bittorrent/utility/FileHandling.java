package bittorrent.utility;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;

import bittorrent.config.CommonConfig;

/**
 * Provides functions for splitting and joining pieces of a file
 */
public class FileHandling
{
	/**
	 * Splits a file into pieces of byte arrays
	 * 
	 * @param config The common config file specifying the file, piece size, and
	 * total file size
	 * @return Map of the pieces with index keys
	 * @throws IOException IF the file does not exist, or there was an error
	 * splitting the file
	 */
	public static HashMap<Integer, byte[]> splitFile(CommonConfig config) throws IOException
	{
		HashMap<Integer, byte[]> pieces = new HashMap<Integer, byte[]>();

		Path file = config.getPath();
		if (Files.notExists(file))
		{
			throw new IOException("Error: file does not exist.");
		}

		int numOfPieces = config.getNumberOfPieces();
		int maxPieceSize = config.getPieceSize();
		int lastPieceSize = config.getLastPieceSize();

		try (FileInputStream fileInputStream = new FileInputStream(file.toFile()))
		{
			for (int index = 0; index < numOfPieces; index++)
			{
				if (index == numOfPieces - 1)
				{
					byte[] readBuffer = new byte[lastPieceSize];
					fileInputStream.read(readBuffer, 0, lastPieceSize);
					pieces.put(index, readBuffer);
				}
				else
				{
					byte[] readBuffer = new byte[maxPieceSize];
					fileInputStream.read(readBuffer, 0, maxPieceSize);
					pieces.put(index, readBuffer);
				}
			}
		}
		catch (IOException e)
		{
			StringBuilder message = new StringBuilder("Error splitting file. ");
			message.append(e.getMessage());
			throw new IOException(message.toString());
		}
		return pieces;
	}

	/**
	 * Merges the byte pieces into a single file
	 * 
	 * The file is overwritten at the end of this call
	 * 
	 * @param file The file to write
	 * @param pieces The pieces to write to the file
	 * @return
	 */
	public static File mergeFile(Path file, HashMap<Integer, byte[]> pieces)
	{
		File outFile = file.toFile();
		try (FileOutputStream fl = new FileOutputStream(outFile))
		{
			for (byte[] piece : pieces.values())
			{
				fl.write(piece);
				fl.flush();
			}
		}
		catch (IOException e)
		{

		}
		return outFile;
	}
}

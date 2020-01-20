package bittorrent.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.StringTokenizer;

import bittorrent.app.BitTorrentSettings;

// @formatter:off
/**
 * Reads the Common.cfg file. 
 * 
 * The common.cfg file should contain parameters for:
 * - NumberOfPreferredNeighbors: the number of preferred neighbors
 * - UnchokingInterval: the time in seconds between calculating a new unchoking
 * 	 neighbor  
 * - OptimisticUnchokingInterval: the time in seconds between calculating a 
 * 	 new optimistic unchoking neighbor
 * - FileName is the file in which all peers are interested
 * - FileSize specified the size of the file in bytes
 * - PieceSize specifies the size of a piece in bytes
 */
// @formatter:on
public class CommonConfig
{
	private static String DELIMITER = " ";

	private int numberOfPreferredNeightbors;
	private Duration unchokingInterval;
	private Duration optimisticUnchokingInterval;
	private String fileName;
	private int fileSize;
	private int pieceSize;

	private enum ConfigOption
	{
		// @formatter:off
		PREFERRED_NEIGHBORS("NumberOfPreferredNeighbors"), 
		UNCHOKING_INTERVAL("UnchokingInterval"), 
		OPTIMISTIC_UNCHOKING_INTERVAL("OptimisticUnchokingInterval"), 
		FILE_NAME("FileName"), 
		FILE_SIZE("FileSize"), 
		PIECE_SIZE("PieceSize"),
		UNKNOWN("");
		// @formatter:on

		private String format;

		ConfigOption(String format)
		{
			this.format = format;
		}

		/**
		 * Parses the common configuration word
		 * 
		 * @param word The common configuration word to parse
		 * @return The common config operation
		 */
		public static ConfigOption parse(String word)
		{
			ConfigOption configOption = ConfigOption.UNKNOWN;
			for (ConfigOption option : ConfigOption.values())
			{
				if (option.getFormat().equals(word))
				{
					configOption = option;
					break;
				}
			}
			return configOption;
		}

		/**
		 * Returns the format of the option as shown in the common config file
		 * 
		 * @return The format of the option as shown in the common config file
		 */
		public String getFormat()
		{
			return this.format;
		}

	}

	public static CommonConfig setCommonConfig(BitTorrentSettings settings)
	{
		CommonConfig config = new CommonConfig();
		config.numberOfPreferredNeightbors = settings.getNumberOfPreferredNeighbors();
		config.unchokingInterval =  Duration.ofSeconds(settings.getUnchokingInterval());
		config.optimisticUnchokingInterval =  Duration.ofSeconds(settings.getOptimisticallyUnchokednterval());
		config.fileName = settings.getSharedFile().toString();
		config.fileSize = (int) settings.getSharedFileSize();
		config.pieceSize = settings.getPieceSize();
		return config;
	}
	
	/**
	 * Reads the provided Common.cfg file
	 * 
	 * @param peerInfoFile The path to the PeerInfo.cfg file
	 * @throws IOException If there is an error reading the PeerInfo.cfg file
	 */
	public static CommonConfig readCommonConfig(Path commonConfigPath) throws IOException, NumberFormatException
	{
		if (Files.notExists(commonConfigPath))
		{
			StringBuilder errorMessage = new StringBuilder("Error: ");
			errorMessage.append(commonConfigPath.toString());
			errorMessage.append(" does not exist.");
			throw new IOException(errorMessage.toString());
		}
		CommonConfig config = new CommonConfig();

		String line = "";
		int lineNumber = 0;
		try (BufferedReader reader = Files.newBufferedReader(commonConfigPath))
		{
			while ((line = reader.readLine()) != null)
			{
				StringTokenizer tokenizer = new StringTokenizer(line, DELIMITER);
				String token = tokenizer.nextToken();
				ConfigOption peerInfo = ConfigOption.parse(token);
				switch (peerInfo)
				{
				case FILE_NAME:
					config.fileName = tokenizer.nextToken();
					break;
				case FILE_SIZE:
					config.fileSize = Integer.parseInt(tokenizer.nextToken());
					break;
				case OPTIMISTIC_UNCHOKING_INTERVAL:
					int optimisticSeconds = Integer.parseInt(tokenizer.nextToken());
					config.optimisticUnchokingInterval = Duration.ofSeconds(optimisticSeconds);
					break;
				case PIECE_SIZE:
					config.pieceSize = Integer.parseInt(tokenizer.nextToken());
					break;
				case PREFERRED_NEIGHBORS:
					config.numberOfPreferredNeightbors = Integer.parseInt(tokenizer.nextToken());
					break;
				case UNCHOKING_INTERVAL:
					int unchokingSeconds = Integer.parseInt(tokenizer.nextToken());
					config.unchokingInterval = Duration.ofSeconds(unchokingSeconds);
					break;
				default:
					StringBuilder errorMessage = new StringBuilder("Error: Unrecognized common config parameter ");
					errorMessage.append(token);
					errorMessage.append(".");
					throw new IOException(errorMessage.toString());
				}
			}
		}
		catch (Exception e)
		{
			StringBuilder errorMessage = new StringBuilder("Error: Unable to read common config file ");
			errorMessage.append(commonConfigPath.toAbsolutePath());
			errorMessage.append(" on file ");
			errorMessage.append(lineNumber);
			errorMessage.append(". ");
			errorMessage.append(e.getMessage());
			throw new IOException(errorMessage.toString());
		}
		return config;
	}

	/**
	 * Returns the number of preferred neighbors
	 * 
	 * @return The number of preferred neighbors
	 */
	public int getNumberOfPreferredNeightbors()
	{
		return numberOfPreferredNeightbors;
	}

	/**
	 * Returns the unchoking interval
	 * 
	 * @return
	 */
	public Duration getUnchokingInterval()
	{
		return unchokingInterval;
	}

	/**
	 * Returns the optimistic unchoking interval
	 * 
	 * @return The optimistic unchoking interval
	 */
	public Duration getOptimisticUnchokingInterval()
	{
		return optimisticUnchokingInterval;
	}

	/**
	 * Returns the file name config option as a path
	 * 
	 * @return The file name config option as a path
	 */
	public Path getPath()
	{
		return Paths.get(this.getFileName());
	}

	/**
	 * Returns the file name config option
	 * 
	 * @return The file name config option
	 */
	public String getFileName()
	{
		return fileName;
	}

	/**
	 * Returns the file size config option
	 * 
	 * @return The file size config option
	 */
	public int getFileSize()
	{
		return fileSize;
	}

	/**
	 * Returns the piece size for the file
	 * 
	 * @return The piece size for the file
	 */
	public int getPieceSize()
	{
		return pieceSize;
	}

	/**
	 * Returns the number of pieces for the provided file
	 * 
	 * The number of pieces is determined the the file size divided by the piece
	 * size. The resulting calculation is rounded up.
	 * 
	 * @return The number of pieces
	 */
	public int getNumberOfPieces()
	{
		return (int) Math.ceil((double) fileSize / pieceSize);
	}

	/**
	 * Returns the size of the last piece
	 * 
	 * @return The size of the last piece
	 */
	public int getLastPieceSize()
	{
		return (int) Math.ceil((double) fileSize % pieceSize);
	}

	/**
	 * Returns the number of bits for the bitfield
	 * 
	 * @return The size of the last piece
	 */
	public int getBitfieldPieceCount()
	{
		return (int) Math.ceil((double) (this.getNumberOfPieces()) / 8);
	}
}

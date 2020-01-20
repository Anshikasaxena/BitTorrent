package bittorrent.app;

import java.nio.file.Path;

public class SettingsLoader
{
	private static BitTorrentSettings settings;

	public static void loadSettings(Path common, Path peer)
	{
		settings = BitTorrentSettings.initializeSettings(common, peer);
	}

	public static BitTorrentSettings settings()
	{
		return settings;
	}
}

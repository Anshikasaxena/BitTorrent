package bittorrent.app;

import bittorrent.BitTorrent;
import bittorrent.app.view.SimulationWindow;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;

//@formatter:off
/**
 * A bit torrent configuration GUI
 * 
 * This class runs a JavaFX configuration GUI for the bit torrent
 * implementation. Configuration parameters are available for each of the
 * parameters of the peer and common config. Additional config parameters
 * include:
 * 	- number of neighbors
 */
//@formatter:on
public class BitTorrentApp extends Application
{
	private static Stage primaryStage;

	public static void main(String[] args)
	{
		launch(args);
	}

	public static Stage getStage()
	{
		return BitTorrentApp.primaryStage;
	}

	/**
	 * Shutdown the bit torrent configuration application
	 */
	public static void shutdown()
	{
		Platform.exit();
	}

	@Override
	public void start(Stage primaryStage) throws Exception
	{
		BitTorrentApp.primaryStage = primaryStage;
		SettingsLoader.loadSettings(BitTorrent.COMMON_CONFIG_FILE, BitTorrent.PEER_CONFIG_FILE);
		SimulationWindow window = new SimulationWindow();
		Scene primaryScene = new Scene(window, BitTorrent.DEFAULT_WINDOW_WIDTH, BitTorrent.DEFAULT_WINDOW_HEIGHT);
		BitTorrentApp.primaryStage.setScene(primaryScene);
		BitTorrentApp.primaryStage.setTitle(BitTorrent.APPLICATION_NAME);
		BitTorrentApp.primaryStage.show();
	}

}

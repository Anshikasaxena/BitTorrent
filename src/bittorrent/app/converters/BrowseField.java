package bittorrent.app.converters;

import java.io.File;
import java.nio.file.Paths;

import bittorrent.app.BitTorrentApp;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

/**
 * A class to browse a textbox to a button
 */
public class BrowseField
{

	public static void bindBrowse(Button button, TextField textField)
	{
		String currentLocation = System.getProperty("user.dir");
		FileChooser chooser = new FileChooser();
		ExtensionFilter configFilter = new ExtensionFilter("config files (*.cfg)", "*.cfg");
		chooser.getExtensionFilters().add(configFilter);
		chooser.setInitialDirectory(Paths.get(currentLocation).toFile());
		button.setOnAction(value ->
		{
			File selectedFile = chooser.showOpenDialog(BitTorrentApp.getStage());
			if (selectedFile.exists())
			{
				textField.setText(selectedFile.toString());
			}
		});
	}
}

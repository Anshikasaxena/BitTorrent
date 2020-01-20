package bittorrent.app.view;

import bittorrent.app.BitTorrentApp;
import bittorrent.app.BitTorrentSettings;
import bittorrent.app.SettingsLoader;
import bittorrent.app.converters.BrowseField;
import bittorrent.app.converters.PathConverter;
import bittorrent.protocol.peer.PeerProcess;
import javafx.beans.binding.Bindings;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

/**
 * Window of the Bit torrent configuration GUI
 */
public class SimulationWindow extends BorderPane
{
	protected ScrollPane scrollPane;

	protected final TextField commonConfigTextField;
	protected final TextField peerConfigTextField;
	protected final TextField commonFileTextField;

	public SimulationWindow()
	{

		this.setPadding(new Insets(10, 10, 5, 10));

		GridPane gridPane = new GridPane();
		setCenter(gridPane);
		gridPane.setMinWidth(100);
		gridPane.setPadding(new Insets(10, 10, 10, 10));
		gridPane.setHgap(10);
		gridPane.setVgap(10);

		ColumnConstraints labelColumn = new ColumnConstraints();
		labelColumn.setHgrow(Priority.ALWAYS);
		labelColumn.setHalignment(HPos.RIGHT);

		ColumnConstraints interfaceColumn = new ColumnConstraints();
		interfaceColumn.setHgrow(Priority.ALWAYS);
		interfaceColumn.setHalignment(HPos.LEFT);
		interfaceColumn.setFillWidth(false);
		interfaceColumn.setPercentWidth(50);

		ColumnConstraints browseColumn = new ColumnConstraints();
		labelColumn.setHgrow(Priority.ALWAYS);
		labelColumn.setHalignment(HPos.RIGHT);

		gridPane.getColumnConstraints().add(labelColumn);
		gridPane.getColumnConstraints().add(interfaceColumn);
		gridPane.getColumnConstraints().add(browseColumn);

		BitTorrentSettings settings = SettingsLoader.settings();
		int rowIndex = 0;
		Label commonConfigLabel = new Label("Common Config:");
		gridPane.add(commonConfigLabel, 0, rowIndex);
		this.commonConfigTextField = new TextField("");
		Bindings.bindBidirectional(this.commonConfigTextField.textProperty(), settings.commonConfigFileProperty(),
				new PathConverter());
		gridPane.add(this.commonConfigTextField, 1, rowIndex);
		Button commonConfigButton = new Button("Browse...");
		gridPane.add(commonConfigButton, 2, rowIndex);
		commonConfigButton.setMaxWidth(Double.MAX_VALUE);
		BrowseField.bindBrowse(commonConfigButton, this.commonConfigTextField);
		rowIndex++;

		Label peerConfigLabel = new Label("Peer Config:");
		gridPane.add(peerConfigLabel, 0, rowIndex);
		this.peerConfigTextField = new TextField("");
		Bindings.bindBidirectional(this.peerConfigTextField.textProperty(), settings.peerConfigFileProperty(),
				new PathConverter());
		gridPane.add(this.peerConfigTextField, 1, rowIndex);
		Button peerConfigButton = new Button("Browse...");
		gridPane.add(peerConfigButton, 2, rowIndex);
		peerConfigButton.setMaxWidth(Double.MAX_VALUE);
		BrowseField.bindBrowse(peerConfigButton, this.peerConfigTextField);
		rowIndex++;

		Label commonFileLabel = new Label("Shared File:");
		gridPane.add(commonFileLabel, 0, rowIndex);
		this.commonFileTextField = new TextField("");
		Bindings.bindBidirectional(this.commonFileTextField.textProperty(), settings.sharedFileProperty(),
				new PathConverter());
		gridPane.add(this.commonFileTextField, 1, rowIndex);
		Button commonFileButton = new Button("Browse...");
		gridPane.add(commonFileButton, 2, rowIndex);
		commonFileButton.setMaxWidth(Double.MAX_VALUE);
		BrowseField.bindBrowse(commonFileButton, this.commonFileTextField);
		rowIndex++;

		ProgressBar progressBar = new ProgressBar();
		progressBar.progressProperty().bind(settings.progressProperty());

		ButtonBar buttonBar = new ButtonBar();
		buttonBar.setPadding(new Insets(5, 5, 5, 5));
		Button exitButton = new Button("Exit");
		exitButton.setOnAction(value ->
		{
			BitTorrentApp.shutdown();
		});
		buttonBar.getButtons().add(exitButton);
		Button startButton = new Button("Start");
		startButton.setPrefWidth(100);
		startButton.setDefaultButton(true);
		startButton.setOnAction(value ->
		{
			PeerProcess.createNodeThreads();
		});
		buttonBar.getButtons().add(startButton);
		this.setBottom(buttonBar);
	}
}

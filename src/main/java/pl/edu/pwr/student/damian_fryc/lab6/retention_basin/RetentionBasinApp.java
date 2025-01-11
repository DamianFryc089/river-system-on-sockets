package pl.edu.pwr.student.damian_fryc.lab6.retention_basin;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class RetentionBasinApp extends Application {
	private RetentionBasin basin;
	private final StackPane mainPane = new StackPane();
	private final Label fillingLabelPercentage = new Label();
	private final Label fillingLabelValue = new Label();
	private final Rectangle backgroundFillBlock = new Rectangle();

	private double windowWidth = 0;
	private double windowHeight = 0;

	@Override
	public void start(Stage stage) {
		Scene scene = new Scene(mainPane, 300, 250);

		windowWidth = scene.getWidth();
		windowHeight = scene.getHeight();
		scene.widthProperty().addListener((observable, oldValue, newValue) -> windowWidth = scene.getWidth());
		scene.heightProperty().addListener((observable, oldValue, newValue) -> windowHeight = scene.getHeight());

		retentionBasinInput(stage);

		stage.setTitle("Retention Basin");
		stage.setScene(scene);
		stage.show();
	}

	private void retentionBasinInput(Stage stage){
		mainPane.getChildren().clear();

		Text text = new Text("Retention Basin");
		TextField maxVolume = new TextField("1000");
		maxVolume.setTextFormatter(new TextFormatter<>(change -> {
			String newText = change.getText();
			if (newText.matches("\\d*"))
				return change;
			return null;
		}));
		HBox volumeBox = new HBox(new Text("Volume: "), maxVolume);
		volumeBox.alignmentProperty().set(Pos.CENTER);

		TextField portInput = new TextField("5000");
		portInput.setTextFormatter(new TextFormatter<>(change -> {
			String newText = change.getText();
			if (newText.matches("\\d*"))
				return change;
			return null;
		}));
		HBox portBox = new HBox(new Text("     Port: "), portInput);
		portBox.alignmentProperty().set(Pos.CENTER);

		Button acceptButton = new Button("Set");
		acceptButton.setOnAction(event -> {
			basin = new RetentionBasin(Integer.parseInt(maxVolume.getText()), Integer.parseInt(portInput.getText()));
			basin.startServer();
			stage.setOnCloseRequest(event2 -> basin.stopServer());
			stage.setTitle("Retention Basin - " + Integer.parseInt(portInput.getText()));
			controlCenterInput();
		});
		VBox vbox = new VBox(5, text, volumeBox, portBox, acceptButton);
		vbox.alignmentProperty().set(Pos.CENTER);
		mainPane.getChildren().add(vbox);
	}

	private void controlCenterInput(){
		mainPane.getChildren().clear();

		Text text = new Text("Control center");
		TextField hostInput = new TextField("localhost");
		HBox hostBox = new HBox(new Text("Host: "), hostInput);
		hostBox.alignmentProperty().set(Pos.CENTER);

		TextField portInput = new TextField("8000");
		portInput.setTextFormatter(new TextFormatter<>(change -> {
			String newText = change.getText();
			if (newText.matches("\\d*"))
				return change;
			return null;
		}));
		HBox portBox = new HBox(new Text("Port: "), portInput);
		portBox.alignmentProperty().set(Pos.CENTER);

		Button acceptButton = new Button("Set");
		acceptButton.setOnAction(event -> {
			boolean connected = basin.setControlCenter(hostInput.getText(), Integer.parseInt(portInput.getText()));
			if (connected){
				inflowRiverInput();
			}
		});
		VBox vbox = new VBox(5, text, hostBox, portBox, acceptButton);
		vbox.alignmentProperty().set(Pos.CENTER);
		mainPane.getChildren().add(vbox);
	}

	private void inflowRiverInput(){
		mainPane.getChildren().clear();

		Text text = new Text("Inflow river");
		TextField hostInput = new TextField("localhost");
		HBox hostBox = new HBox(new Text("Host: "), hostInput);
		hostBox.alignmentProperty().set(Pos.CENTER);

		TextField portInput = new TextField("6000");
		portInput.setTextFormatter(new TextFormatter<>(change -> {
			String newText = change.getText();
			if (newText.matches("\\d*"))
				return change;
			return null;
		}));
		HBox portBox = new HBox(new Text("Port: "), portInput);
		portBox.alignmentProperty().set(Pos.CENTER);

		Button addButton = new Button("Add (0)");
		addButton.setOnAction(event -> {
			boolean added = basin.addInflowRiver(hostInput.getText(), Integer.parseInt(portInput.getText()));
			if(added) {
				int count = Integer.parseInt(addButton.getText().substring(5, addButton.getText().length() - 1)) + 1;
				addButton.setText("Add (" + count + ")");
			}
		});

		Button acceptButton = new Button("Ok");
		acceptButton.setOnAction(event -> startInput());

		HBox buttonBox = new HBox(addButton, acceptButton);
		buttonBox.alignmentProperty().set(Pos.CENTER);

		VBox vbox = new VBox(5, text, hostBox, portBox, buttonBox);
		vbox.alignmentProperty().set(Pos.CENTER);
		mainPane.getChildren().add(vbox);
	}

	private void startInput(){
		mainPane.getChildren().clear();
		Button acceptButton = new Button("START");
		acceptButton.setOnAction(event -> {
			showFillingPercentage();

			Thread updateThread = new Thread(() -> {
				try {
					while (true) {
						basin.basinLogic();
						updateFillingPercentage();
						Thread.sleep(1000);
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			});
			updateThread.setDaemon(true);
			updateThread.start();
		});

		mainPane.getChildren().add(acceptButton);
	}

	private void showFillingPercentage(){
		mainPane.getChildren().clear();
		fillingLabelValue.setStyle(
				"-fx-font-size: 25;"
		);
		fillingLabelPercentage.setStyle(
				"-fx-font-size: 25;"
		);
		VBox vbox = new VBox(5, fillingLabelValue, fillingLabelPercentage);
		vbox.alignmentProperty().set(Pos.CENTER);

		backgroundFillBlock.setWidth(0);
		backgroundFillBlock.setHeight(0);
		backgroundFillBlock.setFill(Color.LIGHTBLUE);

		mainPane.getChildren().addAll(backgroundFillBlock, vbox);
		updateFillingPercentage();
	}

	private void updateFillingPercentage() {
		javafx.application.Platform.runLater(() -> {
			fillingLabelValue.setText(basin.getCurrentVolume() + " / " + basin.getMaxVolume()+"m³");

			long percentage = basin.getFillingPercentage();
			fillingLabelPercentage.setText(percentage + "%");

			backgroundFillBlock.setWidth(windowWidth);
			backgroundFillBlock.setHeight(windowHeight * percentage / 50);
			backgroundFillBlock.setTranslateY(windowHeight/2);
		});
	}

	public static void main(String[] args) {
		launch(args);
	}
}
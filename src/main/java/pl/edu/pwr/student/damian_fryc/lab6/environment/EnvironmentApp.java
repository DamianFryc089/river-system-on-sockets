package pl.edu.pwr.student.damian_fryc.lab6.environment;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.ArrayList;

public class EnvironmentApp extends Application{
	private Environment environment;
	private final StackPane mainPane = new StackPane();
	private final VBox riverBoxes = new VBox();

	private final ArrayList<Text> riverInfosHostAndPort = new ArrayList<>();
	private final ArrayList<Text> riverInfosRainfall = new ArrayList<>();

	@Override
	public void start(Stage stage) {
		Scene scene = new Scene(mainPane, 600, 300);
		environmentInput(stage);

		stage.setTitle("Environment");
		stage.setScene(scene);
		stage.show();
	}

	private void environmentInput(Stage stage){
		mainPane.getChildren().clear();

		Text text = new Text("Environment");

		TextField portInput = new TextField("7000");
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
			environment = new Environment(Integer.parseInt(portInput.getText()));
			environment.startServer();
			stage.setOnCloseRequest(event2 -> environment.stopServer());
			stage.setTitle("Environment - " + Integer.parseInt(portInput.getText()));
			startUpdatingInfos();
		});
		VBox vbox = new VBox(5, text, portBox, acceptButton);
		vbox.alignmentProperty().set(Pos.CENTER);
		mainPane.getChildren().add(vbox);
	}

	private void startUpdatingInfos(){
		mainPane.getChildren().clear();
		mainPane.getChildren().add(riverBoxes);
		Thread updateThread = new Thread(() -> {
			try {
				while (true) {
					environment.deliverNewRain();
					updateRiverInfos();
					Thread.sleep(1000);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		});
		updateThread.setDaemon(true);
		updateThread.start();
	}

	private void updateRiverInfos() {
		javafx.application.Platform.runLater(() -> {
			ArrayList<RiverSectionInfo> newRiverInfos = environment.getRiverSections();
			for (int i = riverBoxes.getChildren().size(); i < newRiverInfos.size(); i++)
				createNewRiverBox(i);

			for (int i = 0; i < newRiverInfos.size(); i++) {
				riverInfosHostAndPort.get(i).setText(newRiverInfos.get(i).getHost() + ":" + newRiverInfos.get(i).getPort());
				riverInfosRainfall.get(i).setText(newRiverInfos.get(i).rainAmount + "m³/s");
			}
		});
	}

	private void createNewRiverBox(int i){
		HBox newBasin = new HBox(20);
		newBasin.setAlignment(Pos.CENTER);

		Text newBasinInfosHostAndPort = new Text("---");
		riverInfosHostAndPort.add(newBasinInfosHostAndPort);

		StackPane fillingInfo = new StackPane();
		Text currentRainfall = new Text("---");
		riverInfosRainfall.add(currentRainfall);
		fillingInfo.getChildren().add(currentRainfall);

		TextField newRainfall = new TextField("0");
		newRainfall.setTextFormatter(new TextFormatter<>(change -> {
			String newText = change.getText();
			if (newText.matches("\\d*"))
				return change;
			return null;
		}));

		Button changeDischargeButton = new Button("Change");
		changeDischargeButton.setOnAction(event -> environment.setRainfall(i, Integer.parseInt(newRainfall.getText())));


		HBox newRainfallBox = new HBox(5, new Text("New: "), newRainfall, new Text("m³/s"), changeDischargeButton);
		newRainfallBox.setAlignment(Pos.CENTER);

		newBasin.getChildren().addAll(newBasinInfosHostAndPort, fillingInfo, newRainfallBox);
		riverBoxes.getChildren().add(newBasin);
	}

	public static void main(String[] args) {
		launch(args);
	}
}
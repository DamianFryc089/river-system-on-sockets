package pl.edu.pwr.student.damian_fryc.lab6.control_center;

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

public class ControlCenterApp extends Application {
	private ControlCenter controlCenter;
	private final StackPane mainPane = new StackPane();
	private final VBox basinBoxes = new VBox();

	private final ArrayList<Text> basinInfosHostAndPort = new ArrayList<>();
	private final ArrayList<Text> basinInfosFillingPercentage = new ArrayList<>();
	private final ArrayList<Text> basinInfosCurrentDischarge = new ArrayList<>();

	@Override
	public void start(Stage stage) {
		Scene scene = new Scene(mainPane, 600, 300);
		controlCenterInput(stage);
		stage.setTitle("Control Center");
		stage.setScene(scene);
		stage.show();
	}

	private void controlCenterInput(Stage stage){
		mainPane.getChildren().clear();

		Text text = new Text("Control Center");

		TextField portInput = new TextField("8000");
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
			controlCenter = new ControlCenter(Integer.parseInt(portInput.getText()));
			controlCenter.startServer();
			stage.setOnCloseRequest(event2 -> controlCenter.stopServer());
			stage.setTitle("Control Center - " + Integer.parseInt(portInput.getText()));
			startUpdatingInfos();
		});
		VBox vbox = new VBox(5, text, portBox, acceptButton);
		vbox.alignmentProperty().set(Pos.CENTER);
		mainPane.getChildren().add(vbox);
	}

	private void startUpdatingInfos(){
		mainPane.getChildren().clear();
		mainPane.getChildren().add(basinBoxes);
		Thread updateThread = new Thread(() -> {
			try {
				while (true) {
					updateBasinInfos();
					Thread.sleep(1000);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		});
		updateThread.setDaemon(true);
		updateThread.start();
	}

	private void updateBasinInfos() {
		javafx.application.Platform.runLater(() -> {
			ArrayList<RetentionBasinInfo> newBasinsInfo = controlCenter.updatePercentages();
			if(newBasinsInfo.isEmpty()) {
				basinBoxes.getChildren().clear();
				basinInfosHostAndPort.clear();
				basinInfosFillingPercentage.clear();
				basinInfosCurrentDischarge.clear();
				return;
			}
			for (int i = basinBoxes.getChildren().size(); i < newBasinsInfo.size(); i++)
				createNewBasinBox(i);

			for (int i = 0; i < newBasinsInfo.size(); i++) {
				basinInfosHostAndPort.get(i).setText(newBasinsInfo.get(i).getHost() + ":" + newBasinsInfo.get(i).getPort());
				basinInfosFillingPercentage.get(i).setText(newBasinsInfo.get(i).fillingPercentage + "%");
				basinInfosCurrentDischarge.get(i).setText(newBasinsInfo.get(i).waterDischarge + "m³/s");
			}
		});
	}

	private void createNewBasinBox(int i){
		HBox newBasin = new HBox(20);
		newBasin.setAlignment(Pos.CENTER);

		Text newBasinInfosHostAndPort = new Text("---");
		basinInfosHostAndPort.add(newBasinInfosHostAndPort);

		StackPane fillingInfo = new StackPane();
		Text currentFillingPercentage = new Text("---");
		basinInfosFillingPercentage.add(currentFillingPercentage);
		fillingInfo.getChildren().add(currentFillingPercentage);

		Text currentDischarge = new Text("---");
		basinInfosCurrentDischarge.add(currentDischarge);

		TextField newDischarge = new TextField();
		newDischarge.setTextFormatter(new TextFormatter<>(change -> {
			String newText = change.getText();
			if (newText.matches("\\d*"))
				return change;
			return null;
		}));
		Button changeDischargeButton = new Button("Change");
		changeDischargeButton.setOnAction(event -> controlCenter.setWaterDischarge(i, Integer.parseInt(newDischarge.getText())));
		HBox newDischargeBox = new HBox(5, new Text("New: "), newDischarge, new Text("m³/s"), changeDischargeButton);
		newDischargeBox.setAlignment(Pos.CENTER);

		newBasin.getChildren().addAll(newBasinInfosHostAndPort, fillingInfo, currentDischarge, newDischargeBox);
		basinBoxes.getChildren().add(newBasin);
	}

	public static void main(String[] args) {
		launch(args);
	}
}
package pl.edu.pwr.student.damian_fryc.lab6.river_section;

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

public class RiverSectionApp extends Application{
		private RiverSection riverSection;
		private final StackPane mainPane = new StackPane();
		private Text[] waterFragments;

		@Override
		public void start(Stage stage) {
			Scene scene = new Scene(mainPane, 300, 250);
			riverSectionInput(stage);

			stage.setTitle("River Section");
			stage.setScene(scene);
			stage.show();
		}

		private void riverSectionInput(Stage stage){
			mainPane.getChildren().clear();

			Text text = new Text("River Section");
			TextField size = new TextField("5");
			size.setTextFormatter(new TextFormatter<>(change -> {
				String newText = change.getText();
				if (newText.matches("\\d*"))
					return change;
				return null;
			}));
			HBox volumeBox = new HBox(new Text("Size: "), size);
			volumeBox.alignmentProperty().set(Pos.CENTER);

			TextField portInput = new TextField("6000");
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
				riverSection = new RiverSection(Integer.parseInt(size.getText()), Integer.parseInt(portInput.getText()));

				waterFragments = new Text[riverSection.waterFragments.length];
				for (int i = 0; i < waterFragments.length; i++)
					waterFragments[i] = new Text();

				stage.setTitle("River Section - " + Integer.parseInt(portInput.getText()));
				environmentInput();
			});

			VBox vbox = new VBox(5, text, volumeBox, portBox, acceptButton);
			vbox.alignmentProperty().set(Pos.CENTER);
			mainPane.getChildren().add(vbox);
		}

		private void environmentInput(){
			mainPane.getChildren().clear();

			Text text = new Text("Environment");
			TextField hostInput = new TextField("localhost");
			HBox hostBox = new HBox(new Text("Host: "), hostInput);
			hostBox.alignmentProperty().set(Pos.CENTER);

			TextField portInput = new TextField("7000");
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
				boolean connected = riverSection.setEnvironment(hostInput.getText(), Integer.parseInt(portInput.getText()));
				if (connected){
					inflowBasinInput();
				}
			});

			VBox vbox = new VBox(5, text, hostBox, portBox, acceptButton);
			vbox.alignmentProperty().set(Pos.CENTER);
			mainPane.getChildren().add(vbox);
		}

		private void inflowBasinInput(){
			mainPane.getChildren().clear();

			Text text = new Text("Inflow Basin");
			TextField hostInput = new TextField("localhost");
			HBox hostBox = new HBox(new Text("Host: "), hostInput);
			hostBox.alignmentProperty().set(Pos.CENTER);

			TextField portInput = new TextField("5000");
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
				boolean connected = riverSection.setInflowBasin(hostInput.getText(), Integer.parseInt(portInput.getText()));
				if (connected){
					startInput();
				}
			});

			Button noneButton = new Button("None");
			noneButton.setOnAction(event -> startInput());

			HBox buttonBox = new HBox(acceptButton, noneButton);
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
							riverSection.riverLogic();
							updateWaterFragments();
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
			VBox vbox = new VBox(waterFragments);
			vbox.alignmentProperty().set(Pos.CENTER);
			mainPane.getChildren().add(vbox);
			updateWaterFragments();
		}

		private void updateWaterFragments() {
			javafx.application.Platform.runLater(() -> {
				for (int i = 0; i < waterFragments.length; i++) {
					waterFragments[i].setText(riverSection.waterFragments[i]+"");
				}
			});
		}

		public static void main(String[] args) {
			launch(args);
		}
	}

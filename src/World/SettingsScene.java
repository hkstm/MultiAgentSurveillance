package World;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;

public class SettingsScene extends VBox {

    private Stage primaryStage;
    private Scene scene;
    private Button submit;
    private Button loadWorldMap;
    private ComboBox<String> size;

    private static int actionWorldSize;
    private static WorldMap actionWorld;
    private int windowSize = 800;

    public SettingsScene(Stage primaryStage) {
        this.windowSize  = 800;
        actionWorldSize = Settings.SIZE_SMALL;
        actionWorld = new WorldMap(actionWorldSize);
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("Multi-Agent Surveillance - Settings");
        Label label = new Label("Welcome to the Multi-Agent Surveillance demo!");
        label.setTextFill(Color.web("#FFFFFF"));

        submit = new Button("Start");
        submit.setOnAction(e -> {
            Settings settings = new Settings(actionWorldSize, actionWorld);    //instantiating the settings object with the int values
            WorldBuilder worldBuilder = new WorldBuilder(primaryStage, settings);
            Node source = (Node) e.getSource();
            Stage stage = (Stage) source.getScene().getWindow();
            stage.close();
            this.primaryStage = new Stage();
            this.primaryStage.setTitle("MultiAgentScene");
            this.primaryStage.setScene(worldBuilder.getWorldBuilder());
            this.primaryStage.show();
        });

        loadWorldMap = new Button("Load a custom WorldMap");
        loadWorldMap.setOnAction(e -> {
            File recordsDir = new File(System.getProperty("user.home"), ".MultiAgentSurveillance/maps");
            if (! recordsDir.exists()) {
                recordsDir.mkdirs();
            }
            FileChooser fileChooser = new FileChooser();
            fileChooser.setInitialDirectory(recordsDir);
            fileChooser.setTitle("Open WorldMap File");
            fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Text Files", "*.txt"));
            File selectedFile = fileChooser.showOpenDialog(primaryStage);
            FileInputStream fileInputStream = null;
            try {
                fileInputStream = new FileInputStream(selectedFile);
                ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
                actionWorld = (WorldMap) objectInputStream.readObject();
                objectInputStream.close();
            } catch (Exception err) {
                err.printStackTrace();
            }
        });

        size = new ComboBox<>();
        size.getItems().addAll("Small", "Medium", "Large");
        size.setPromptText("Select board size");
        size.setOnAction(e -> {
            if (size.getValue() == "Small") {
                actionWorldSize = Settings.SIZE_SMALL;
            } else if (size.getValue() == "Medium") {
                actionWorldSize = Settings.SIZE_MEDIUM;
            } else {
                actionWorldSize = Settings.SIZE_LARGE;
            }
        });

        VBox layout = new VBox(20);
        layout.getChildren().addAll(label, size, loadWorldMap, submit);
        layout.setAlignment(Pos.CENTER);
        File backgrFile = new File("src/Assets/Othello.jpg");
        BackgroundImage myBI= new BackgroundImage(new Image(backgrFile.toURI().toString(),windowSize, windowSize * 0.75,true,true),
                BackgroundRepeat.REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT,
                BackgroundSize.DEFAULT);
        layout.setBackground(new Background(myBI));
        scene = new Scene(layout, windowSize, windowSize * 0.75);
    }

    public Scene getSettingsScene() {
        return scene;
    }
}


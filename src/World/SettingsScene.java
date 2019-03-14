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
    private Button startWorldBuilder;
    private Button startGameScene;
    private Button loadWorldMap;
    private Button loadWorldImage;
    private ComboBox<String> sizeComboBox;

    private static int worldSizeSelection;
    private static WorldMap worldMapSelection;
    private static Image imageSelection;
    private int windowSize;

    public static final int SIZE_SMALL = 50;
    public static final int SIZE_MEDIUM = 200;
    public static final int SIZE_LARGE = 500;

    public SettingsScene()
    {
        this.windowSize  = 1000;
    }

    public SettingsScene(Stage primaryStage) {
        this.windowSize  = 1000;
        worldSizeSelection = SIZE_SMALL;
        worldMapSelection = new WorldMap(worldSizeSelection);
        imageSelection = new Image(new File("src/Assets/emptyWorldIMG.png").toURI().toString(), worldMapSelection.getSize(), worldMapSelection.getSize(), false, false, true);
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("Multi-Agent Surveillance - Settings");
        Label label = new Label("Welcome to the Multi-Agent Surveillance demo!");
        label.setTextFill(Color.web("#FFFFFF"));

        startWorldBuilder = new Button("Start World Builder");
        startWorldBuilder.setOnAction(e -> {
            Settings settings = new Settings(worldMapSelection);
            WorldBuilder worldBuilder = new WorldBuilder(primaryStage, settings);
            Node source = (Node) e.getSource();
            Stage stage = (Stage) source.getScene().getWindow();
            stage.close();
            this.primaryStage = new Stage();
            this.primaryStage.setTitle("MultiAgentScene");
            this.primaryStage.setScene(worldBuilder.getWorldBuilder());
            this.primaryStage.show();
        });

        startGameScene = new Button("Start Game Scene");
        startGameScene.setOnAction(e -> {
            Node source = (Node) e.getSource();
            Stage stage = (Stage) source.getScene().getWindow();
            stage.close();
            this.primaryStage = new Stage();
            Settings settings = new Settings(worldMapSelection);
            GameScene gameScene = new GameScene(primaryStage, settings);
            this.primaryStage.setTitle("MultiAgentGameScene");
            this.primaryStage.setScene(gameScene.getGameScene());
            this.primaryStage.show();
        });

        loadWorldMap = new Button("Load a custom worldMap");
        loadWorldMap.setOnAction(e -> {
            File recordsDir = new File(System.getProperty("user.home"), ".MultiAgentSurveillance/maps");
            if (! recordsDir.exists()) {
                recordsDir.mkdirs();
            }
            FileChooser fileChooser = new FileChooser();
            fileChooser.setInitialDirectory(recordsDir);
            fileChooser.setTitle("Open worldMap File");
            fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Text Files", "*.txt"));
            File selectedFile = fileChooser.showOpenDialog(primaryStage);
            FileInputStream fileInputStream = null;
            try {
                fileInputStream = new FileInputStream(selectedFile);
                ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
                worldMapSelection = (WorldMap) objectInputStream.readObject();
                objectInputStream.close();
            } catch (Exception err) {
                err.printStackTrace();
            }
        });

        sizeComboBox = new ComboBox<>();
        sizeComboBox.getItems().addAll("Small", "Medium", "Large");
        sizeComboBox.setPromptText("Select World Size");
        sizeComboBox.setOnAction(e -> {
            if (sizeComboBox.getValue() == "Small") {
                worldSizeSelection = SIZE_SMALL;
            } else if (sizeComboBox.getValue() == "Medium") {
                worldSizeSelection = SIZE_MEDIUM;
            } else {
                worldSizeSelection = SIZE_LARGE;
            }
        });

        VBox layout = new VBox(20);
        layout.getChildren().addAll(label, sizeComboBox, loadWorldMap, startWorldBuilder, startGameScene);
        layout.setAlignment(Pos.CENTER);
        File backgrFile = new File("src/Assets/MultiAgentSurveillance.jpg");
        BackgroundImage myBI= new BackgroundImage(new Image(backgrFile.toURI().toString(),windowSize, windowSize,false,true),
                BackgroundRepeat.REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT,
                BackgroundSize.DEFAULT);
        layout.setBackground(new Background(myBI));
        scene = new Scene(layout, windowSize, windowSize * 0.75);
    }

    public Scene getSettingsScene() {
        return scene;
    }

    public double getSize()
    {
        return windowSize;
    }
}
package World;

import javafx.application.Platform;
import javafx.event.EventHandler;
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
import javafx.stage.WindowEvent;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;

/**
 * Screen from which you can load a map, start the worldBuilder or start an actual "game"
 * @author Kailhan Hokstam
 */

public class SettingsScene extends VBox {

    private Stage primaryStage;
    private Scene scene;
    private Button startWorldBuilder;
    private Button startGameScene;
    private Button loadWorldMap;
    private ComboBox<String> sizeComboBox;
    private File selectedFile;

    private static int worldSizeSelection;
    private static WorldMap worldMapSelection;
    private int windowSize;

    public static final int SIZE_SMALL = 50;
    public static final int SIZE_MEDIUM = 100;
    public static final int SIZE_LARGE = 200;

    public SettingsScene(Stage primaryStage) {
        this.windowSize  = StartWorldBuilder.WINDOW_SIZE;
        worldSizeSelection = SIZE_MEDIUM;
        worldMapSelection = new WorldMap(worldSizeSelection);
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("Multi-Agent Surveillance - Settings");

        initButtons();
        initSizeSelectionBox();
        initScreenDisplayed();
    }

    /**
     * Adds all elements together and loads background
     */
    public void initScreenDisplayed(){
        Label label = new Label("Welcome to the Multi-Agent Surveillance demo!");
        label.setTextFill(Color.web("#FFFFFF"));
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

    /**
     * Inits buttons
     */
    public void initButtons(){
        initStartGameButton();
        initStartWorldBuilderButton();
        initLoadWorldMapButton();
    }

    /**
     * Inits button for switching to world builder
     */
    public void initStartGameButton(){

        startWorldBuilder = new Button("Start World Builder");
        startWorldBuilder.setOnAction(e -> {
            Settings settings = new Settings(worldMapSelection);
            WorldBuilder worldBuilder = new WorldBuilder(primaryStage, settings);
            this.primaryStage.setTitle("MultiAgentScene");
            this.primaryStage.setScene(worldBuilder.getWorldBuilder());
            this.primaryStage.show();
        });
    }

    /**
     * Inits button for loading a simulation/game
     */
    public void initStartWorldBuilderButton() {
        startGameScene = new Button("Start Game Scene");
        startGameScene.setOnAction(e -> {
            Settings settings = new Settings(worldMapSelection);
            GameScene gameScene = new GameScene(primaryStage, settings);
            this.primaryStage.setTitle("MultiAgentGameScene");
            this.primaryStage.setScene(gameScene.getGameScene());
            this.primaryStage.show();
        });
    }

    /**
     *  Inits button for loading a world map
     */
    public void initLoadWorldMapButton() {
        loadWorldMap = new Button("Load a custom worldMap");
        loadWorldMap.setOnAction(e -> {
            File recordsDir = new File(System.getProperty("user.home"), ".MultiAgentSurveillance/maps");
            if (! recordsDir.exists()) {
                recordsDir.mkdirs();
            }
            FileChooser fileChooser = new FileChooser();
            fileChooser.setInitialDirectory(recordsDir);
            fileChooser.setTitle("Open World Map File");
            //change this if you don't want to use dat files for some reason
            fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Text Files", "*.dat"));
//            if(fileChooser.showOpenDialog(primaryStage) != null)
                selectedFile = fileChooser.showOpenDialog(primaryStage);
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
    }

    /**
     * Inits box for selecting size of objects in world, because the world is always 200x200 this will change how big 1 tile is e.g.
     * small world will mean that 1 tile is really big
     */
    public void initSizeSelectionBox() {
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
    }

    public Scene getSettingsScene() {
        return scene;
    }
}
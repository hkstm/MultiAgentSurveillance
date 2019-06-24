package World;

import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javax.swing.*;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * World Builder allows saving and loading of WorldMap objects
 * @author Kailhan Hokstam
 */
public class WorldBuilder extends BorderPane {
    private Settings settings;
    private Stage primaryStage;
    private BorderPane bPane;
    private VBox buttonContainer;
    private Scene scene;
    private WorldMap worldMap;
    private GridPane grid;
    private int windowSize;
    private int tileSize;

    private List<TileButton> toAdd = new ArrayList<>();

    private Image[] tileImgArray;
    private Button goToMenuBut;
    private Button restartGameBut;
    private Button saveBoardBut;
    private ComboBox<String> tileTypeSelection;
    private int activeTile;

    private HashMap<String, Boolean> currentlyActiveKeys = new HashMap<>();
    private int qPressedRow;
    private int qPressedColumn;
    private boolean selectingRegion;

    public WorldBuilder(Stage primaryStage, Settings settings) {
        this.grid = new GridPane();
        this.windowSize = StartWorldBuilder.WINDOW_SIZE;
        this.settings = settings;
        this.primaryStage = primaryStage;
        this.worldMap = new WorldMap(settings.getWorldMap());
        this.tileSize = windowSize / worldMap.getSize();

        initTileImgArray();
        initTileTypeSelection();
        initGoToMenuButton();
        initRestartButton();
        initSaveBoardButton();

//        initTiles();
//        redrawBoard();

        initButtonContainer();
        createMainScene();
        initRegionSelection();

        initTiles();
        redrawBoard();
    }

    /**
     * Redraws board by deleting previous elements, creating clickable tiles and adding other elements of the worldBuilder screen
     */
    public void redrawBoard (){
        grid.getChildren().clear();
        createTiles();
        grid.getChildren().addAll(toAdd);
    }

    /**
     * Create tiles that can be clicked to change their state to currently selected state (activeTile) based on a WorldMap
     * Should not update all displayed tiles, only those whose state have been changed in selected WorldMap
     */
    public void createTiles(){
//        System.out.println("Create tiles");
        for (int r = 0; r < worldMap.getSize(); r++) {
            for (int c = 0; c < worldMap.getSize(); c++) {
                if(toAdd.get(c + (r * worldMap.getSize())).getTileStatus() != worldMap.getTileState(r, c)) {
                    toAdd.set(c + (r * worldMap.getSize()), new TileButton(r, c, tileImgArray[worldMap.getTileState(r,c)], tileSize, worldMap.getTileState(r,c)));
                }
                toAdd.get(c + (r * worldMap.getSize())).setOnAction((event) -> {
                    makeAButtonInteractive(event);
                    System.out.println("Action Detected");
                });
                toAdd.get(c + (r * worldMap.getSize())).arm();
                //System.out.println("creating tiles");
                GridPane.setConstraints(toAdd.get(c + (r * worldMap.getSize())), c, r);
            }
        }
    }


    /**
     * Makes the World Builder store which keys are pressed
     */
    public void initRegionSelection() {
        scene.setOnKeyPressed(event -> {
            String codeString = event.getCode().toString();

            if (!currentlyActiveKeys.containsKey(codeString)) {
                currentlyActiveKeys.put(codeString, true);
            }
        });
        scene.setOnKeyReleased(event ->
                currentlyActiveKeys.remove(event.getCode().toString())
        );
    }

    /**
     * Logic for selecting a region
     * @param event triggered by keypress
     */
    public void makeAButtonInteractive(ActionEvent event) {
        System.out.println("Making buttons interactive");
        TileButton button = (TileButton) event.getSource();
        if (!currentlyActiveKeys.containsKey("Q") && !selectingRegion) {
            worldMap.updateTile(button.getRow(), button.getColumn(), activeTile);
            redrawBoard();
            System.out.println("update world map");
            selectingRegion = false;
        } else if(selectingRegion) {
            worldMap.fillWorldArray(qPressedRow, qPressedColumn, button.getRow(), button.getColumn(), activeTile);
            System.out.println("called fillWorldArray");
            selectingRegion = false;
            removeActiveKey("Q");
            redrawBoard();
        } else {
            qPressedRow = button.getRow();
            qPressedColumn = button.getColumn();
            selectingRegion = true;
            System.out.println("selectingRegion set to true");
        }
    }

    /**
     * Removes a key as active e.g. currently pressed
     * @param codeString string associated with a certain key
     * @return if key associated with codeString was active
     */
    private boolean removeActiveKey(String codeString) {
        Boolean isActive = currentlyActiveKeys.get(codeString);
        if (isActive != null && isActive) {
            currentlyActiveKeys.put(codeString, false);
            System.out.println("Made :" + codeString + " inactive");
            return true;
        } else {
            return false;
        }
    }

    /**
     * Creates initial tiles for display
     */
    public void initTiles(){
        System.out.println("init tiles");
        toAdd.clear();
        for (int r = 0; r < worldMap.getSize(); r++) {
            for (int c = 0; c < worldMap.getSize(); c++) {
                toAdd.add(new TileButton(r, c, tileImgArray[worldMap.getTileState(r,c)], tileSize, worldMap.getTileState(r,c)));
                toAdd.get(c + (r * worldMap.getSize())).setOnAction((event) -> {
//                    System.out.println("Action detected");
                    makeAButtonInteractive(event);
                });
                toAdd.get(c + (r * worldMap.getSize())).arm();
                GridPane.setConstraints(toAdd.get(toAdd.size()-1), c, r);
            }
        }
    }

//    /**
//     * Updates world map and redraws board
//     * @param r y coordinate that needs to be updated
//     * @param c x coordinated that needs to be updated
//     * @param state of tile e.g. wall
//     */
//    public void updateWorldMap(int r, int c, int state) {
//        worldMap.updateTile(r, c, state);
//        redrawBoard();
//    }

    public Scene getWorldBuilder() {
        return scene;
    }

    /**
     * Loads all images for the world and stores them in an array
     */
    public void initTileImgArray(){
        Image emptyTileImg = new Image(new File("src/Assets/emptyTile.png").toURI().toString(), tileSize, tileSize, false, false, true);
        Image structureTileImg = new Image(new File("src/Assets/structureTile.png").toURI().toString(), tileSize, tileSize, false, false, true);
        Image doorTileImg = new Image(new File("src/Assets/doorTile.png").toURI().toString(), tileSize, tileSize, false, false, true);
        Image windowTileImg = new Image(new File("src/Assets/windowTile.png").toURI().toString(), tileSize, tileSize, false, false, true);
        Image targetTileImg = new Image(new File("src/Assets/targetTile.png").toURI().toString(), tileSize, tileSize, false, false, true);
        Image sentryTileImg = new Image(new File("src/Assets/sentryTile.png").toURI().toString(), tileSize, tileSize, false, false, true);
        Image decreasedVisRangeTileImg = new Image(new File("src/Assets/decreasedVisRangeTile.png").toURI().toString(), tileSize, tileSize, false, false, true);
        Image wallTileImg = new Image(new File("src/Assets/wallTile16.png").toURI().toString(), tileSize, tileSize, false, false, true);
        this.tileImgArray = new Image[]{emptyTileImg, structureTileImg, doorTileImg, windowTileImg, targetTileImg, sentryTileImg, decreasedVisRangeTileImg, wallTileImg};
    }

    /**
     * Inits drop down where you can pick what kind of tile you currently want to place in the world
     */
    public void initTileTypeSelection(){
        tileTypeSelection = new ComboBox<>();
        tileTypeSelection.getItems().addAll(Settings.TILE_TYPES);
        tileTypeSelection.setPromptText("Change tile type");

        tileTypeSelection.setOnAction(e -> {
            for (int i = 0; i < Settings.TILE_TYPES.length; i++){
                if (tileTypeSelection.getValue().equals(Settings.TILE_TYPES[i])){
                    activeTile = i;
                }
            }
        });
    }

    /**
     * Inits button for going back to main menu where you can pick to start a world builder again, load custom map and simulate/play
     */
    public void initGoToMenuButton() {
        this.goToMenuBut = new Button("Menu");
        goToMenuBut.setOnAction(e -> { // Switch to settings
            SettingsScene settingsScene = new SettingsScene(primaryStage);
            this.primaryStage.setTitle("Multi-Agent Surveillance Settings");
            this.primaryStage.setScene(settingsScene.getSettingsScene());
            this.primaryStage.show();
        });
        this.goToMenuBut.setWrapText(true);
    }

    /**
     * Inits button for restarting World Builder with the same settings
     */
    public void initRestartButton(){
        this.restartGameBut = new Button("Restart Game");
        restartGameBut.setOnAction(e -> { // Create a new game with the same setings
            WorldBuilder worldBuilder = new WorldBuilder(primaryStage, settings);
            this.primaryStage.setTitle("Multi-Agent-Surveillance Game");
            this.primaryStage.setScene(worldBuilder.getWorldBuilder());
            this.primaryStage.show();
        });
        this.restartGameBut.setWrapText(true);
    }

    /**
     * Inits button for saving the map you created
     */
    public void initSaveBoardButton(){
        this.saveBoardBut = new Button("Save this World Map");
        saveBoardBut.setOnAction(e -> { // Save current worldMap
            try {
                File recordsDir = new File(System.getProperty("user.home"), ".MultiAgentSurveillance/maps");
                if (!recordsDir.exists()) {
                    recordsDir.mkdirs();
                }
                String fileName = JOptionPane.showInputDialog(null,"Enter a file name for the current worldMap");
                //saveAsPng(grid, fileName + "IMG");
                FileOutputStream fileOutputStream = new FileOutputStream(new File(System.getProperty("user.home"), ".MultiAgentSurveillance/maps/" + fileName + ".dat"));
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
                objectOutputStream.writeObject(worldMap);
                objectOutputStream.flush();
                objectOutputStream.close();
            } catch (Exception err) {
                err.printStackTrace();
            }
        });
        this.saveBoardBut.setWrapText(true);
    }

    /**
     * Inits container for all elements seen on right side of screen
     */
    public void initButtonContainer(){
        this.buttonContainer = new VBox();
        VBox.setVgrow(goToMenuBut, Priority.ALWAYS);
        VBox.setVgrow(restartGameBut, Priority.ALWAYS);
        VBox.setVgrow(saveBoardBut, Priority.ALWAYS);
        VBox.setVgrow(tileTypeSelection, Priority.ALWAYS);
        goToMenuBut.setMaxHeight(Double.MAX_VALUE);
        restartGameBut.setMaxHeight(Double.MAX_VALUE);
        saveBoardBut.setMaxHeight(Double.MAX_VALUE);
        tileTypeSelection.setMaxHeight(Double.MAX_VALUE);
        goToMenuBut.setMaxWidth(Double.MAX_VALUE);
        restartGameBut.setMaxWidth(Double.MAX_VALUE);
        saveBoardBut.setMaxWidth(Double.MAX_VALUE);
        tileTypeSelection.setMaxWidth(Double.MAX_VALUE);
        buttonContainer.getChildren().addAll(goToMenuBut, restartGameBut, saveBoardBut,  tileTypeSelection);
    }

    /**
     * Creates full scene dispalyed
     */
    public void createMainScene(){
        bPane = new BorderPane();
        bPane.setCenter(grid);
        bPane.setRight(buttonContainer);
        scene = new Scene(bPane);
    }
}
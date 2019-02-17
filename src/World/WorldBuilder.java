package World;

import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import javax.swing.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static World.WorldMap.EMPTY;

/**
 * Main in game screen
 * @author Kailhan Hokstam
 */
public class WorldBuilder extends BorderPane {
    private Settings settings;
    private Stage primaryStage;
    private BorderPane bPane;
    private Scene scene;
    private WorldMap worldMap;
    private GridPane grid;
    private int windowSize = 1000;
    private int tileSize;

    private List<TileButton> toAdd = new ArrayList<>();

    private Image emptyTileImg;
    private Image structureTileImg;
    private Image doorTileImg;
    private Image windowTileImg;
    private Image targetTileImg;
    private Image sentryTileImg;
    private Image decreasedVisRangeTileImg;
    private Image wallTileImg;
    private Image[] tileImgArray;
    
    private Image bgrImg;

    private Button goToMenuBut;
    private Button restartGameBut;
    private Button loadBoardBut;
    private Button saveBoardBut;
    private ComboBox<String> tileTypeSelection;
    private int activeTile;
    private static final long timeForMoveInMs = 500;

    public WorldBuilder(Stage primaryStage, Settings settings) {
        this.grid = new GridPane();
        this.settings = settings;
        this.primaryStage = primaryStage;
        this.worldMap = new WorldMap(settings.getWorldMap());
        this.tileSize = windowSize / worldMap.getSize();

        this.emptyTileImg = new Image(new File("src/Assets/emptyTile.png").toURI().toString(), tileSize, tileSize, false, false, true);
        this.structureTileImg = new Image(new File("src/Assets/structureTile.png").toURI().toString(), tileSize, tileSize, false, false, true);
        this.doorTileImg = new Image(new File("src/Assets/doorTile.png").toURI().toString(), tileSize, tileSize, false, false, true);
        this.windowTileImg = new Image(new File("src/Assets/windowTile.png").toURI().toString(), tileSize, tileSize, false, false, true);
        this.targetTileImg = new Image(new File("src/Assets/targetTile.png").toURI().toString(), tileSize, tileSize, false, false, true);
        this.sentryTileImg = new Image(new File("src/Assets/sentryTile.png").toURI().toString(), tileSize, tileSize, false, false, true);
        this.decreasedVisRangeTileImg = new Image(new File("src/Assets/decreasedVisRangeTile.png").toURI().toString(), tileSize, tileSize, false, false, true);
        this.wallTileImg = new Image(new File("src/Assets/wallTile.png").toURI().toString(), tileSize, tileSize, false, false, true);
        this.tileImgArray = new Image[]{emptyTileImg, structureTileImg, doorTileImg, windowTileImg, targetTileImg, sentryTileImg, decreasedVisRangeTileImg, wallTileImg};
        this.bgrImg = new Image(new File("src/Assets/bgr.png").toURI().toString(), tileSize, tileSize, false, false, true);

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

        this.goToMenuBut = new Button("Menu");
        goToMenuBut.setOnAction(e -> { // Switch to settings
            SettingsScene settingsScene = new SettingsScene(primaryStage);
            Node source = (Node)e.getSource();
            Stage stage = (Stage)source.getScene().getWindow();
            stage.close();
            this.primaryStage = new Stage();
            this.primaryStage.setTitle("Multi-Agent Surveillance Settings");
            this.primaryStage.setScene(settingsScene.getSettingsScene());
            this.primaryStage.show();
        });
        this.goToMenuBut.setWrapText(true);

        this.restartGameBut = new Button("Restart Game");
        restartGameBut.setOnAction(e -> { // Create a new game with the same setings
            WorldBuilder worldBuilder = new WorldBuilder(primaryStage, settings);
            Node source = (Node)e.getSource();
            Stage stage = (Stage)source.getScene().getWindow();
            stage.close();
            this.primaryStage = new Stage();
            this.primaryStage.setTitle("Multi-Agent-Surveillance Game");
            this.primaryStage.setScene(worldBuilder.getWorldBuilder());
            this.primaryStage.show();
        });
        this.restartGameBut.setWrapText(true);

        this.saveBoardBut = new Button("Save this World Map");
        saveBoardBut.setOnAction(e -> { // Save current worldMap
            try {
                File recordsDir = new File(System.getProperty("user.home"), ".MultiAgentSurveillance/maps");
                if (!recordsDir.exists()) {
                    recordsDir.mkdirs();
                }
                String fileName = JOptionPane.showInputDialog(null,"Enter a file name for the current worldMap");
                FileOutputStream fileOutputStream = new FileOutputStream(new File(System.getProperty("user.home"), ".MultiAgentSurveillance/maps/" + fileName + ".txt"));
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
                objectOutputStream.writeObject(worldMap);
                objectOutputStream.flush();
                objectOutputStream.close();
            } catch (Exception err) {
                err.printStackTrace();
            }
        });
        this.saveBoardBut.setWrapText(true);

        grid.setGridLinesVisible(false);
        grid.setAlignment(Pos.CENTER);
        initTiles();
        redrawBoard();

        VBox vBox = new VBox();
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
        vBox.getChildren().addAll(goToMenuBut, restartGameBut, saveBoardBut,  tileTypeSelection);

        bPane = new BorderPane();
        bPane.setCenter(grid); //can directly create scene from grid if borderpane layout is not gonna be used
        bPane.setRight(vBox);
        scene = new Scene(bPane);
    }

    /**
     * Updates tiles and general information displayed in the actual game screen
     */
    public void redrawBoard (){
        grid.getChildren().clear();
        createTiles();
        grid.getChildren().addAll(toAdd);

        for(Node aNode: grid.getChildren()) {
            GridPane.setHalignment(aNode, HPos.CENTER);
        }
    }

    /**
     * Creates different kind of tiles depending on who owns a certain cell and how many disks will be flipped
     * if someone places a disk in that certain cell
     */
    public void createTiles(){
        for (int r = 0; r < worldMap.getSize(); r++) {
            for (int c = 0; c < worldMap.getSize(); c++) {
                if(toAdd.get(c + (r * worldMap.getSize())).getTileStatus() != worldMap.getTileState(r, c)) {
                    toAdd.set(c + (r * worldMap.getSize()), new TileButton(r, c, tileImgArray[worldMap.getTileState(r,c)], tileSize, worldMap.getTileState(r,c)));
                }
                toAdd.get(c + (r * worldMap.getSize())).setOnAction((event) -> {
                    TileButton button = (TileButton)event.getSource();
                    updateWorldMap(button.getX(), button.getY(), activeTile); // Actual communication with worldMap, says which button has been clicked and thus which worldMap cell needs to be checked
                });
                GridPane.setConstraints(toAdd.get(c + (r * worldMap.getSize())), r, c);
            }
        }
    }

    public void initTiles(){
        toAdd.clear();
        for (int r = 0; r < worldMap.getSize(); r++) {
            for (int c = 0; c < worldMap.getSize(); c++) {
                toAdd.add(new TileButton(r, c, tileImgArray[worldMap.getTileState(r,c)], tileSize, worldMap.getTileState(r,c)));
                System.out.println("r " + r + " c " + c);
                toAdd.get(c + (r * worldMap.getSize())).setOnAction((event) -> {
                    TileButton button = (TileButton)event.getSource();
                    updateWorldMap(button.getX(), button.getY(), activeTile); // Actual communication with worldMap, says which button has been clicked and thus which worldMap cell needs to be checked
                });
                GridPane.setConstraints(toAdd.get(toAdd.size()-1), r, c);
            }
        }
    }

    public void updateWorldMap(int r, int c, int state)
    {
        worldMap.updateTile(r, c, state);
        redrawBoard();
    }

    public Scene getWorldBuilder() {
        return scene;
    }
}


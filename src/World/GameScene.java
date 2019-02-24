package World;

import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import Agent.*;

/**
 * Main in game screen
 * @author Kailhan Hokstam
 */
public class GameScene extends BorderPane {
    private Settings settings;
    private Stage primaryStage;
    private HBox hBox;
    private Scene scene;
    private WorldMap worldMap;
    private GridPane grid;
    private int windowSize;
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

    private Button goToMenuBut;
    private Button restartGameBut;
    private Button startGameBut;

    private Group agentGroup = new Group();

    public GameScene(Stage primaryStage, Settings settings) {
        this.grid = new GridPane();
        this.windowSize = 1000;
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
        this.wallTileImg = new Image(new File("src/Assets/wallTile16.png").toURI().toString(), tileSize, tileSize, false, false, true);
        this.tileImgArray = new Image[]{emptyTileImg, structureTileImg, doorTileImg, windowTileImg, targetTileImg, sentryTileImg, decreasedVisRangeTileImg, wallTileImg};

        this.goToMenuBut = new Button("Menu");
        goToMenuBut.setOnAction(e -> { // Switch to settings
            SettingsScene settingsScene = new SettingsScene(primaryStage);
            Node source = (Node) e.getSource();
            Stage stage = (Stage) source.getScene().getWindow();
            stage.close();
            this.primaryStage = new Stage();
            this.primaryStage.setTitle("Multi-Agent Surveillance Settings");
            this.primaryStage.setScene(settingsScene.getSettingsScene());
            this.primaryStage.show();
        });
        this.goToMenuBut.setWrapText(true);

        this.restartGameBut = new Button("Restart Game");
        restartGameBut.setOnAction(e -> { // Create a new game with the same setings
            GameScene gameScene = new GameScene(primaryStage, settings);
            Node source = (Node) e.getSource();
            Stage stage = (Stage) source.getScene().getWindow();
            stage.close();
            this.primaryStage = new Stage();
            this.primaryStage.setTitle("Multi-Agent-Surveillance Game");
            this.primaryStage.setScene(gameScene.getGameScene());
            this.primaryStage.show();
        });
        this.restartGameBut.setWrapText(true);

        this.startGameBut = new Button("Start Game");
        startGameBut.setOnAction(e -> { //

        });
        this.startGameBut.setWrapText(true);

        redrawBoard();
        grid.setGridLinesVisible(true);
        grid.setAlignment(Pos.CENTER);

        VBox vBox = new VBox();
        VBox.setVgrow(goToMenuBut, Priority.ALWAYS);
        VBox.setVgrow(restartGameBut, Priority.ALWAYS);
        VBox.setVgrow(startGameBut, Priority.ALWAYS);
        goToMenuBut.setMaxHeight(Double.MAX_VALUE);
        restartGameBut.setMaxHeight(Double.MAX_VALUE);
        startGameBut.setMaxHeight(Double.MAX_VALUE);
        goToMenuBut.setMaxWidth(Double.MAX_VALUE);
        restartGameBut.setMaxWidth(Double.MAX_VALUE);
        startGameBut.setMaxWidth(Double.MAX_VALUE);
        vBox.getChildren().addAll(goToMenuBut, restartGameBut, startGameBut);

        hBox = new HBox();
        StackPane worldPane = new StackPane();
        worldPane.getChildren().addAll(grid, agentGroup);
        hBox.getChildren().addAll(worldPane, vBox); //can directly create scene from grid if borderpane layout is not gonna be used
        scene = new Scene(hBox);
        hBox.setMinSize(windowSize + windowSize * 0.1, windowSize);
    }

    /**
     * Updates tiles and general information displayed in the actual game screen
     */
    public void redrawBoard() {
        grid.getChildren().clear();
        createTiles();
        createAgents();
//        grid.getChildren().addAll(agentGroup);
        grid.setGridLinesVisible(true);
    }

    public void createTiles() {
        for (int r = 0; r < worldMap.getSize(); r++) {
            for (int c = 0; c < worldMap.getSize(); c++) {
                ImageView tmpImage = new ImageView(tileImgArray[worldMap.getTileState(r, c)]);
                tmpImage.setSmooth(false);
                grid.add((tmpImage), r, c);
            }
        }
    }

    public void createAgents() {
        agentGroup.getChildren().clear();
        for(Agent agent : worldMap.getAgents()) {
            if(agent instanceof Guard) {
                Guard guard = (Guard) agent;
                AgentCircle circle = new AgentCircle(guard.getPosition());
                circle.setFill(Color.PEACHPUFF);
                agentGroup.getChildren().add(new Pane(circle));
                System.out.println("Added guard");
            }
            if(agent instanceof Intruder) {
                Intruder intruder = (Intruder) agent;
                AgentCircle circle = new AgentCircle(intruder.getPosition());
                System.out.println(circle.getLayoutX());
                circle.setFill(Color.DARKRED);
                Pane tmpPane = new Pane();
                tmpPane.getChildren().addAll(circle);
                circle.setCenterX(230);
                circle.setCenterY(-100);
                agentGroup.getChildren().add(tmpPane);
                System.out.println("added agent");
            }
        }
    }

    public void updateWorldMap(int r, int c, int state) {
        worldMap.updateTile(r, c, state);
        redrawBoard();
    }

    public Scene getGameScene() {
        return scene;
    }
}
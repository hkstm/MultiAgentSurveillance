package World;

import javafx.animation.AnimationTimer;
import javafx.event.EventHandler;
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

import java.awt.*;
import java.awt.geom.Point2D;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import Agent.*;
import javafx.stage.WindowEvent;

/**
 * Main in game screen
 * @author Kailhan Hokstam
 */
public class GameScene extends BorderPane implements Runnable {

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

    private boolean gameStarted = false; //used for start and stop button

    public GameScene(Stage primaryStage, Settings settings) {
        this.grid = new GridPane(); //main grid that shows the tiles
        this.windowSize = 1000;
        this.settings = settings;
        this.primaryStage = primaryStage;
        this.primaryStage.setOnCloseRequest(we -> { //doesnt work properly right now, used to actually close threads when closing main window
            System.out.println("Stage is closing");
            worldMap.removeAllAgents();
            System.exit(0);
        });
        this.worldMap = new WorldMap(settings.getWorldMap()); //create world data structure
        this.tileSize = windowSize / worldMap.getSize();

        //load some assets
        this.emptyTileImg = new Image(new File("src/Assets/emptyTile.png").toURI().toString(), tileSize, tileSize, false, false, true);
        this.structureTileImg = new Image(new File("src/Assets/structureTile.png").toURI().toString(), tileSize, tileSize, false, false, true);
        this.doorTileImg = new Image(new File("src/Assets/doorTile.png").toURI().toString(), tileSize, tileSize, false, false, true);
        this.windowTileImg = new Image(new File("src/Assets/windowTile.png").toURI().toString(), tileSize, tileSize, false, false, true);
        this.targetTileImg = new Image(new File("src/Assets/targetTile.png").toURI().toString(), tileSize, tileSize, false, false, true);
        this.sentryTileImg = new Image(new File("src/Assets/sentryTile.png").toURI().toString(), tileSize, tileSize, false, false, true);
        this.decreasedVisRangeTileImg = new Image(new File("src/Assets/decreasedVisRangeTile.png").toURI().toString(), tileSize, tileSize, false, false, true);
        this.wallTileImg = new Image(new File("src/Assets/wallTile16.png").toURI().toString(), tileSize, tileSize, false, false, true);
        this.tileImgArray = new Image[]{emptyTileImg, structureTileImg, doorTileImg, windowTileImg, targetTileImg, sentryTileImg, decreasedVisRangeTileImg, wallTileImg};

        this.goToMenuBut = new Button("Menu"); //menu button on right side
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

        this.restartGameBut = new Button("Restart Game"); //restart button on right side
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

        this.startGameBut = new Button("Start/Stop Game"); //should stop and start game, not properly working atm
        startGameBut.setOnAction(e -> { //
            if(!gameStarted) {
                Agent.worldMap = worldMap;
                worldMap.addAgent(new Intruder(new Point2D.Double(-200, -50), 0));
                worldMap.startAgents();
                System.out.println("Started agents");
                new AnimationTimer() {
                    @Override
                    public void handle(long currentTime) {
                        redrawBoard();
                    }
                }.start();
                gameStarted = true;
            } else {
                worldMap.removeAllAgents();
                gameStarted = false;
            }

        });
        this.startGameBut.setWrapText(true);

        redrawBoard(); //redrawing board otherwise window that displays board and button is not properly sized

        //setting box that contains buttons
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

        hBox = new HBox(); //container that stores the box containing buttons and the stackpane with the world and the agents drawn ontop of it
        StackPane worldPane = new StackPane(); //allows us to drap agents on top of world
        worldPane.getChildren().addAll(grid, agentGroup);
        hBox.getChildren().addAll(worldPane, vBox); //can directly create scene from grid if borderpane layout is not gonna be used
        scene = new Scene(hBox); // allows us to actually display the world, agents and buttons
        hBox.setMinSize(windowSize + windowSize * 0.1, windowSize); //dont think


    }

    public void run(){
     redrawBoard();
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
        grid.setAlignment(Pos.CENTER);
    }

    public void createTiles() {
        for (int r = 0; r < worldMap.getSize(); r++) {
            for (int c = 0; c < worldMap.getSize(); c++) {
                //System.out.println("r" + r + "c" + c);
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
                Pane tmpPane = new Pane();
                tmpPane.getChildren().addAll(circle);
                agentGroup.getChildren().add(tmpPane);
            }
            if(agent instanceof Intruder) {
                Intruder intruder = (Intruder) agent;
                AgentCircle circle = new AgentCircle(intruder.getPosition());
                circle.setFill(Color.DARKRED);
                Pane tmpPane = new Pane();
                tmpPane.getChildren().addAll(circle);
                agentGroup.getChildren().add(tmpPane);
                //System.out.println("position" + intruder.getPosition().toString());
            }
            //System.out.println("proceeding after while loop, agent on seperate thread");
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
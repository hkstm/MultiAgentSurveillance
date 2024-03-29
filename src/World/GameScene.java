package World;

import javafx.animation.AnimationTimer;
import javafx.scene.CacheHint;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import javafx.geometry.Point2D;
import java.io.*;
import java.util.ArrayList;
import java.util.Random;
import Agent.*;

import static Agent.Agent.*;
import static World.StartWorldBuilder.WINDOW_SIZE;
import static World.WorldMap.EMPTY;
import static World.WorldMap.DECREASED_VIS_RANGE;

/**
 * Main in game screen
 * @author Kailhan
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
    private Image[] tileImgArray;
    private Button goToMenuBut;
    private Button restartGameBut;
    private Button startGameBut;
    private Group agentGroup = new Group();
    private ArrayList<TileView> tileViews = new ArrayList<>();

    private boolean gameStarted; //used for start and stop button
    private int mode; //modes for different gameModes e.g. multiple intruders/guards and what the end game conditions are
    public static final double SIMULATION_SPEEDUP_FACTOR = 1;
    public static final int ASSUMED_WORLDSIZE = 100;
    public static final double SCALING_FACTOR = WINDOW_SIZE/ASSUMED_WORLDSIZE; //ASSUMING WORLD IS ALWAYS 100 X 100 AND THAT WINDOWSIZE IS 1000
    public static Random random = new Random();
    private long currentTimeCountDown;
    private boolean countDown;
    private boolean visitedTarget;
    private long firstVisitTime;

    private boolean paused;
    private Pheromones pher;
    private boolean pherActive = true;

    public GameScene(Stage primaryStage, Settings settings) {
        this.grid = new GridPane(); //main grid that shows the tiles
        this.windowSize = WINDOW_SIZE;
        this.mode = 0;
        this.settings = settings;
        this.primaryStage = primaryStage;
        this.primaryStage.setOnCloseRequest(we -> { //doesnt work properly right now, used to actually close threads when closing main window
            System.out.println("Stage is closing");
            worldMap.removeAllAgents();
            System.exit(0);
        });
        this.worldMap = new WorldMap(settings.getWorldMap()); //create world data structure
        this.tileSize = windowSize / worldMap.getSize();
        this.gameStarted = false;

        initTileImgArray();
        initGoToMenuButton();
        initTiles();
        initRestartButton();

        this.startGameBut = new Button("Start/Stop Game"); //should stop and start game, not properly working atm
        Agent.worldMap = worldMap;

        int amountOfGuards = 20;
        //StraightLiner straightLiner = new StraightLiner(new Point2D(10, 10), 45);
        //worldMap.addAgent(straightLiner);
        Intruder intruder = new Intruder(getEntrancePoint(), 180);
        worldMap.addAgent(intruder);
        for(int n = 0; n < amountOfGuards; n++) {
            double x = 0;
            double y = 0;
            do{
                x = new Random().nextDouble()*worldMap.getSize()*SCALING_FACTOR;
                y = new Random().nextDouble()*worldMap.getSize()*SCALING_FACTOR;
            }while(!worldMap.isEmpty(worldMap.getTileState(locationToWorldgrid(y), locationToWorldgrid(x))));
            worldMap.addAgent(new StupidGuard(new Point2D(x, y), new Random().nextDouble()*360));
        }
        this.pher = new Pheromones(worldMap);

        //worldMap.addOnlyAgent(areaOptimzer);
        //Actual game "loop" in here
        startGameBut.setOnAction(e -> { //
            currentTimeCountDown = System.nanoTime();
            if(!gameStarted) {
                gameStarted = true;
//                worldMap.startAgents();
                System.out.println("Started agents");
                new AnimationTimer() {
                    long currentTimeCalc = System.nanoTime();
                    long previousTime = currentTimeCalc;
                    @Override
                    public void handle(long currentTime) {
                        if(gameStarted && !paused){
//                        long beforeUpdatingAgents = System.nanoTime();
                            worldMap.forceUpdateAgents();
//                        long afterUpdatingAgents = System.nanoTime();
//                        System.out.println("updating agentstook: " + ((afterUpdatingAgents-beforeUpdatingAgents)/1e9));

//                        long beforeDrawingBoard = System.nanoTime();
                            redrawBoard();
    //                        long afterDrawingBoard = System.nanoTime();
    //                        System.out.println("redrawing board took: " + ((afterDrawingBoard-beforeDrawingBoard)/1e9));


                            long delta = (currentTime - previousTime);
                            delta *= SIMULATION_SPEEDUP_FACTOR;
    //                        System.out.println("drawing tick in: " + (delta/1e9));
                            previousTime = currentTime;
                            pher.update(delta);
                            generateRandomSound(delta);
                            haveGuardsCapturedIntruder(mode, delta);
                            haveIntrudersWon(mode, delta);
//                        System.out.println();
                        }
                    }
                }.start();
            } else {
                gameStarted = false;
                worldMap.removeAllAgents();
                initRedrawBoard();
            }
        });

        this.startGameBut.setWrapText(true);

        initRedrawBoard(); //redrawing board otherwise window that displays board and button is not properly sized
        initFullScreen();
    }

    public void run(){initRedrawBoard();
    }

    public void initRedrawBoard(){
        grid.getChildren().clear();
        createTiles();
    }

    /**
     * Updates tiles and general information displayed in the actual game screen
     */
    public void redrawBoard() {
        grid.getChildren().clear();
        createTiles();
        createAgents();
        drawCones();
//        drawTileShapes();
        agentGroup.toFront();
    }

    public void drawCones() {
        worldMap.createCones();
//        Circle goal = new Circle(worldMap.getAgents().get(0).getGoalPosition().getX(), worldMap.getAgents().get(0).getGoalPosition().getY(), 10);
//        goal.setFill(Color.CORNFLOWERBLUE);
//        worldMap.getAgentsCones().add(goal);
//        Circle goalPath = new Circle(worldMap.getAgents().get(0).getGoalPositionPath().getX(), worldMap.getAgents().get(0).getGoalPositionPath().getY(), 5);
//        goal.setFill(Color.HOTPINK);
//        worldMap.getAgentsCones().add(goalPath);
//        Circle goalPrev = new Circle(worldMap.getAgents().get(0).getPrevGoalPosition().getX(), worldMap.getAgents().get(0).getPrevGoalPosition().getY(), 10);
//        goal.setFill(Color.LIGHTSEAGREEN);
//        worldMap.getAgentsCones().add(goalPrev);
        agentGroup.getChildren().addAll(worldMap.getAgentsCones());
    }

    public void drawTileShapes() {
        worldMap.createWorldGridShapes();
        agentGroup.getChildren().addAll(worldMap.getWorldGridShapes());
    }

    public void initTiles() {
        for (int r = 0; r < worldMap.getSize(); r++) {
            for (int c = 0; c < worldMap.getSize(); c++) {
                TileView tmpView = new TileView(tileImgArray[worldMap.getTileStatePhero(r, c)], r, c, worldMap.getTileStatePhero(r, c));
                tmpView.setCache(true);
                tmpView.setCacheHint(CacheHint.SPEED);
                tileViews.add(c + (r * worldMap.getSize()), tmpView);
                grid.add(tmpView, c, r);
                tileViews.set(c + (r * worldMap.getSize()),  new TileView(tileImgArray[worldMap.getTileStatePhero(r, c)], r, c, worldMap.getTileStatePhero(r, c)));
            }
        }
    }

    public void createTiles() {
        for (int r = 0; r < worldMap.getSize(); r++) {
            for (int c = 0; c < worldMap.getSize(); c++) {
                TileView tmpView = null;
                if(tileViews.get(c + (r * worldMap.getSize())).getState() != worldMap.getTileStatePhero(r, c)) {
                    tmpView = new TileView(tileImgArray[worldMap.getTileStatePhero(r, c)], r, c, worldMap.getTileStatePhero(r, c));
                } else {
                    tmpView = tileViews.get(c + (r * worldMap.getSize()));
                }
                tmpView.setCache(true);
                tmpView.setCacheHint(CacheHint.SPEED);
                tileViews.set(c + (r * worldMap.getSize()), tmpView);
                grid.add(tmpView, c, r);
            }
        }
    }

    /**
     * Intruder wins if they are 3 seconds in any of the target areas or visit the target area twice with a time difference of at least 3 seconds
     * @param mode might want to change logic depending on amount of targets and that could be switched using mode
     * @param delta timestep to calculate if time thresholdis met
     */
    public void haveIntrudersWon(int mode, long delta) {
        if(mode == 0) {
            boolean intrudersWon = false;
            if(!countDown) {
                currentTimeCountDown = System.nanoTime();
            }
            if(worldMap.intruderInTarget()) {
                //System.out.println("intruder in target");
                if(!visitedTarget) {
                    firstVisitTime = System.nanoTime();
                    visitedTarget = true;
                }
                if((System.nanoTime() - currentTimeCountDown) > (3*1e9/SIMULATION_SPEEDUP_FACTOR)) {
                    intrudersWon = true;
                }
                countDown = true;
            } else {
                countDown = false;
            }
            if(visitedTarget && (System.nanoTime() - firstVisitTime) > (3*1e9/SIMULATION_SPEEDUP_FACTOR)) {
                intrudersWon = true;
            }
            if(intrudersWon) {
                createAlert("INTRUDER has reached TARGET");
            }

        } else {
            System.out.println("other modes have not been implemented intruders cannot win");
        }

    }

    /**
     * Checks if guard are in range to "capture" intruder and if so they have won the game, multiple modes need to be added
     * e.g. if "all" intruders need to be caught or only 1
     */
    public void haveGuardsCapturedIntruder(int mode, long delta) {
        Agent[] agentGuards = worldMap.getAgents().toArray(new Agent[worldMap.getAgents().size()]);
        Agent[] agentIntruders = worldMap.getAgents().toArray(new Agent[worldMap.getAgents().size()]);
        for(Agent agentGuard : agentGuards) {
            if(agentGuard instanceof Guard) {
                for(Agent agentIntruder : agentIntruders) {
                    if(agentIntruder instanceof Intruder) {
                        if(agentGuard.getPosition().distance(agentIntruder.getPosition()) < (DISTANCE_TO_CATCH * SCALING_FACTOR)){
                            createAlert("GUARDS have found INTRUDER");
                        }
                    }
                }
            }
        }
    }


    /**
     * Random sound according to sort of poisson process (more binomial with low probability which should approximate it probs&stat stuff
     */
    public void generateRandomSound(long delta){
        double occurenceRate = 0.1/1e9; //because delta is in nano seconds
        occurenceRate *= (ASSUMED_WORLDSIZE/25); //map is ASSUMED_WORLDSIZE so ASSUMED_WORLDSIZE/25 times as big as 25
        if(random.nextDouble() < occurenceRate/(delta)) {
            Point2D randomNoiseLocation = new Point2D(random.nextInt(windowSize), random.nextInt(windowSize));
            for(Agent agent : worldMap.getAgents()) {
                if(randomNoiseLocation.distance(agent.getPosition()) < SOUNDRANGE_MEDIUMFAR * SCALING_FACTOR) {
                    double angleBetweenPoints = Math.toDegrees(Math.atan2((agent.getPosition().getY() - randomNoiseLocation.getY()), (agent.getPosition().getX() - randomNoiseLocation.getX())));
                    angleBetweenPoints += new Random().nextGaussian()*SOUND_NOISE_STDEV;
                    agent.getAudioLogs().add(new AudioLog(System.nanoTime(), angleBetweenPoints, new Point2D(agent.getPosition().getX(), agent.getPosition().getY())));
//                    System.out.println("Agent heard sound");
                }
            }
        }
    }

    private void createAlert(String s) {
        gameStarted = false;
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Game Finished");
        alert.setHeaderText(null);
        alert.setContentText(s);
        alert.show();
        goToMenuBut.fire();
    }

    /**
     * Actually add AgentCircles to list which is drawn
     */
    public void createAgents() {
        agentGroup.getChildren().clear();
//        AgentCircle circleTmp = new AgentCircle(new Point2D(1000, 1000));
//        circleTmp.setFill(Color.CORNFLOWERBLUE);
//        agentGroup.getChildren().add(circleTmp);
//        agentGroup.toFront();
        for(Agent agent : worldMap.getAgents()) {
            AgentCircle circle = new AgentCircle(agent);
            agentGroup.getChildren().add(circle);
            agentGroup.toFront();
            //System.out.println("proceeding after while loop, agent on seperate thread");
        }
    }

    /**
     * Sets (size) and creates all containers and combines them
     */
    public void initFullScreen() {
        //setting box that contains buttons
        VBox vBox = new VBox();

        VBox.setVgrow(goToMenuBut, Priority.ALWAYS);
        goToMenuBut.setMaxHeight(Double.MAX_VALUE);
        goToMenuBut.setMaxWidth(Double.MAX_VALUE);

        VBox.setVgrow(restartGameBut, Priority.ALWAYS);
        restartGameBut.setMaxHeight(Double.MAX_VALUE);
        restartGameBut.setMaxWidth(Double.MAX_VALUE);

        VBox.setVgrow(startGameBut, Priority.ALWAYS);
        startGameBut.setMaxHeight(Double.MAX_VALUE);
        startGameBut.setMaxWidth(Double.MAX_VALUE);
        vBox.getChildren().addAll(goToMenuBut, restartGameBut, startGameBut);
//        vBox.getChildren().addAll(goToMenuBut, startGameBut);


        hBox = new HBox(); //container that stores the box containing buttons and the stackpane with the world and the agents drawn ontop of it
        Pane worldPane = new Pane(); //allows us to draw agents on top of world
        worldPane.getChildren().addAll(grid, agentGroup);
        hBox.getChildren().addAll(worldPane, vBox);
        scene = new Scene(hBox); // allows us to actually display the world, agents and buttons
        hBox.setMinSize(windowSize + windowSize * 0.1, windowSize); //dont think this is done properly but it helps with sizing
        //hBox.setMinSize(windowSize, windowSize); //dont think this is done properly but it helps with sizing

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
        Image wallTileImg = new Image(new File("src/Assets/wallTile.png").toURI().toString(), tileSize, tileSize, false, false, true);
        Image openDoorTileImg = new Image(new File("src/Assets/openDoorTile.png").toURI().toString(), tileSize, tileSize, false, false, true);
        Image openWindowTileImg = new Image(new File("src/Assets/openWindowTile.png").toURI().toString(), tileSize, tileSize, false, false, true);
        Image marker1TileImg = new Image(new File("src/Assets/orangeTile.png").toURI().toString(), tileSize, tileSize, false, false, true);
        Image marker2TileImg = new Image(new File("src/Assets/greenTile.png").toURI().toString(), tileSize, tileSize, false, false, true);
        this.tileImgArray = new Image[]{emptyTileImg, structureTileImg, doorTileImg, windowTileImg, targetTileImg, sentryTileImg, decreasedVisRangeTileImg, wallTileImg,
                openDoorTileImg, openWindowTileImg, marker1TileImg, marker2TileImg};
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
            worldMap.removeAllAgents();
        });
        this.goToMenuBut.setWrapText(true);
    }

    /**
     * Inits button for restarting World Builder with the same settings
     */
    public void initRestartButton(){
//        this.restartGameBut = new Button("Restart Game");
        this.restartGameBut = new Button("Sort of pause");
        restartGameBut.setOnAction(e -> { // Create a new game with the same setings
//            WorldBuilder worldBuilder = new WorldBuilder(primaryStage, settings);
//            this.primaryStage.setTitle("Multi-Agent-Surveillance Game");
//            this.primaryStage.setScene(worldBuilder.getWorldBuilder());
//            this.primaryStage.show();
            paused = !paused;
        });
        this.restartGameBut.setWrapText(true);
    }

    public Scene getGameScene() {
        return scene;
    }

    public Point2D getEntrancePoint()
    {
        double xStart = 500;
        double yStart = 500;
        double random = Math.random();
        if(random < 0.25)
        {
            xStart = 985;
            yStart = Math.random()*((985-15))+15;
            if(worldMap.getWorldGrid()[Agent.locationToWorldgrid(xStart)][locationToWorldgrid(yStart)] !=   DECREASED_VIS_RANGE && worldMap.getWorldGrid()[Agent.locationToWorldgrid(xStart)][locationToWorldgrid(yStart)] != EMPTY)
            {
                if(yStart <= 500)
                {
                    while(true)
                    {
                        yStart += 10;
                        if(worldMap.getWorldGrid()[Agent.locationToWorldgrid(xStart)][locationToWorldgrid(yStart)] ==   DECREASED_VIS_RANGE || worldMap.getWorldGrid()[Agent.locationToWorldgrid(xStart)][locationToWorldgrid(yStart)] == EMPTY)
                        {
                            return new Point2D(xStart, yStart);
                        }
                        if(yStart > 985)
                        {
                            break;
                        }
                    }
                }
                else
                {
                    while(true)
                    {
                        yStart -= 10;
                        if(worldMap.getWorldGrid()[Agent.locationToWorldgrid(xStart)][locationToWorldgrid(yStart)] ==   DECREASED_VIS_RANGE || worldMap.getWorldGrid()[Agent.locationToWorldgrid(xStart)][locationToWorldgrid(yStart)] == EMPTY)
                        {
                            return new Point2D(xStart, yStart);
                        }
                        if(yStart < 15)
                        {
                            break;
                        }
                    }
                }
            }
            else
            {
                return new Point2D(xStart, yStart);
            }
        }
        if(random < 0.5)
        {
            xStart = Math.random()*((985-15))+15;
            yStart = 985;
            if(worldMap.getWorldGrid()[Agent.locationToWorldgrid(xStart)][locationToWorldgrid(yStart)] !=   DECREASED_VIS_RANGE && worldMap.getWorldGrid()[Agent.locationToWorldgrid(xStart)][locationToWorldgrid(yStart)] != EMPTY)
            {
                if(xStart <= 500)
                {
                    while(true)
                    {
                        xStart += 10;
                        if(worldMap.getWorldGrid()[Agent.locationToWorldgrid(xStart)][locationToWorldgrid(yStart)] ==   DECREASED_VIS_RANGE || worldMap.getWorldGrid()[Agent.locationToWorldgrid(xStart)][locationToWorldgrid(yStart)] == EMPTY)
                        {
                            return new Point2D(xStart, yStart);
                        }
                        if(xStart > 985)
                        {
                            break;
                        }
                    }
                }
                else
                {
                    while(true)
                    {
                        xStart -= 10;
                        if(worldMap.getWorldGrid()[Agent.locationToWorldgrid(xStart)][locationToWorldgrid(yStart)] ==   DECREASED_VIS_RANGE || worldMap.getWorldGrid()[Agent.locationToWorldgrid(xStart)][locationToWorldgrid(yStart)] == EMPTY)
                        {
                            return new Point2D(xStart, yStart);
                        }
                        if(xStart < 15)
                        {
                            break;
                        }
                    }
                }
            }
            else
            {
                return new Point2D(xStart, yStart);
            }
        }
        if(random < 0.75)
        {
            xStart = 15;
            yStart = Math.random()*((985-15))+15;
            if(worldMap.getWorldGrid()[Agent.locationToWorldgrid(xStart)][locationToWorldgrid(yStart)] !=   DECREASED_VIS_RANGE && worldMap.getWorldGrid()[Agent.locationToWorldgrid(xStart)][locationToWorldgrid(yStart)] != EMPTY)
            {
                if(yStart <= 500)
                {
                    while(true)
                    {
                        yStart += 10;
                        if(worldMap.getWorldGrid()[Agent.locationToWorldgrid(xStart)][locationToWorldgrid(yStart)] ==   DECREASED_VIS_RANGE || worldMap.getWorldGrid()[Agent.locationToWorldgrid(xStart)][locationToWorldgrid(yStart)] == EMPTY)
                        {
                            return new Point2D(xStart, yStart);
                        }
                        if(yStart > 985)
                        {
                            break;
                        }
                    }
                }
                else
                {
                    while(true)
                    {
                        yStart -= 10;
                        if(worldMap.getWorldGrid()[Agent.locationToWorldgrid(xStart)][locationToWorldgrid(yStart)] ==   DECREASED_VIS_RANGE || worldMap.getWorldGrid()[Agent.locationToWorldgrid(xStart)][locationToWorldgrid(yStart)] == EMPTY)
                        {
                            return new Point2D(xStart, yStart);
                        }
                        if(yStart < 15)
                        {
                            break;
                        }
                    }
                }
            }
            else
            {
                return new Point2D(xStart, yStart);
            }
        }
        if(random < 1)
        {
            xStart = Math.random()*((985-15))+15;
            yStart = 15;
            if(worldMap.getWorldGrid()[Agent.locationToWorldgrid(xStart)][locationToWorldgrid(yStart)] !=   DECREASED_VIS_RANGE && worldMap.getWorldGrid()[Agent.locationToWorldgrid(xStart)][locationToWorldgrid(yStart)] != EMPTY)
            {
                if(xStart <= 500)
                {
                    while(true)
                    {
                        xStart += 10;
                        if(worldMap.getWorldGrid()[Agent.locationToWorldgrid(xStart)][locationToWorldgrid(yStart)] ==   DECREASED_VIS_RANGE || worldMap.getWorldGrid()[Agent.locationToWorldgrid(xStart)][locationToWorldgrid(yStart)] == EMPTY)
                        {
                            return new Point2D(xStart, yStart);
                        }
                        if(xStart > 985)
                        {
                            break;
                        }
                    }
                }
                else
                {
                    while(true)
                    {
                        xStart -= 10;
                        if(worldMap.getWorldGrid()[Agent.locationToWorldgrid(xStart)][locationToWorldgrid(yStart)] ==   DECREASED_VIS_RANGE || worldMap.getWorldGrid()[Agent.locationToWorldgrid(xStart)][locationToWorldgrid(yStart)] == EMPTY)
                        {
                            return new Point2D(xStart, yStart);
                        }
                        if(xStart < 15)
                        {
                            break;
                        }
                    }
                }
            }
            else
            {
                return new Point2D(xStart, yStart);
            }
        }
        System.out.println("NO LEGAL STARTING POSITION FOUND, PLEASE EDIT TERRAIN");
        return new Point2D(xStart, yStart);
    }
}

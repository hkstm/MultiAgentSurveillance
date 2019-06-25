package Agent;

import World.Settings;
import World.WorldMap;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Point2D;
import javafx.scene.control.Button;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.Random;

import static Agent.Agent.*;
import static World.GameScene.ASSUMED_WORLDSIZE;
import static World.GameScene.SCALING_FACTOR;

public class RunSimulation extends Application {

    public static Random random = new Random();
    private WorldMap worldMap;
    private boolean countDown;
    private long currentTimeCountDown;
    private boolean visitedTarget;
    private long firstVisitTime;
    private File worldFile;

    private Pheromones pher;
    private boolean pherActive = true;



    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Multi-Agent Surveillance - Simulation Only");
        int amountOfSims = 100;
        worldFile = loadFile(primaryStage);
        for(int i = 0; i < amountOfSims; i++) {
            worldMap = loadMap(worldFile);
            Agent.worldMap = worldMap;
            int mode = 0;

            long currentTimeCalc = System.nanoTime();
            long previousTime = currentTimeCalc;
            boolean gameStarted = false;
            boolean gameEnded = false;
            currentTimeCountDown = System.nanoTime();
            countDown = false;
            visitedTarget = false;
            firstVisitTime = 0;
            Agent.worldMap = worldMap;
            Guard guard1  = new Guard(new Point2D(200, 450), 70);
            Guard guard2  = new Guard(new Point2D(500, 400), 100);
            Intruder intruder = new Intruder(new Point2D(500, 500), 0);
            AreaOptimizer areaOptimzer = new AreaOptimizer(new Point2D(500, 400), 0);
            //       Guard guard1  = new Guard(new Point2D(200, 300), 70);
//        Guard guard2  = new Guard(new Point2D(500, 100), 100);
            AreaOptimizer areaOptimizer = new AreaOptimizer(new Point2D(500, 400), 0);
            Guard stupidGuard = new StupidGuard(new Point2D(800, 300), 315);
            StraightLiner straightLiner = new StraightLiner(new Point2D(20, 20), -45);
//        worldMap.addAgent(guard);
//        worldMap.addOnlyAgent(areaOptimizer);
//        worldMap.addOnlyAgent(areaOptimizer);
//            worldMap.addOnlyAgent(stupidGuard);
            worldMap.addOnlyAgent(straightLiner);
            //Actual game "loop" in here
            this.pher = new Pheromones(worldMap);
            System.out.println("doing simulation");
            worldMap.startAgents();
            while(!gameEnded){
                long currentTime = System.nanoTime();
                worldMap.forceUpdateAgents();
                long delta = (currentTime - previousTime);
                delta /= 1e9;
//                if(delta > 0.02) System.out.println("delta: " + delta);
//                System.out.println();
                previousTime = currentTime;
                pher.update(delta);
                generateRandomSound(delta);
                if(haveGuardsCapturedIntruder(mode, delta) || haveIntrudersWon(mode, delta)) gameEnded = true;

            }
            System.out.println("game ended");
        }
        System.out.println("done");
    }

    public boolean haveIntrudersWon(int mode, long delta) {
        boolean intrudersWon = false;
        if(!countDown) {
            currentTimeCountDown = System.nanoTime();
        }
        if(worldMap.intruderInTarget()) {
            if(!visitedTarget) {
                firstVisitTime = System.nanoTime();
                visitedTarget = true;
            }
            if((System.nanoTime() - currentTimeCountDown) > (3*1e9)) {
                intrudersWon = true;
            }
            countDown = true;
        } else {
            countDown = false;
        }
        if(visitedTarget && (System.nanoTime() - firstVisitTime) > (3*1e9)) {
            intrudersWon = true;
        }
        return intrudersWon;
    }


    /**
     * Checks if guard are in range to "capture" intruder and if so they have won the game, multiple modes need to be added
     * e.g. if "all" intruders need to be caught or only 1
     */
    public boolean haveGuardsCapturedIntruder(int mode, long delta) {
        Agent[] agentGuards = worldMap.getAgents().toArray(new Agent[worldMap.getAgents().size()]);
        Agent[] agentIntruders = worldMap.getAgents().toArray(new Agent[worldMap.getAgents().size()]);
        for(Agent agentGuard : agentGuards) {
            if(agentGuard instanceof Guard) {
                for(Agent agentIntruder : agentIntruders) {
                    if(agentIntruder instanceof Intruder) {
                        if(agentGuard.getPosition().distance(agentIntruder.getPosition()) < (DISTANCE_TO_CATCH * SCALING_FACTOR)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }


    /**
     * Random sound according to sort of poisson process (more binomial with low probability which should approximate it probs&stat stuff
     */
    public void generateRandomSound(long delta){
        double occurenceRate = 0.1/1e9; //because delta is in nano seconds
        occurenceRate *= (ASSUMED_WORLDSIZE/25); //map is ASSUMED_WORLDSIZE so ASSUMED_WORLDSIZE/25 times as big as 25
        if(random.nextDouble() < occurenceRate/(delta)) {
            Point2D randomNoiseLocation = new Point2D(random.nextInt((int)(ASSUMED_WORLDSIZE*SCALING_FACTOR)), random.nextInt((int)(ASSUMED_WORLDSIZE*SCALING_FACTOR))); //prolly not right
            for(Agent agent : worldMap.getAgents()) {
                if(randomNoiseLocation.distance(agent.getPosition()) < SOUNDRANGE_MEDIUMFAR * SCALING_FACTOR) {
                    double soundDirection = Math.toDegrees(Math.atan2((agent.getPosition().getY() - randomNoiseLocation.getY()), (agent.getPosition().getX() - randomNoiseLocation.getX())));
                    soundDirection += new Random().nextGaussian()*SOUND_NOISE_STDEV;
                    agent.getAudioLogs().add(new AudioLog(System.nanoTime(), soundDirection, new Point2D(agent.getPosition().getX(), agent.getPosition().getY())));
//                    System.out.println("Agent heard sound");
                }
            }
        }
    }

    public File loadFile(Stage primaryStage){
        File recordsDir = new File(System.getProperty("user.home"), ".MultiAgentSurveillance/maps");
        if (!recordsDir.exists()) {
            recordsDir.mkdirs();
        }
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(recordsDir);
        fileChooser.setTitle("Open World Map File");
        //change this if you don't want to use dat files for some reason
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Text Files", "*.dat"));
//            if(fileChooser.showOpenDialog(primaryStage) != null)
        File selectedFile = fileChooser.showOpenDialog(primaryStage);
        return selectedFile;
    }

    public WorldMap loadMap(File worldFile) {
        FileInputStream fileInputStream = null;
        WorldMap worldMapSelection = null;
        try {
            fileInputStream = new FileInputStream(worldFile);
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            worldMapSelection = (WorldMap) objectInputStream.readObject();
            objectInputStream.close();
        } catch (Exception err) {
            err.printStackTrace();
        }
        return worldMapSelection;
    }
}

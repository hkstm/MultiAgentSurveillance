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

import static Agent.Agent.DISTANCE_TO_CATCH;
import static Agent.Agent.SOUND_NOISE_STDEV;
import static World.GameScene.ASSUMED_WORLDSIZE;
import static World.GameScene.SCALING_FACTOR;

public class RunSimulation extends Application {

    public static Random random = new Random();
    private WorldMap worldMap;
    private boolean countDown;
    private long currentTimeCountDown;
    private boolean visitedTarget;
    private long firstVisitTime;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("JavaFX App");
        int amountOfSims = 3;

        for(int i = 0; i < amountOfSims; i++) {

            worldMap = loadMap(primaryStage);
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

            Guard guard = new Guard(new Point2D(200, 300), 70);
            Intruder intruder = new Intruder(new Point2D(500, 300), 0);
            AreaOptimizer areaOptimzer = new AreaOptimizer(new Point2D(500, 400), 0);
//        worldMap.addAgent(guard);
//        worldMap.addAgent(intruder);
//        worldMap.addOnlyAgent(guard);
            worldMap.addOnlyAgent(intruder);
            worldMap.addOnlyAgent(areaOptimzer);
            //Actual game "loop" in here
            System.out.println("doing simulation");
            while(!gameEnded){
                long currentTime = System.nanoTime();
                worldMap.forceUpdateAgents();
                long delta = (currentTime - previousTime);
                previousTime = currentTime;
                generateRandomSound(delta);
                gameEnded = haveGuardsCapturedIntruder(mode, delta);
                if(!gameEnded) gameEnded = haveIntrudersWon(mode, delta);
            }
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
                if(randomNoiseLocation.distance(agent.getPosition())/SCALING_FACTOR < 5) {
                    double angleBetweenPoints = Math.toDegrees(Math.atan2((agent.getPosition().getY() - randomNoiseLocation.getY()), (agent.getPosition().getX() - randomNoiseLocation.getX())));
                    angleBetweenPoints += new Random().nextGaussian()*SOUND_NOISE_STDEV;
                    agent.getAudioLogs().add(new AudioLog(System.nanoTime(), angleBetweenPoints, new Point2D(agent.getPosition().getX(), agent.getPosition().getY())));
                    System.out.println("Agent heard sound");
                }
            }
        }
    }

    public WorldMap loadMap(Stage primaryStage){
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
        FileInputStream fileInputStream = null;
        WorldMap worldMapSelection = null;
        try {
            fileInputStream = new FileInputStream(selectedFile);
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            worldMapSelection = (WorldMap) objectInputStream.readObject();
            objectInputStream.close();
        } catch (Exception err) {
            err.printStackTrace();
        }
        return worldMapSelection;
    }
}

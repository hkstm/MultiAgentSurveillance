package Agent;
import World.GameScene;
import World.WorldMap;
import javafx.scene.paint.Color;

import javafx.geometry.Point2D;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static Agent.MoveTo.destX;
import static Agent.MoveTo.destY;
import static World.GameScene.SCALING_FACTOR;
import static World.WorldMap.SENTRY;
import static World.WorldMap.TARGET;


public class Guard extends Agent {

    /**
     * A subclass of Agent for the Guards with an internal map containing the starting positions of other guards and the terrain across the map
     * @author Benjamin, Thibaut, Kailhan
     */


    private double walkingSpeed = 1.4; //m/s


    Routine routine;
    protected double timeCost; //time until end in seconds
    protected double distanceCost; //meters moved
    protected double directCommsCost; //message size in "bytes"
    protected double indirectCommsCost; //number of markers placed;

    public Guard(Point2D position, double direction) {
        super(position, direction);
        this.timeCost = 0;
        this.distanceCost = 0;
        this.directCommsCost = 0;
        this.indirectCommsCost = 0;
        this.viewingAngle = 45;
//        this.viewingAngle = 60;
        this.visualRange[0] = 0;
        this.visualRange[1] = 8;
//        this.visualRange[1] = 20;
        this.color = Color.AZURE;
        Routine guard1 = Routines.sequence(
                Routines.moveTo(500,300)
                //Routines.chase(guard, GameScene.intruder)
                // Routines.wander(worldMap,this)
        );
        this.setRoutine(guard1);



        //this.knownTerrain = worldMap.getWorldGrid();
    }

    public void updatePerformanceCriteria() {
        timeCost += delta;
        distanceCost += previousPosition.distance(position);
    }

    /**
     * put your agent specific logic in this
    /*
     * This should be the structure of any bot but Im not sure how this bot fits into it -kailhan
     */

    public void run() {
        previousTime = System.nanoTime();
        previousPosition = new Point2D(position.getX(), position.getY());
        while(!exitThread) {
            executeAgentLogic();
        }
    }
    /**
     * Used instead of the run method if we want to manually control when the agent should update
     */
    public void forceUpdate() {
        if(firstRun) {
            previousTime = System.nanoTime();
            previousPosition = new Point2D(position.getX(), position.getY());
            routine.start();
            firstRun = false;
        }
        executeAgentLogic();
    }
    /**
     * Logic that gets executed every tick
     */
    public void executeAgentLogic() {
        updatePerformanceCriteria();
        currentTime = System.nanoTime();
        delta = currentTime - previousTime;
        delta /= 1e9; //makes it in seconds
        System.out.println("x: " + getPosition().getX() + "y: " + getPosition().getY());
        update();



    }

    /**
     * ^^^
     */

    public boolean equals(Object obj) {
        boolean equals = false;
        if (obj == null) return false;
        if (obj == this) return true;
        if (!(obj instanceof Guard)) return false;
        Guard o = (Guard) obj;
        if((o.direction == this.direction) && (o.position.equals(this.position))) equals = true;
        return equals;
    }
    public void updateVisualRange(){
        if (worldMap.coordinatesToCell(position) == SENTRY) { // row.e. guard in on a tower
           this.visualRange[0] = 2;
           this.visualRange[1] = 15;
           this.viewingAngle = 30;
        } else {
            this.visualRange[0] = 0;
            this.visualRange[1] = 6;
            this.viewingAngle = 45;

        }
    }

    public void openTower() {
        if (worldMap.coordinatesToCell(position) == SENTRY)
        {
            class OpenTower extends TimerTask
            {
                public void run()
                {
                    //worldMap.updateTile((int)position.getRow(), (int)position.getColumn(), SENTRY);
                    knownTerrain[(int)position.getX()][(int)position.getY()] = SENTRY;
                }
            }
            Timer timer = new Timer();
            TimerTask openTower = new OpenTower();

                timer.schedule(openTower, 3000);
            timer.cancel();
        }
    }
    public void update() {
        if (routine.getState() == null) {
            // hasn't started yet so we start it
            routine.start();
        }
        routine.act(this, worldMap);
    }
    public Routine getRoutine() {
        return routine;
    }

    public void setRoutine(Routine routine) {
        this.routine = routine;
    }
    public void gameTree(double timeStep) {
        rePath = false;
        if (tempWalls.size() > 0) {
            for (int i = 0; i < tempWalls.size(); i++) {
                knownTerrain[tempWalls.get(i).y][tempWalls.get(i).x] = worldMap.getWorldGrid()[tempWalls.get(i).y][tempWalls.get(i).y];
                int[][] phaseDetectionBlocks = aStarTerrain(knownTerrain);
                Astar phaseDetectionPathFinder = new Astar(knownTerrain[0].length, knownTerrain.length, (int) (position.getX() / SCALING_FACTOR), (int) (position.getY() / SCALING_FACTOR), (int) goalPosition.getX(), (int) goalPosition.getY(), phaseDetectionBlocks);
                List<Node> phaseDetectionPath = phaseDetectionPathFinder.findPath();
                for (int j = 0; j < phaseDetectionPath.size(); j++) {
                    if (phaseDetectionPath.get(j).row == tempWalls.get(i).y && phaseDetectionPath.get(j).column == tempWalls.get(i).x) {
                        knownTerrain[tempWalls.get(i).y][tempWalls.get(i).x] = 7;
                    } else {
                        tempWalls.remove(i);
                        break;
                    }
                }
            }
        }
        if (oldTempGoal != null) {
            checkChangedStatus();
        }
        oldTempGoal = tempGoal;
        int[][] blocks = aStarTerrain(worldMap.getWorldGrid());
        Astar pathMaker = new Astar(knownTerrain[0].length, knownTerrain.length, locationToWorldgrid(position.getX()),
                locationToWorldgrid(position.getY()), locationToWorldgrid(destX), locationToWorldgrid(destY), blocks);
        List<Node> path = pathMaker.findPath();

        System.out.println("destX : "+destX + " destY : " + destY);
        if (oldTempGoal != null) {
            wallPhaseDetection();
            if (rePath) {
                blocks = aStarTerrain(knownTerrain);
                pathMaker = new Astar(knownTerrain[0].length, knownTerrain.length, (int) (position.getX() / SCALING_FACTOR), (int) (position.getY() / SCALING_FACTOR), (int) goalPosition.getX(), (int) goalPosition.getY(), blocks);
                path = pathMaker.findPath();
            }

            if (!changed) {
                //System.out.println("not changed");

                if (path.size() > 0) {
                    tempGoal = new Point2D(worldMap.convertArrayToWorld(path.get(path.size() - 1).row) + worldMap.convertArrayToWorld(1) / 2, worldMap.convertArrayToWorld(path.get(path.size() - 1).column) + worldMap.convertArrayToWorld(1) / 2);
                } else {
                    System.out.println("path size: " + path.size() + "setting tempGoal to current position");
                    tempGoal = new Point2D(position.getX(), position.getY());
                }
            }
        }
//        cornerCorrection();
        double divisor = Math.abs(tempGoal.getY()-position.getY());
        double preDivisor = Math.abs(previousTempGoal.getY() - tempGoal.getY());
        if(divisor == 0)
        {
            divisor++;
            System.out.println("divisor is zero");
        } else if (preDivisor == 0) {
            preDivisor++;
            //System.out.println("preDivisor is zero");
        }
        double turnAngle = Math.toDegrees(Math.atan(Math.abs(tempGoal.getX() - position.getX()) / divisor));
        double previousAngle = Math.toDegrees(Math.atan(Math.abs(previousTempGoal.getX() - tempGoal.getX()) / preDivisor));
        double walkingDistance = (BASE_SPEED * SCALING_FACTOR * timeStep);

        /**
         * logic for turning, call updateDirection() for proper turning
         */
        if(tempGoal.getX() >= position.getX() && tempGoal.getY() <= position.getY())
        {
            updateDirection(turnAngle);
        }
        else if(tempGoal.getX() >= position.getX() && tempGoal.getY() > position.getY())
        {
            updateDirection(180-turnAngle);
        }
        else if(tempGoal.getX() < position.getX() && tempGoal.getY() > position.getY())
        {
            updateDirection(180+turnAngle);
        }
        else if(tempGoal.getX() < position.getX() && tempGoal.getY() <= position.getY())
        {
            updateDirection(360-turnAngle);
        }
        if(legalMoveCheck(walkingDistance))
        {
            move(walkingDistance);


        }
    }

}

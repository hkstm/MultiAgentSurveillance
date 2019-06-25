package Agent;
import javafx.scene.paint.Color;

import javafx.geometry.Point2D;

import javax.swing.*;
import java.awt.Point;
import java.util.Random;
import java.util.ArrayList;
import java.util.List;

import static World.GameScene.ASSUMED_WORLDSIZE;
import static World.GameScene.SCALING_FACTOR;
import static World.WorldMap.*;

/**
 * A subclass of Agent for the Intruders
 * @author Benjamin, Kailhan
 */

public class Intruder extends Agent{
    protected boolean tired;
    protected final long createdMillis = System.currentTimeMillis();
    protected int sprintCounter = 5;
    protected int walkCounter = 15;
    protected List<Point> tempWalls = new ArrayList<Point>();
    private Point tempOldPos = null;
    private Point oldPos = null;
    private boolean first;
    private int alternatingCounter;
    private boolean modify;
    private double runTime = 5;
    private double walkTime = 10;



    /**
     * An Intruder constructor with an empty internal map
     * @param position is a point containing the coordinates of the Intruderq
     * @param direction is the angle which the agent is facing, this spans from -180 to 180 degrees
     */

    public Intruder(Point2D position, double direction) {
        super(position, direction);
        this.viewingAngle = 45;
        this.visualRange[0] = 0;
        this.visualRange[1] = 7.5;
        this.color = Color.LIGHTGOLDENRODYELLOW;
        this.tired = false;
    }

    public boolean equals(Object obj) {
        boolean equals = false;
        if (obj == null) return false;
        if (obj == this) return true;
        if (!(obj instanceof Guard)) return false;
        Intruder o = (Intruder) obj;
        if((o.direction == this.direction) && (o.position.equals(this.position)) && (o.tired == this.tired)) equals = true;
        return equals;
    }

    /**
     * A method for going through a window or door. It is important the the intruder is standing on the tile to be changed.
     * The time taken is consistent (3 seconds for a window and 5 for a door), unless a door is to be opened quietly, in which case a normal distribution is used.
     */

    public void executeAgentLogic() {
        try {
            gameTreeIntruder(delta);
        }
        catch(Exception e) {
            //do something here :D
        }
    }

    public void gameTreeIntruder(double timeStep)
    {
        double walkingDistance = (BASE_SPEED *SCALING_FACTOR*timeStep);
        double sprintingDistance = (SPRINT_SPEED *SCALING_FACTOR*timeStep);
        updateWalls();
        if(!frozen)
        {
            open();
        }
        if(oldTempGoal != null)
        {
            checkChangedStatus();
        }
        double elapsedTime = (System.currentTimeMillis()-startTime)/1000;
        if(elapsedTime > freezeTime)
        {
            frozen = false;
            startTime = 0;
            freezeTime = 0;
            oldTempGoal = tempGoal;
            int[][] blocks = aStarTerrain(knownTerrain);
            Astar pathFinder = new Astar(knownTerrain[0].length, knownTerrain.length, (int)(position.getX()/SCALING_FACTOR), (int)(position.getY()/SCALING_FACTOR), (int)goalPosition.getX(), (int)goalPosition.getY(), blocks, this, modify);
            List<Node> path = pathFinder.findPath();
            if(!changed)
            {
                tempGoal = new Point2D((path.get(path.size()-1).row*SCALING_FACTOR)+(SCALING_FACTOR/2), (path.get(path.size()-1).column*SCALING_FACTOR)+(SCALING_FACTOR/2));
                if (path.size() > 1) {
                    previousTempGoal = new Point2D((path.get(path.size() - 2).row * SCALING_FACTOR) + (SCALING_FACTOR / 2), (path.get(path.size() - 2).column * SCALING_FACTOR) + (SCALING_FACTOR / 2));
                }
                else{
                    previousTempGoal = tempGoal;
                }
            }
            wallPhaseDetection();
            cornerCorrection();
            double divisor = Math.abs(tempGoal.getY()-position.getY());
            double preDivisor = Math.abs(previousTempGoal.getY()-tempGoal.getY());
            if(divisor == 0)
            {
                divisor++;
            }
            else if (preDivisor == 0){
                preDivisor++;
            }
            double turnAngle = Math.toDegrees(Math.atan(Math.abs(tempGoal.getX()-position.getX())/divisor));
            performTurn(turnAngle);
            if(oldPos == null)
            {
                tempOldPos = new Point((int)(position.getX()/SCALING_FACTOR), (int)(position.getY()/SCALING_FACTOR));
                first = true;
            }
            else
            {
                tempOldPos = new Point((int)(position.getX()/SCALING_FACTOR), (int)(position.getY()/SCALING_FACTOR));
                first = false;
            }
            int wallCount = 0;
            double sprintPercent = 0;
            double random = Math.random();
            int rangeYTop = 2;
            int rangeXLeft = 2;
            int rangeXRight = 2;
            int rangeYButtom = 2;
            //System.out.println("x: " + locationToWorldgrid(position.getX()) + "y: " + locationToWorldgrid(position.getY()));
            if(locationToWorldgrid(position.getX()) - 2 < 0 ){
                rangeXLeft = 1;
            }else if (locationToWorldgrid(position.getY()) - 2 < 0){
                rangeYTop = 1;
            }else if (locationToWorldgrid(position.getX()) + 2 > knownTerrain.length){
                rangeXRight = 1;
            }else if (locationToWorldgrid(position.getY()) + 2 > knownTerrain[0].length){
                rangeYButtom = 1;
            }
            for (int r = locationToWorldgrid(position.getY()) - rangeYTop; r < locationToWorldgrid(position.getY()) + rangeYButtom; r++) {
                for (int c = locationToWorldgrid(position.getX()) - rangeXLeft; c < locationToWorldgrid(position.getX()) + rangeXRight; c++) {
                    // System.out.println(knownTerrain[r][c]);
                    if (knownTerrain[r][c] == WALL) {
                        wallCount++;
                    }
                }
            }
            if (wallCount == 0) {
                sprintPercent = 1.0;
            } else if (wallCount < 5) {
                sprintPercent = 0.9;
            } else if (wallCount > 5 && wallCount < 10) {
                sprintPercent = 0.5;
            } else if (wallCount > 10) {
                sprintPercent = 0.0;
            }
            //  System.out.println("wall cout" + wallCount);
//            System.out.println("sprint percent" + sprintPercent);
            for (Agent agent : worldMap.getAgents()) {
                if (agent instanceof Guard && this.inVision(agent.getPosition()) && !tired) {
                    updateDirection(direction + 180);
                    if (legalMoveCheck(sprintingDistance)) {
                        runTime -= timeStep;
                        move(sprintingDistance);
                        if (runTime < 0){
                            tired = true;
                            runTime = 5;
                        }
                    }
                }
                else if (agent instanceof Guard && this.inVision(agent.getPosition()) && tired){
                    updateDirection(direction + 180);
                    if (legalMoveCheck(walkingDistance)){
                        walkTime -= timeStep;
                        move(walkingDistance);
                        if (walkTime < 0){
                            tired = false;
                            walkTime = 10;
                        }
                    }
                }
                else if (random <= sprintPercent && !tired) {
                    if (legalMoveCheck(sprintingDistance)) {
                        runTime = runTime - timeStep;
                        // System.out.println("sprint time" + runTime);
                        move(sprintingDistance);
                        // System.out.println("sprint percent: " + sprintPercent + "tired?: " + tired + "im sprinting");
                        if (runTime < 0) {
                            tired = true;
                            runTime = 5;
                        }
                    }
                } else if (tired) {
                    if (legalMoveCheck(walkingDistance)) {
                        walkTime -= timeStep;
                        //  System.out.println("walk time" + walkTime);
                        // System.out.println("sprint percent: " + sprintPercent + "tired?: " + tired + "im walking");
                        move(walkingDistance);
                        if (walkTime < 0) {
                            tired = false;
                            walkTime = 10;
                        }
                    }
                } else if (this.inVision(goalPosition) && !tired) {
                    move(walkingDistance);
                } else {
                    if (legalMoveCheck(walkingDistance)) {
                        //  System.out.println("sprint percent: " + sprintPercent + "tired?: " + tired + "im walking");
                        move(walkingDistance);
                    }
                }
            }
            modify = false;
            if(tempOldPos.x != (int)(position.getX()/SCALING_FACTOR) || tempOldPos.y != (int)(position.getY()/SCALING_FACTOR))
            {
                if(!first && oldPos.x == (int)(position.getX()/SCALING_FACTOR) && oldPos.y == (int)(position.getY()/SCALING_FACTOR))
                {
                    alternatingCounter++;
                }
                else
                {
                    alternatingCounter = 0;
                }
            }
            if(alternatingCounter == 4)
            {
                System.out.println("modify");
                alternatingCounter = 0;
                modify = true;
                points[0] = oldPos;
                points[1] = tempOldPos;
            }
            oldPos = tempOldPos;
            if(audioLogs.size() > 0)
            {
                modify = true;
            }
        }
    }

    public void open()
    {
        //open door
        if(worldMap.worldGrid[(int)(position.getY()/SCALING_FACTOR)][(int)(position.getX()/SCALING_FACTOR)] == DOOR)
        {
            worldMap.updateTile(locationToWorldgrid(position.getY()), locationToWorldgrid(position.getX()), OPEN_DOOR);
            knownTerrain[(int)(position.getY()/SCALING_FACTOR)][(int)(position.getX()/SCALING_FACTOR)] = 0;
            Random random = new Random();
            startTime = System.currentTimeMillis();
            if(Math.sqrt((Math.pow(goalPosition.getX()-position.getX(), 2))+(Math.pow(goalPosition.getY()-position.getY(), 2))) < 200)
            {
                freezeTime = (random.nextGaussian()*2+12);
            }
            else
            {
                freezeTime = 5;
                doorNoise = true;
            }
            frozen = true;
        }
        //go through window
        else if(worldMap.worldGrid[(int)(position.getY()/SCALING_FACTOR)][(int)(position.getX()/SCALING_FACTOR)] == WINDOW)
        {
            startTime = System.currentTimeMillis();
            worldMap.updateTile(locationToWorldgrid(position.getY()), locationToWorldgrid(position.getX()), OPEN_WINDOW);
            freezeTime = 3;
            frozen = true;
        }
    }

    public Point[] getPoints()
    {
        return points;
    }

    public int[][] aStarTerrain(int[][] terrain) {
        List<Point> walls = new ArrayList<Point>();
        for(int i = 0; i < terrain.length; i++)
        {
            for(int j = 0; j < terrain[0].length; j++)
            {
                if(terrain[i][j] == STRUCTURE || terrain[i][j] == SENTRY || terrain[i][j] == WALL)
                {
                    Point wall = new Point(i, j);
                    walls.add(wall);
                }
            }
        }
        int[][] blocks = new int[walls.size()][2];
        for(int i = 0; i < walls.size(); i++)
        {
            blocks[i][0] = (int)walls.get(i).getY();
            blocks[i][1] = (int)walls.get(i).getX();
        }
        return blocks;
    }
}


package Agent;
import javafx.scene.paint.Color;

import javafx.geometry.Point2D;
import java.awt.Point;
import java.util.Random;
import java.util.ArrayList;
import java.util.List;
import static World.GameScene.SCALING_FACTOR;
import static World.WorldMap.EMPTY;
import static World.WorldMap.WALL;
import static World.WorldMap.DOOR;
import static World.WorldMap.OPEN_DOOR;
import static World.WorldMap.WINDOW;
import static World.WorldMap.OPEN_WINDOW;
import static World.WorldMap.SENTRY;
import static World.WorldMap.STRUCTURE;

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
    private boolean inDanger;



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
        inDanger = false;
        double walkingDistance = (BASE_SPEED *SCALING_FACTOR*timeStep);
        double sprintingDistance = (SPRINT_SPEED *SCALING_FACTOR*timeStep);
        if(!inDanger)
        {
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
                double previousAngle = Math.toDegrees(Math.atan(Math.abs(previousTempGoal.getX()-tempGoal.getX())/preDivisor));
                double finalAngle = previousAngle - turnAngle;
                if(tempGoal.getX() >= position.getX() && tempGoal.getY() <= position.getY())
                {
                    turnToFace(turnAngle-90);
                }
                else if(tempGoal.getX() >= position.getX() && tempGoal.getY() > position.getY())
                {
                    turnToFace(90-turnAngle);
                }
                else if(tempGoal.getX() < position.getX() && tempGoal.getY() > position.getY())
                {
                    turnToFace(90+turnAngle);
                }
                else if(tempGoal.getX() < position.getX() && tempGoal.getY() <= position.getY())
                {
                    turnToFace(270-turnAngle);
                }
                if(oldPos == null)
                {
                    tempOldPos = new Point((int)(position.getX()/SCALING_FACTOR), (int)(position.getY()/SCALING_FACTOR));
                    first = true;
                }
                else if (preDivisor == 0){
                    preDivisor++;
                }
                if(tempGoal.getX() >= position.getX() && tempGoal.getY() <= position.getY())
                {
                    tempOldPos = new Point((int)(position.getX()/SCALING_FACTOR), (int)(position.getY()/SCALING_FACTOR));
                    first = false;
                }
                int wallCount = 0;
                int sprintPercent = 0;
//            for (int i = locationToWorldgrid(position.getX() - 2 ); i < locationToWorldgrid(position.getX() + 2 ); i++){
//                for (int j = locationToWorldgrid(position.getY() - 2); j < locationToWorldgrid(position.getY() + 2); j++){
//                    if (knownTerrain[i][j] == WALL){
//                        wallCount++;
//                    }
//                    if (wallCount > 10 ){
//                        sprintPercent = Math.random()
//                    }
//                }
//            }
                if(!tired)
                {

                    if(legalMoveCheck(sprintingDistance))
                    {
                        long nowMillis = System.currentTimeMillis();
                        int countSec = (int)((nowMillis - this.createdMillis) / 1000);
                        if (countSec != sprintCounter){
                            move(sprintingDistance);
                        }
                        else{
                            tired = true;
                            sprintCounter = sprintCounter + 15;
                        }
                    }
                }
                if (tired)
                {
                    if(legalMoveCheck(walkingDistance))
                    {
                        long nowMillis = System.currentTimeMillis();
                        int countSec = (int)((nowMillis - this.createdMillis) / 1000);
                        if (countSec != walkCounter) {
                            move(walkingDistance);
                        }
                        else{
                            tired = false;
                            walkCounter += 15;
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
                if(alternatingCounter == 6)
                {
                    alternatingCounter = 0;
                    modify = true;
                    points[0] = oldPos;
                    points[1] = tempOldPos;
                }
                oldPos = tempOldPos;
            }
        }
    }

    public void open()
    {
        //open door
        if(worldMap.worldGrid[(int)(position.getY()/SCALING_FACTOR)][(int)(position.getX()/SCALING_FACTOR)] == DOOR)
        {
            worldMap.worldGrid[(int)(position.getY()/SCALING_FACTOR)][(int)(position.getX()/SCALING_FACTOR)] = OPEN_DOOR;
            worldMap.updateTile(locationToWorldgrid(position.getY()), locationToWorldgrid(position.getX()), OPEN_DOOR);
            knownTerrain[(int)(position.getY()/SCALING_FACTOR)][(int)(position.getX()/SCALING_FACTOR)] = 0;
            Random random = new Random();
            startTime = System.currentTimeMillis();
            if(Math.random() > 0.5)
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
            worldMap.worldGrid[(int)(position.getY()/SCALING_FACTOR)][(int)(position.getX()/SCALING_FACTOR)] = OPEN_WINDOW;
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

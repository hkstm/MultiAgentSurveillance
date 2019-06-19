package Agent;
import javafx.scene.paint.Color;

import javafx.geometry.Point2D;
import java.awt.Point;
import java.util.Random;
import java.util.ArrayList;
import java.util.List;
import static World.GameScene.SCALING_FACTOR;
import static World.WorldMap.EMPTY;

/**
 * A subclass of Agent for the Intruders
 * @author Benjamin, Kailhan
 */

public class Intruder extends Agent{
    protected boolean tired;
    protected double startTime = 0;
    protected static Point2D oldTempGoal;
    protected static Point2D tempGoal;
    protected static Point2D previousTempGoal;
    protected double freezeTime = 0;
    protected boolean frozen = false;
    protected static boolean changed = false;
    protected final long createdMillis = System.currentTimeMillis();
    protected int sprintCounter = 5;
    protected int walkCounter = 10; //check if this is right (might be 10 sec not 15)
    protected List<Point> tempWalls = new ArrayList<Point>();
    //protected boolean rePath = false;
    private boolean pathChanged= false;
    private boolean reAdded = false;


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
        //System.out.println("goal position: "+goalPosition);
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
            System.out.println("on target");
            //System.out.println("pls fix when intruder is on target");
        }
    }

    public void gameTreeIntruder(double timeStep)
    {
        //System.out.println("2");
        //System.out.println();
        //printKnownTerrain();
        //TODO fix corner issue
        //TODO check for guards
        //TODO make noise
        //TODO add weights to flags and other types of squares, try manually an possibly with a genetic algorithm
        double walkingDistance = (BASE_SPEED *SCALING_FACTOR*timeStep);
        double sprintingDistance = (SPRINT_SPEED *SCALING_FACTOR*timeStep);
        if(tempWalls.size() > 0)
        {
            for(int i = 0 ; i < tempWalls.size() ; i++)
            {
                reAdded = false;
                //knownTerrain[tempWalls.get(i).y][tempWalls.get(i).x] = worldMap.getWorldGrid()[tempWalls.get(i).y][tempWalls.get(i).y];
                int[][] phaseDetectionBlocks = aStarTerrain(knownTerrain);
                Astar phaseDetectionPathFinder = new Astar(knownTerrain[0].length, knownTerrain.length, (int)(position.getX()/SCALING_FACTOR), (int)(position.getY()/SCALING_FACTOR), (int)goalPosition.getX(), (int)goalPosition.getY(), phaseDetectionBlocks);
                List<Node> phaseDetectionPath = phaseDetectionPathFinder.findPath();
                for(int j = 0 ; j < phaseDetectionPath.size() ; j++)
                {
                    //System.out.println("size: "+phaseDetectionPath.size()+" j: "+j+" column: "+phaseDetectionPath.get(j).column+" row: "+phaseDetectionPath.get(j).row);
                    if(phaseDetectionPath.get(j).row == tempWalls.get(i).y && phaseDetectionPath.get(j).column == tempWalls.get(i).x)
                    {
                        System.out.println("wall re-added");
                        knownTerrain[tempWalls.get(i).y][tempWalls.get(i).x] = 7;
                        reAdded = true;
                        break;
                    }
                }
                if(!reAdded)
                {
                    tempWalls.remove(i);
                }
            }
        }
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

            //    long nowMillis = System.currentTimeMillis();
            //    int countSec = (int)((nowMillis - this.blindMillis) / 1000);
            //rePath = false;
            oldTempGoal = tempGoal;
            int[][] blocks = aStarTerrain(knownTerrain);
            Astar pathFinder = new Astar(knownTerrain[0].length, knownTerrain.length, (int)(position.getX()/SCALING_FACTOR), (int)(position.getY()/SCALING_FACTOR), (int)goalPosition.getX(), (int)goalPosition.getY(), blocks);
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
            //if(oldTempGoal != null)
            //{
            //    wallPhaseDetection();
            //    if(!rePath)
            //    {
            //        cornerCorrection();
                    //System.out.println("corner cut detected, corrected to:");
                    //System.out.println("position "+position);
                    //System.out.println("temporary goal: "+tempGoal);
                    //System.out.println();
            //    }
            //}
            wallPhaseDetection();
            cornerCorrection();
            double divisor = Math.abs(tempGoal.getY()-position.getY());
            double preDivisor = Math.abs(previousTempGoal.getY()-tempGoal.getY());
            if(divisor == 0)
            {
                divisor++;
                System.out.println("divisor is zero");
            }
            else if (preDivisor == 0){
                preDivisor++;
                //System.out.println("preDivisor is zero");
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
                        walkCounter += 15; //HAO please check if theses are the correct resting times
                    }
                }
            }
        }
    }

    public void cornerCorrection()
    {
        if(oldTempGoal.getX()+10 == tempGoal.getX() && oldTempGoal.getY()-10 == tempGoal.getY())
        {
            if(isObstruction((int)(oldTempGoal.getY()/SCALING_FACTOR), (int)((oldTempGoal.getX()+10)/SCALING_FACTOR)))
            {
                tempGoal = new Point2D(oldTempGoal.getX(), oldTempGoal.getY()-10);
                changed = true;
            }
            else if(isObstruction((int)((oldTempGoal.getY()-10)/SCALING_FACTOR), (int)(oldTempGoal.getX()/SCALING_FACTOR)))
            {
                tempGoal =  new Point2D(oldTempGoal.getX()+10, oldTempGoal.getY());
                changed = true;
            }
        }
        else if(oldTempGoal.getX()-10 == tempGoal.getX() && oldTempGoal.getY()+10 == tempGoal.getY())
        {
            if(isObstruction((int)((oldTempGoal.getY()+10)/SCALING_FACTOR), (int)(oldTempGoal.getX()/SCALING_FACTOR)))
            {
                tempGoal = new Point2D(oldTempGoal.getX()-10, oldTempGoal.getY());
                changed = true;
            }
            else if(isObstruction((int)(oldTempGoal.getY()/SCALING_FACTOR), (int)((oldTempGoal.getX()-10)/SCALING_FACTOR)))
            {
                tempGoal = new Point2D(oldTempGoal.getX(), oldTempGoal.getY()+10);
                changed = true;
            }
        }
        else if(oldTempGoal.getX()+10 == tempGoal.getX() && oldTempGoal.getY()+10 == tempGoal.getY())
        {
            if(isObstruction((int)((oldTempGoal.getY()+10)/SCALING_FACTOR), (int)(oldTempGoal.getX()/SCALING_FACTOR)))
            {
                tempGoal = new Point2D(oldTempGoal.getX()+10, oldTempGoal.getY());
                changed = true;
            }
            else if(isObstruction((int)(oldTempGoal.getY()/SCALING_FACTOR), (int)((oldTempGoal.getX()+10)/SCALING_FACTOR)))
            {
                tempGoal = new Point2D(oldTempGoal.getX(), oldTempGoal.getY()+10);
                changed = true;
            }
        }
        else if(oldTempGoal.getX()-10 == tempGoal.getX() && oldTempGoal.getY()-10 == tempGoal.getY())
        {
            if (isObstruction((int)(oldTempGoal.getY()/SCALING_FACTOR), (int)((oldTempGoal.getX()-10)/SCALING_FACTOR)))
            {
                tempGoal = new Point2D(oldTempGoal.getX(), oldTempGoal.getY()-10);
                changed = true;
            }
            else if (isObstruction((int)((oldTempGoal.getY()-10)/SCALING_FACTOR), (int)(oldTempGoal.getX()/SCALING_FACTOR)))
            {
                tempGoal = new Point2D(oldTempGoal.getX()-10, oldTempGoal.getY());
                changed = true;
            }
        }
    }

    public void wallPhaseDetection()
    {
        pathChanged = false;
        if(oldTempGoal.getX()+10 == tempGoal.getX() && oldTempGoal.getY()-10 == tempGoal.getY() && isObstruction((int) (oldTempGoal.getY() / SCALING_FACTOR), (int) ((oldTempGoal.getX() + 10) / SCALING_FACTOR)) && isObstruction((int) ((oldTempGoal.getY() - 10) / SCALING_FACTOR), (int) (oldTempGoal.getX() / SCALING_FACTOR)))
        {
            //System.out.println("phase detection case 1");
            Point tempWall = new Point((int) (tempGoal.getX() / SCALING_FACTOR), (int) (tempGoal.getY() / SCALING_FACTOR));
            tempWalls.add(tempWall);
            knownTerrain[(int) tempWall.getY()][(int) tempWall.getX()] = 7;
            //rePath = true;
            pathChanged = true;
            updatePath();
        }
        if(oldTempGoal.getX()-10 == tempGoal.getX() && oldTempGoal.getY()+10 == tempGoal.getY() && isObstruction((int)((oldTempGoal.getY()+10)/SCALING_FACTOR), (int)(oldTempGoal.getX()/SCALING_FACTOR)) && isObstruction((int)(oldTempGoal.getY()/SCALING_FACTOR), (int)((oldTempGoal.getX()-10)/SCALING_FACTOR)))
        {
            //System.out.println("phase detection case 2");
            Point tempWall = new Point((int) (tempGoal.getX() / SCALING_FACTOR), (int) (tempGoal.getY() / SCALING_FACTOR));
            tempWalls.add(tempWall);
            knownTerrain[(int) tempWall.getY()][(int) tempWall.getX()] = 7;
            //rePath = true;
            pathChanged = true;
            updatePath();
        }
        if(oldTempGoal.getX()+10 == tempGoal.getX() && oldTempGoal.getY()+10 == tempGoal.getY() && isObstruction((int)((oldTempGoal.getY()+10)/SCALING_FACTOR), (int)(oldTempGoal.getX()/SCALING_FACTOR)) && isObstruction((int)(oldTempGoal.getY()/SCALING_FACTOR), (int)((oldTempGoal.getX()+10)/SCALING_FACTOR)))
        {
            //System.out.println("phase detection case 3");
            Point tempWall = new Point((int)(tempGoal.getX() / SCALING_FACTOR), (int)(tempGoal.getY() / SCALING_FACTOR));
            tempWalls.add(tempWall);
            knownTerrain[(int)tempWall.getY()][(int)tempWall.getX()] = 7;
            //rePath = true;
            pathChanged = true;
            updatePath();
        }
        if(oldTempGoal.getX()-10 == tempGoal.getX() && oldTempGoal.getY()-10 == tempGoal.getY() && isObstruction((int)(oldTempGoal.getY()/SCALING_FACTOR), (int)((oldTempGoal.getX()-10)/SCALING_FACTOR)) && isObstruction((int)((oldTempGoal.getY()-10)/SCALING_FACTOR), (int)(oldTempGoal.getX()/SCALING_FACTOR)))
        {
            //System.out.println("phase detection case 4");
            //System.out.println(position);
            //System.out.println(tempGoal);
            Point tempWall = new Point((int) (tempGoal.getX() / SCALING_FACTOR), (int) (tempGoal.getY() / SCALING_FACTOR));
            tempWalls.add(tempWall);
            knownTerrain[(int) tempWall.getY()][(int) tempWall.getX()] = 7;
            // = true;
            pathChanged = true;
            updatePath();
        }
    }

    public void checkChangedStatus()
    {
        Point2D pointToCheck = new Point2D(tempGoal.getX(), tempGoal.getY());
        Point2D currentPos = new Point2D(position.getX(), position.getY());
        if(changed && checkApproximateEquality(currentPos, pointToCheck))
        {
            changed = false;
        }
    }

    public boolean checkApproximateEquality(Point2D p1, Point2D p2)
    {
        if(p1.getX() <= p2.getX()+1 && p1.getX() >= p2.getX()-1 && p1.getY() <= p2.getY()+1 && p1.getY() >= p2.getY()-1)
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    public boolean isObstruction(int y, int x)
    {
        if(knownTerrain[y][x] == 1 || knownTerrain[y][x] == 5 || knownTerrain[y][x] == 7)
        {
            return true;
        }
        return false;
    }

    public void updatePath()
    {
        int[][] blocks = aStarTerrain(knownTerrain);
        Astar pathFinder = new Astar(knownTerrain[0].length, knownTerrain.length, (int)(position.getX()/SCALING_FACTOR), (int)(position.getY()/SCALING_FACTOR), (int)goalPosition.getX(), (int)goalPosition.getY(), blocks);
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
        if(pathChanged)
        {
            wallPhaseDetection();
        }
        cornerCorrection();
        //System.out.println("wall phase attempt detected, corrected to:");
        //System.out.println("position: "+position);
        //System.out.println("temporary goal: "+tempGoal);
        //System.out.println();
    }

    public void open()
    {
        //open door
        if(worldMap.worldGrid[(int)(position.getY()/SCALING_FACTOR)][(int)(position.getX()/SCALING_FACTOR)] == 2)
        {
            worldMap.worldGrid[(int)(position.getY()/SCALING_FACTOR)][(int)(position.getX()/SCALING_FACTOR)] = 0;
            worldMap.updateTile(locationToWorldgrid(position.getY()), locationToWorldgrid(position.getX()), EMPTY);
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
                //HERE A NOISE MUST BE MADE!!!!!
            }
            frozen = true;
        }
        //go through window
        else if(worldMap.worldGrid[(int)(position.getY()/SCALING_FACTOR)][(int)(position.getX()/SCALING_FACTOR)] == 3)
        {
            startTime = System.currentTimeMillis();
            worldMap.worldGrid[(int)(position.getY()/SCALING_FACTOR)][(int)(position.getX()/SCALING_FACTOR)] = 0;
            worldMap.updateTile(locationToWorldgrid(position.getY()), locationToWorldgrid(position.getX()), EMPTY);
            freezeTime = 3;
            frozen = true;
        }
    }
}


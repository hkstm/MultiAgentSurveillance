package Agent;
import javafx.scene.paint.Color;

import javafx.geometry.Point2D;
import java.util.Random;
import java.util.ArrayList;
import java.util.List;
import static World.GameScene.SCALING_FACTOR;

/**
 * A subclass of Agent for the Intruders
 * @author Benjamin, Kailhan
 */

public class Intruder extends Agent{
    private boolean tired;
    private double walkingSpeed = 1.4; //m/s
    private double sprintSpeed = 3.0; //m/s
    private double startTime = 0;
    private static Point2D oldTempGoal;
    private static Point2D tempGoal;
    private static Point2D previousTempGoal;
    private double freezeTime = 0;
    private boolean frozen = false;
    private static boolean changed = false;
    private final long createdMillis = System.currentTimeMillis();
    private long blindMillis = 0;
    private int sprintCounter = 5;
    private int walkCounter = 10; //check if this is right (might be 10 sec not 15)
    private boolean blind = false;


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

    public void run() {
        previousTime = System.nanoTime(); //the first time step is reaallllyyyy small (maybe too small, might have to force it to wait)
        previousPosition = new Point2D(position.getX(), position.getY());
        while(!exitThread) {
            executeAgentLogic();
        }
    }

    public void forceUpdate() {
        if(firstRun) {
            previousTime = System.nanoTime();
            previousPosition = new Point2D(position.getX(), position.getY());
            firstRun = false;
        }
        executeAgentLogic();
    }

    public void executeAgentLogic() {
        currentTime = System.nanoTime();
        delta = currentTime - previousTime;
        delta /= 1e9; //makes it in seconds
        createCone();
        gameTreeIntruder(delta);
        checkForAgentSound();
        previousTime = currentTime;
    }

    public void gameTreeIntruder(double timeStep)
    {
        //TODO fix corner issue
        //TODO add blur
        //TODO check for guards
        //TODO make noise
        //TODO test doors and windows
        //TODO add weights to flags and other types of squares, try manually an possibly with a genetic algorithm
        //this createCone should be redundant but it resolves some errors due to not being able to properly access the cones
        createCone();
        if(!frozen)
        {
            //open door
            if(worldMap.worldGrid[(int)(position.getY()/SCALING_FACTOR)][(int)(position.getX()/SCALING_FACTOR)] == 2)
            {
                worldMap.worldGrid[(int)(position.getY()/SCALING_FACTOR)][(int)(position.getX()/SCALING_FACTOR)] = 0;
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
                freezeTime = 3;
                frozen = true;
            }
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
            if (!blind) {
                updateKnownTerrain();
                //System.out.println("nottttttttttttttttttttttttttttt blind");
            }
            else {
                //System.out.println("blinnnnnnnnnnnnnnnnnnnnnnnnnnd");
                long nowMillis = System.currentTimeMillis();
                int countSec = (int)((nowMillis - this.blindMillis) / 1000);
                System.out.println(countSec);
                if (countSec == 2){
                    blind = false;
                }
            }

            //prints the known terrain every iteration
            //for(int i = 0; i < knownTerrain.length; i++)
            //{
            //    for(int j = 0; j < knownTerrain.length; j++)
            //    {
            //        System.out.print(knownTerrain[i][j]+" ");
            //    }
            //    System.out.println();
            //}
            //System.out.println();
            //System.out.println();

            int[][] blocks = aStarTerrain(knownTerrain);
            Astar pathFinder = new Astar(knownTerrain[0].length, knownTerrain.length, (int)(position.getX()/SCALING_FACTOR), (int)(position.getY()/SCALING_FACTOR), (int)goalPosition.getX(), (int)goalPosition.getY(), blocks);
            List<Node> path = new ArrayList<Node>();
            path = pathFinder.findPath();
            oldTempGoal = tempGoal;
            if(!changed)
            {
                tempGoal = new Point2D((path.get(path.size()-1).row*SCALING_FACTOR)+(SCALING_FACTOR/2), (path.get(path.size()-1).column*SCALING_FACTOR)+(SCALING_FACTOR/2));
                previousTempGoal = new Point2D((path.get(path.size()-2).row*SCALING_FACTOR)+(SCALING_FACTOR/2), (path.get(path.size()-2).column*SCALING_FACTOR)+(SCALING_FACTOR/2));
            }
            if(oldTempGoal != null)
            {
                cornerCorrection();
            }
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
            double walkingDistance = (walkingSpeed*SCALING_FACTOR*timeStep);
            double sprintingDistance = (sprintSpeed*SCALING_FACTOR*timeStep);
            double finalAngle = previousAngle - turnAngle;
            if (finalAngle > 45 || finalAngle < -45){
                //System.out.println(turnAngle);
                blind = true;
                blindMillis = System.currentTimeMillis();
            }
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
                        walkCounter += 10; //changed from 15
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
            if(isObstruction((int)(oldTempGoal.getY()/SCALING_FACTOR), (int)((oldTempGoal.getX()-10)/SCALING_FACTOR)))
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
            if(isObstruction((int)(oldTempGoal.getY()/SCALING_FACTOR), (int)((oldTempGoal.getX()+10)/SCALING_FACTOR)))
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
            if (isObstruction((int)((oldTempGoal.getY()-10)/SCALING_FACTOR), (int)(oldTempGoal.getX()/SCALING_FACTOR)))
            {
                tempGoal = new Point2D(oldTempGoal.getX()-10, oldTempGoal.getY());
                changed = true;
            }
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
}

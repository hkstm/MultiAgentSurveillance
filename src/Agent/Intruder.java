package Agent;
import World.WorldMap;
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
    private int counter = 0;
    private final double SPRINTING_TIME = 5*1e9;
    private final double RESTING_TIME = 10*1e9;
    private double walkingSpeed = 1.4; //m/s
    private double sprintSpeed = 3.0; //m/s
    private double startTime;
    private static Point2D oldTempGoal;
    private static Point2D tempGoal;
    private double freezeTime = 0;
    private static boolean changed = false;
    private static boolean blur = false;
    private final long createdMillis = System.currentTimeMillis();
    private int sprintCounter = 5;
    private int walkCounter = 15;


    /**
     * An Intruder constructor with an empty internal map
     * @param position is a point containing the coordinates of the Intruderq
     * @param direction is the angle which the agent is facing, this spans from -180 to 180 degrees
     */

    public Intruder(Point2D position, double direction) {
        super(position, direction);
        //tempGoal = new Point2D(500,500);
        this.viewingAngle = 45;
//        this.viewingAngle = 60;
        this.visualRange[0] = 0;
        this.visualRange[1] = 7.5;
//        this.visualRange[1] = 20;
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

    /*
    public void open(boolean loud)
    {
        if (worldMap.coordinatesToCell(position) == DOOR)
        {
            class OpenDoor extends TimerTask
            {
                public void run()
                {
                    worldMap.updateTile((int)position.getX(), (int)position.getY(), OPEN_DOOR);
                    knownTerrain[(int)position.getX()][(int)position.getY()] = OPEN_DOOR;
                }
            }
            Timer timer = new Timer();
            TimerTask openDoor = new OpenDoor();
            if (loud)
            {
                timer.schedule(openDoor, 5000);
            }
            else
            {
                Random r = new Random();
                double time;
                do
                {
                    time = r.nextGaussian()*2+12;
                }
                while (time <= 0);
                time = time*1000;
                timer.schedule(openDoor, (long)time);
            }
            timer.cancel();
        }
        else if (worldMap.coordinatesToCell(position) == WINDOW)
        {
            class OpenWindow extends TimerTask
            {
                public void run()
                {
                    worldMap.updateTile((int)position.getX(), (int)position.getY(), OPEN_WINDOW);
                    knownTerrain[(int)position.getX()][(int)position.getY()] = OPEN_WINDOW;
                }
            }
            Timer timer = new Timer();
            TimerTask openWindow = new OpenWindow();
            timer.schedule(openWindow, 3000);
        }
    }
    */

    public void run() {
        previousTime = System.nanoTime(); //the first time step is reaallllyyyy small (maybe too small, might have to force it to wait)
        previousPosition = new Point2D(position.getX(), position.getY());
        while(!exitThread) {
            executeAgentLogic();
        }
    }

    public void executeAgentLogic() {
        currentTime = System.nanoTime();
        delta = currentTime - previousTime;
        delta /= 1e9; //makes it in seconds
        gameTreeIntruder(delta);
        checkForAgentSound();
        previousTime = currentTime;
    }

    public void gameTreeIntruder(double timeStep)
    {
        //TODO add blur
        //TODO check for guards
        //TODO make noise
        //TODO test doors and windows
        //TODO add weights to flags and other types of squares, try manually an possibly with a genetic algorithm
        if(oldTempGoal != null)
        {
            checkChangedStatus();
        }
        //open door
        if(knownTerrain[(int)(position.getX()/SCALING_FACTOR)][(int)(position.getY()/SCALING_FACTOR)] == 2)
        {
            Random random = new Random();
            startTime = System.nanoTime();
            if(Math.random() > 0.5)
            {
                freezeTime = (random.nextGaussian()*2+12)*1e9;
            }
            else
            {
                freezeTime = 5;
                //HERE A NOISE MUST BE MADE!!!!!
            }
            knownTerrain[(int)position.getX()][(int)position.getY()] = 33;
        }
        //go through window
        if(knownTerrain[(int)(position.getX()/SCALING_FACTOR)][(int)(position.getY()/SCALING_FACTOR)] == 3)
        {
            startTime = System.nanoTime();
            freezeTime = 3e9;
        }
        if(currentTime+freezeTime > startTime)
        {
            freezeTime = 0;
            startTime = System.nanoTime();
            //this should maybe take in some parameters, like how far and how wide the cone is, not all agents have the same vision capabilities
            //also, it does not detect walls
            direction -= 90;
            updateKnownTerrain();
            //for(int row = 0; row < knownTerrain.length; row++)
            //{
            //    for(int column = 0; column < knownTerrain.length; column++)
            //    {
            //        System.out.print(knownTerrain[row][column]+" ");
            //    }
            //    System.out.println();
            //}
            //System.out.println();
            //System.out.println();
            int[][] blocks = aStarTerrain(knownTerrain);
            /**
             * idk what this is supposed to be but getting out of bounds trying to do it the way it was previously done -kailhan
             */
            Astar pathFinder = new Astar(knownTerrain[0].length, knownTerrain.length, locationToWorldgrid(position.getX()),
                   locationToWorldgrid(position.getY()), locationToWorldgrid(goalPosition.getX()), locationToWorldgrid(position.getY()), blocks);
            List<Node> path = new ArrayList<Node>();
            path = pathFinder.findPath();

            oldTempGoal = tempGoal;
            if(!changed)
            {
                //System.out.println("not changed");
                /**
                 * idk what this is supposed to be but it is dying if i dont change it -kailhan
                 */

                if(path.size() > 0) {
                    tempGoal = new Point2D(worldMap.convertArrayToWorld(path.get(path.size()-1).row) + worldMap.convertArrayToWorld(1)/2, worldMap.convertArrayToWorld(path.get(path.size()-1).column) + worldMap.convertArrayToWorld(1)/2);
                } else {
                    System.out.println("path size: " + path.size() + "setting tempGoal to current position");
                    tempGoal = new Point2D(position.getX(), position.getY());
                }
             }
            if(oldTempGoal != null)
            {
                cornerCorrection();
            }
            if(changed)
            {
                //System.out.println("changed");
            }
            //System.out.println();
            //System.out.println("x goal: "+tempGoal.x+" y goal: "+tempGoal.y);
            //System.out.println("x: "+(int)(position.getX()/SCALING_FACTOR)+" y: "+(int)(position.getX()/SCALING_FACTOR));
            //System.out.println(goalPosition.x+" "+goalPosition.y);
            //System.out.println(Math.abs(tempGoal.y-(int)(position.getY()/SCALING_FACTOR)));
            double divisor = Math.abs(tempGoal.getY()-position.getY());
            if(divisor == 0)
            {
                divisor++;
                System.out.println("divisor is zero");
            }
            double turnAngle = Math.toDegrees(Math.atan(Math.abs(tempGoal.getX()-position.getX())/divisor));
            double walkingDistance = (walkingSpeed*SCALING_FACTOR*timeStep);
            double sprintingDistance = (sprintSpeed*SCALING_FACTOR*timeStep);
            /**
             * you probably dont wanna do this anymore with the new move logic and also call updateDirection()
             * for proper turning ~Kailhan
             */
            if(tempGoal.getX() >= position.getX() && tempGoal.getY() <= position.getY())
            {
                turnToFace(turnAngle);
            }
            else if(tempGoal.getX() >= position.getX() && tempGoal.getY() > position.getY())
            {
                turnToFace(180-turnAngle);
            }
            else if(tempGoal.getX() < position.getX() && tempGoal.getY() > position.getY())
            {
                turnToFace(180+turnAngle);
            }
            else if(tempGoal.getX() < position.getX() && tempGoal.getY() <= position.getY())
            {
                turnToFace(360-turnAngle);
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
                        System.out.println(countSec);
                        tired = true;
                        sprintCounter = sprintCounter + 15;
                    }
                }
            }
            else
            {
                if(legalMoveCheck(walkingDistance))
                {
                    long nowMillis = System.currentTimeMillis();
                    int countSec = (int)((nowMillis - this.createdMillis) / 1000);
                    if (countSec != walkCounter) {
                        move(walkingDistance);
                    }
                    else{
                        System.out.println(countSec);
                        tired = false;
                        walkCounter = walkCounter +15;
                    }
                }
            }
        }
    }

    public void cornerCorrection()
    {
        if(oldTempGoal.getX()+10 == tempGoal.getX() && oldTempGoal.getY()-10 == tempGoal.getY()) //could use inequailty operators here
        {
            if(knownTerrain[(int)(oldTempGoal.getY()/SCALING_FACTOR)][(int)((oldTempGoal.getX()+10)/SCALING_FACTOR)] == 1 || knownTerrain[(int)(oldTempGoal.getY()/SCALING_FACTOR)][(int)((oldTempGoal.getX()+10)/SCALING_FACTOR)] == 5 || knownTerrain[(int)(oldTempGoal.getY()/SCALING_FACTOR)][(int)((oldTempGoal.getX()+10)/SCALING_FACTOR)] == 7)
            {
                System.out.println("1");
                tempGoal = new Point2D(tempGoal.getX()-10, tempGoal.getY());
                changed = true;
            }
            else if(knownTerrain[(int)((oldTempGoal.getY()-10)/SCALING_FACTOR)][(int)(oldTempGoal.getX()/SCALING_FACTOR)] == 1 || knownTerrain[(int)((oldTempGoal.getY()-10)/SCALING_FACTOR)][(int)(oldTempGoal.getX()/SCALING_FACTOR)] == 5 || knownTerrain[(int)((oldTempGoal.getY()-10)/SCALING_FACTOR)][(int)(oldTempGoal.getX()/SCALING_FACTOR)] == 7)
            {
                System.out.println("2");
                tempGoal =  new Point2D(tempGoal.getX(), tempGoal.getY()+10);
                changed = true;
            }
        }
        else if(oldTempGoal.getX()-10 == tempGoal.getX() && oldTempGoal.getY()+10 == tempGoal.getY()) //could use inequailty operators here
        {
            if(knownTerrain[(int)((oldTempGoal.getY()+10)/SCALING_FACTOR)][(int)(oldTempGoal.getX()/SCALING_FACTOR)] == 1 || knownTerrain[(int)((oldTempGoal.getY()+10)/SCALING_FACTOR)][(int)(oldTempGoal.getX()/SCALING_FACTOR)] == 5 || knownTerrain[(int)((oldTempGoal.getY()+10)/SCALING_FACTOR)][(int)(oldTempGoal.getX()/SCALING_FACTOR)] == 7)
            {
                System.out.println("3");
                tempGoal = new Point2D(oldTempGoal.getX()-10, oldTempGoal.getY());
                changed = true;
            }
            if(knownTerrain[(int)((oldTempGoal.getY()-10)/SCALING_FACTOR)][(int)(oldTempGoal.getX()/SCALING_FACTOR)] == 1 || knownTerrain[(int)((oldTempGoal.getY()-10)/SCALING_FACTOR)][(int)(oldTempGoal.getX()/SCALING_FACTOR)] == 5 || knownTerrain[(int)((oldTempGoal.getY()-10)/SCALING_FACTOR)][(int)(oldTempGoal.getX()/SCALING_FACTOR)] == 7)
            {
                System.out.println("4");
                tempGoal = new Point2D(tempGoal.getX()+10, tempGoal.getY());
                changed = true;
            }
        }
        else if(oldTempGoal.getX()+10 == tempGoal.getX() && oldTempGoal.getY()+10 == tempGoal.getY()) //could use inequailty operators here
        {
            if(knownTerrain[(int)((oldTempGoal.getY()+10)/SCALING_FACTOR)][(int)(oldTempGoal.getX()/SCALING_FACTOR)] == 1 || knownTerrain[(int)((oldTempGoal.getY()+10)/SCALING_FACTOR)][(int)(oldTempGoal.getX()/SCALING_FACTOR)] == 5 || knownTerrain[(int)((oldTempGoal.getY()+10)/SCALING_FACTOR)][(int)(oldTempGoal.getX()/SCALING_FACTOR)] == 7)
            {
                System.out.println("5");
                tempGoal = new Point2D(tempGoal.getX()-10, tempGoal.getY());
                changed = true;
            }
            if(knownTerrain[(int)(oldTempGoal.getY()/SCALING_FACTOR)][(int)((oldTempGoal.getX()+10)/SCALING_FACTOR)] == 1 || knownTerrain[(int)(oldTempGoal.getY()/SCALING_FACTOR)][(int)((oldTempGoal.getX()+10)/SCALING_FACTOR)] == 5 || knownTerrain[(int)(oldTempGoal.getY()/SCALING_FACTOR)][(int)((oldTempGoal.getX()+10)/SCALING_FACTOR)] == 7)
            {
                System.out.println("6");
                tempGoal = new Point2D(tempGoal.getX()-10, tempGoal.getY());
                changed = true;
            }
        }
        else if(oldTempGoal.getX()-10 == tempGoal.getX() && oldTempGoal.getY()-10 == tempGoal.getY()) //could use inequailty operators here
        {
            if (knownTerrain[(int) (oldTempGoal.getY() / SCALING_FACTOR)][(int) ((oldTempGoal.getX() - 10) / SCALING_FACTOR)] == 1 || knownTerrain[(int) (oldTempGoal.getY() / SCALING_FACTOR)][(int) ((oldTempGoal.getX() - 10) / SCALING_FACTOR)] == 5 || knownTerrain[(int) (oldTempGoal.getY() / SCALING_FACTOR)][(int) ((oldTempGoal.getX() - 10) / SCALING_FACTOR)] == 7)
            {
                System.out.println("7");
                tempGoal = new Point2D(tempGoal.getX() + 10, tempGoal.getY());
                changed = true;
            }
            if (knownTerrain[(int) ((oldTempGoal.getY() - 10) / SCALING_FACTOR)][(int) (oldTempGoal.getX() / SCALING_FACTOR)] == 1 || knownTerrain[(int) ((oldTempGoal.getY() - 10) / SCALING_FACTOR)][(int) (oldTempGoal.getX() / SCALING_FACTOR)] == 5 || knownTerrain[(int) ((oldTempGoal.getY() - 10) / SCALING_FACTOR)][(int) (oldTempGoal.getX() / SCALING_FACTOR)] == 7)
            {
                System.out.println("8");
                tempGoal = new Point2D(tempGoal.getX(), tempGoal.getY() + 10);
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
            System.out.println("changing to false");
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
}

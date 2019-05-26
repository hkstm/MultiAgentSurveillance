package Agent;
import javafx.scene.paint.Color;

import javafx.geometry.Point2D;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.ArrayList;
import java.util.List;
import java.awt.Point;

import static World.WorldMap.*;
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
    private int visionRadius = 10;
    private int visionAngle = 45;
    private double walkingSpeed = 1.4; //m/s
    private double sprintSpeed = 3.0; //m/s
    private double startTime= System.nanoTime();
    private Point tempGoal;
    private double freezeTime = 0;

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
     * @param loud is whether the intruder wishes to open the door fast but loudly or slowly and quietly
     */

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
        //TODO check for guards, need the vision to be working for this
        //TODO add weights to flags and other types of squares, try manually an possibly with a genetic algorithm
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
        if(startTime+freezeTime > currentTime)
        {
            System.out.println("unfrozen");
            freezeTime = 0;
            startTime = System.nanoTime();
            updateKnownTerrain(); //this should maybe take in some parameters, like how far and how wide the cone is, not all agents have the same vision capabilities :D
            int[][] blocks = aStarTerrain(knownTerrain);
            Astar pathFinder = new Astar(knownTerrain[0].length, knownTerrain.length, (int)(position.getX()/SCALING_FACTOR), (int)(position.getY()/SCALING_FACTOR), (int)(getGoalPosition().getX()/SCALING_FACTOR), (int)(getGoalPosition().getY()/SCALING_FACTOR), blocks);
            List<Node> path = new ArrayList<Node>();
            path = pathFinder.findPath();
            tempGoal = new Point(path.get(path.size()-1).i, path.get(path.size()-1).j);
            double turnAngle = Math.toDegrees(Math.atan(Math.abs(tempGoal.y-(int)(position.getY()/SCALING_FACTOR))/Math.abs(tempGoal.x-(int)(position.getX()/SCALING_FACTOR))));
            double walkingDistance = (walkingSpeed*SCALING_FACTOR*timeStep);
            double sprintingDistance = (sprintSpeed*SCALING_FACTOR*timeStep);
            if(tempGoal.x >= (int)(position.getX()/SCALING_FACTOR) && tempGoal.y <= (int)(position.getY()/SCALING_FACTOR))
            {
                turnToFace(turnAngle);
            }
            else if(tempGoal.x >= (int)(position.getX()/SCALING_FACTOR) && tempGoal.y >= (int)(position.getY()/SCALING_FACTOR))
            {
                turnToFace(90+turnAngle);
            }
            else if(tempGoal.x <= (int)(position.getX()/SCALING_FACTOR) && tempGoal.y >= (int)(position.getY()/SCALING_FACTOR))
            {
                turnToFace(270-turnAngle);
            }
            else if(tempGoal.x <= (int)(position.getX()/SCALING_FACTOR) && tempGoal.y <= (int)(position.getY()/SCALING_FACTOR))
            {
                turnToFace(270+turnAngle);
            }
            if(!tired)
            {
                if(counter == 0)
                {
                    counter = 1;
                    startTime = System.nanoTime();
                }
                if(startTime+SPRINTING_TIME > currentTime)
                {
                    tired = true;
                    counter = 0;
                }
                if(legalMoveCheck(sprintingDistance))
                {
                    System.out.println("sprinting");
                    move(sprintingDistance);
                }
            }
            else
            {
                if(counter == 0)
                {
                    counter = 1;
                    startTime = System.nanoTime();
                }
                if(startTime+RESTING_TIME > currentTime)
                {
                    tired = false;
                    counter = 0;
                }
                if(legalMoveCheck(walkingDistance))
                {
                    System.out.println("walking");
                    move(walkingDistance);
                }
            }
        }
    }
}

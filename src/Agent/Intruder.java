package Agent;
import World.WorldMap;
import javafx.scene.paint.Color;

import javafx.geometry.Point2D;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import static World.WorldMap.*;
import static World.GameScene.SCALING_FACTOR;

/**
 * A subclass of Agent for the Intruders
 * @author Benjamin, Kailhan
 */

public class Intruder extends Agent{
    private boolean tired;
    private int visionRadius = 10;
    private int visionAngle = 45;
    private double walkingSpeed = 1.4; //m/s

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
        //this.knownTerrain = worldMap.getWorldGrid();
        this.tired = false;
        for(int i = 1;i < 200;i++) {
            for(int j = 1;j<200;j++) {
                //knownTerrain[i][j] = 8;
            }
        }
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
    public Point2D gameTreeIntruder(double timeStep)
    {
        previousPosition= new Point2D(position.getX(), position.getY());
        updateKnownTerrain(visionRadius*SCALING_FACTOR, viewingAngle);
        Point2D goal = getGoalPosition();
        double walkingDistance = (walkingSpeed*SCALING_FACTOR*timeStep);
        double xDifference = Math.abs(goal.getX()-position.getX());
        double yDifference = Math.abs(goal.getY()-position.getY());
        double angleToGoal = Math.atan(xDifference/yDifference);
        if(goal.getX() >= position.getX() && goal.getY() >= position.getY())
        {
            turn(180-angleToGoal);
        }
        else if(goal.getX() >= position.getX() && goal.getY() <= position.getY())
        {
            turn(angleToGoal);
        }
        else if(goal.getX() <= position.getX() && goal.getY() >= position.getY())
        {
            turn(-(180-angleToGoal));
        }
        else if(goal.getX() <= position.getX() && goal.getY() <= position.getY())
        {
            turn(-angleToGoal);
        }
        if(legalMoveCheck(walkingDistance))
        {
            move(walkingDistance);
        }
        return position;
    }
}

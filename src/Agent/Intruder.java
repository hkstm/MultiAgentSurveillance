package Agent;
import java.awt.geom.Point2D;
import java.lang.Math;
import World.WorldMap;
import java.awt.*;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

/**
 * A subclass of Agent for the Intruders
 * @author Benjamin
 */

public class Intruder extends Agent{
    private boolean tired;

    /**
     * An Intruder constructor with an empty internal map
     * @param position is a point containing the coordinates of the Intruder
     * @param direction is the angle which the agent is facing, this spans from -180 to 180 degrees
     */

    public Intruder(Point2D.Double position, double direction)
    {
        super(position, direction);
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
        if (coordinatesToCell(position) == 2)
        {
            class OpenDoor extends TimerTask
            {
                public void run()
                {
                    worldMap.updateTile((int)position.getX(), (int)position.getY(), 22);
                    knownTerrain[(int)position.getX()][(int)position.getY()] = 22;
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
        else if (coordinatesToCell(position) == 3)
        {
            class OpenWindow extends TimerTask
            {
                public void run()
                {
                    worldMap.updateTile((int)position.getX(), (int)position.getY(), 33);
                    knownTerrain[(int)position.getX()][(int)position.getY()] = 33;
                }
            }
            Timer timer = new Timer();
            TimerTask openWindow = new OpenWindow();
            timer.schedule(openWindow, 3000);
        }
    }
}

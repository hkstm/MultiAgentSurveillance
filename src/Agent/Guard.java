package Agent;
import World.WorldMap;
import java.awt.geom.Point2D;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;


public class Guard extends Agent {

    /**
     * A subclass of Agent for the Guards with an internal map containing the starting positions of other guards and the terrain across the map
     * @author Benjamin, Thibaut
     */

    double speed = 1.4;
    int[] visualRange = new int[2];
    double viewingAngle;


    public Guard(Point2D.Double position, double direction)
    {
        super(position, direction);
        //this.knownTerrain = worldMap.getWorldGrid();
    }



    public boolean equals(Object obj) {
        boolean equals = false;
        if (obj == null) return false;
        if (obj == this) return true;
        if (!(obj instanceof Guard)) return false;
        Guard o = (Guard) obj;
        if((o.direction == this.direction) && (o.position.equals(this.position))) equals = true;
        return equals;
    }
    public int[] getVisualRange(){
        if (coordinatesToCell(position) == 5){ // i.e. guard in on a tower
           visualRange[0] = 2;
           visualRange[1] = 15;
           viewingAngle = 30;
        }
        else {
            visualRange[0] = 0;
            visualRange[1] = 6;
            viewingAngle = 45;
        }
        return visualRange;
    }
    public void openTower()
    {
        if (coordinatesToCell(position) == 5)
        {
            class OpenTower extends TimerTask
            {
                public void run()
                {
                    //worldMap.updateTile((int)position.getX(), (int)position.getY(), 5);
                    knownTerrain[(int)position.getX()][(int)position.getY()] = 5;
                }
            }
            Timer timer = new Timer();
            TimerTask openTower = new OpenTower();

                timer.schedule(openTower, 3000);
            timer.cancel();
        }
    }
}

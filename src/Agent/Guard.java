package Agent;
import World.WorldMap;
import javafx.scene.paint.Color;

import java.awt.geom.Point2D;
import java.util.Timer;
import java.util.TimerTask;

import static World.WorldMap.SENTRY;
import static World.WorldMap.TARGET;


public class Guard extends Agent {

    /**
     * A subclass of Agent for the Guards with an internal map containing the starting positions of other guards and the terrain across the map
     * @author Benjamin, Thibaut
     */

    public Guard(Point2D.Double position, double direction) {
        super(position, direction);
        this.viewingAngle = 45;
        this.visualRange[0] = 0;
        this.visualRange[1] = 6;
        this.color = Color.AZURE;
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
    public void updateVisualRange(){
        if (worldMap.coordinatesToCell(position) == SENTRY) { // i.e. guard in on a tower //shouldnt this be SENTRY then not TARGET?
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
}
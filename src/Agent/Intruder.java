package Agent;
import java.lang.Math;
import World.WorldMap;
import java.awt.*;

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

    public Intruder(Point position, double direction)
    {
        super(position, direction);
        //this.knownTerrain = worldMap.getWorldGrid();
        this.tired = false;
//        for (int i = 1;i < 200;i++)
//        {
//            for (int j = 1;j<200;j++)
//                knownTerrain[i][j] = 8; //8 indicates unexplored terrain
//        }
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
}

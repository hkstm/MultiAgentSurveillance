package Agent;
import World.WorldMap;
import java.awt.geom.Point2D;

/**
 * A subclass of Agent for the Intruders
 * @author Benjamin
 */

public class Guard extends Agent {

    /**
     * A subclass of Agent for the Guards with an internal map containing the starting positions of other guards and the terrain across the map
     * @author Benjamin
     */

    public Guard(Point2D.Double position, double direction)
    {
        super(position, direction);
        this.knownTerrain = worldMap.getWorldGrid();
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
}

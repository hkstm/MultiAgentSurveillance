package Agent;
import World.WorldMap;
import java.awt.geom.Point2D;

/**
 * A subclass of Agent for the Intruders
 * @author Benjamin
 */

public class Guard extends Agent {
    private Point2D.Double position;
    private double direction;
    private int[][] knownTerrain;
    private WorldMap worldMap;

    /**
     * A subclass of Agent for the Guards with an internal map containing the starting positions of other guards and the terrain across the map
     * @author Benjamin
     */

    public Guard(Point2D.Double position, double direction)
    {
        super(position, direction);
        this.knownTerrain = worldMap.getWorldGrid();
    }
}

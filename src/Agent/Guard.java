package Agent;
import World.WorldMap;
import java.awt.*;

/**
 * A subclass of Agent for the Intruders
 * @author Benjamin
 */

public class Guard extends Agent {
    private Point position;
    private double direction;
    private int[][] knownTerrain;
    private World.WorldMap WorldMap;

    /**
     * A subclass of Agent for the Guards with an internal map containing the starting positions of other guards and the terrain across the map
     * @author Benjamin
     */

    public Guard(Point position, double direction)
    {
        super(position, direction);
        this.knownTerrain = WorldMap.getWorldGrid();
    }
}

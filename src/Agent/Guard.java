package Agent;

import World.WorldMap;

import java.awt.*;

public class Guard extends Agent {
    private Point position;
    private double direction;
    private int[][] knownTerrain;
    private World.WorldMap WorldMap;
    public Guard(Point position, double direction)
    {
        super(position, direction);
        this.knownTerrain = WorldMap.getWorldGrid();
    }
}

package World;

public class Settings {

    private int worldSize;
    private WorldMap worldMap;
    public static final String[] TILE_TYPES = {"Empty", "Structure", "Door", "Window", "Target", "Sentry", "Decreased Vis. Range", "Wall", "Entry Point"};

    public Settings(WorldMap worldMap) {
        this.worldSize = worldMap.getSize();
        this.worldMap = worldMap;
    }

    public int getWorldSize() {
        return worldSize;
    }
    public WorldMap getWorldMap() {
        return worldMap;
    }
}

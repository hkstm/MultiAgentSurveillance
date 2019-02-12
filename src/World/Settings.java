package World;

public class Settings {

    private int worldSize;
    private WorldMap worldMap;
    public static final String[] TILE_TYPES = {"Empty", "Structure", "Door", "Window", "Target", "Sentry", "Decreased Vis. Range", "Wall"};
    public static final int SIZE_SMALL = 4;
    public static final int SIZE_MEDIUM = 6;
    public static final int SIZE_LARGE = 8;

    public Settings() {
        this(8);
    }

    public Settings(int worldSize) {
        this(worldSize, new WorldMap(worldSize));
    }

    public Settings(int worldSize, WorldMap worldMap) {

        this.worldSize = worldSize;
        if(worldMap == null) {
            this.worldMap = new WorldMap(worldSize);
        } else {
            this.worldMap = worldMap;
        }
    }

    public int getWorldSize() {
        return worldSize;
    }
    public WorldMap getWorldMap() {
        return worldMap;
    }

}

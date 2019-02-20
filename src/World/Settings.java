package World;

public class Settings {

    private int worldSize;
    private WorldMap worldMap;
    public static final String[] TILE_TYPES = {"Empty", "Structure", "Door", "Window", "Target", "Sentry", "Decreased Vis. Range", "Wall"};
    public static final int SIZE_SMALL = 50;
    public static final int SIZE_MEDIUM = 200;
    public static final int SIZE_LARGE = 500;

    public Settings() {
        this(200);
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

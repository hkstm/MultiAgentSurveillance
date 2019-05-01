package Agent;

import World.WorldMap;
import java.util.Random;


public class Wander extends Routine {
    private static Random random;
    WorldMap worldMap;
    private MoveTo moveTo;
    private Routine routine;
    private Agent agent;

    public void reset() {
        //  this.moveTo = new MoveTo(random.nextInt(worldMap.width, worldmap.height) IDK how to get them
    }

    public Wander(WorldMap worldMap) {
        super();
        this.worldMap = worldMap;
        //  this.moveTo = new MoveTo(random.nextInt(worldMap.width, worldmap.height) IDK how to get them
    }

    @Override
    public void act(Agent agent, WorldMap worldMap) {
        if (!moveTo.isWalking()) {
            return;
        }
        this.moveTo.act(agent, worldMap);
        if (this.moveTo.isSuccess()) {
            succeed();
        } else if (this.moveTo.isFailure()) {
            fail();
        }
    }
    public void update() {
        if (routine.getState() == null) {
            // hasn't started yet so we start it
            routine.start();
        }
        routine.act(agent, worldMap);
    }
}

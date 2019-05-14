package Agent;

import World.WorldMap;
import java.util.Random;


public class Wander extends Routine {
    private static Random random;
    WorldMap worldMap;
    private MoveTo moveTo;
    private Routine routine;
    private Guard guard;

    public void reset() {
        this.moveTo = new MoveTo((Math.random() * 199) +1,(Math.random() * 199) +1);
    }

    public Wander(WorldMap worldMap) {
        super();
        this.worldMap = worldMap;
         this.moveTo = new MoveTo((Math.random() * 199) +1,(Math.random() * 199) +1);
    }

    @Override
    public void act(Guard guard, WorldMap worldMap) {
        if (!moveTo.isWalking()) {
            return;
        }
        this.moveTo.act(guard, worldMap);
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
        routine.act(guard, worldMap);
    }
}

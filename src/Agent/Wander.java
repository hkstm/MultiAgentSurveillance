package Agent;

import World.GameScene;
import World.WorldMap;
import java.util.Random;

import static Agent.MoveTo.destX;
import static Agent.MoveTo.destY;


public class Wander extends Routine {
    WorldMap worldMap;
    private MoveTo moveTo;
    Guard guard = GameScene.guard;

    @Override
    public void start() {
        super.start();
        this.moveTo.start();
    }

    public void reset() {
        this.moveTo = new MoveTo((Math.random() * 199) +1,(Math.random() * 199) +1);
    }

    public Wander(WorldMap worldMap) {
        super();
        this.worldMap = worldMap;

         this.moveTo = new MoveTo((Math.random() * 100) +1,(Math.random() * 100) +1);
         System.out.println(destX + "<-- x, y-->" + destY);
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

}

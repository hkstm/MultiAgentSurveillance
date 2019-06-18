package Agent;

import World.GameScene;
import World.WorldMap;
import java.util.Random;

import static Agent.MoveTo.destX;
import static Agent.MoveTo.destY;


public class Wander extends Routine {
    WorldMap worldMap;
    private MoveTo moveTo;
    /**
     * you dont wanna do this hmu if i forgot to mention it when meeting -kailhan
     */
    private Guard guard;

    @Override
    public void start() {
        super.start();
        this.moveTo.start();
    }

    public void reset() {

    }

    public Wander(WorldMap worldMap,Guard guard) {
        super();
        this.worldMap = worldMap;
        this.guard = guard;
        /**
         * logic for random move
         */

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

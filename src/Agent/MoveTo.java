package Agent;

import World.WorldMap;

import java.awt.geom.Point2D;

import static World.GameScene.SCALING_FACTOR;
/**
 * this routine makes the agent move from a point a to a target
 * @author Thibaut Donis
 */
public class MoveTo extends Routine {
    final protected int destX;
    final protected int destY;
    Routine routine;
    WorldMap worldMap;
    Agent agent;

    public MoveTo(int destX, int destY) {
        super();
        this.destX = destX;
        this.destY = destY;
    }

    public void reset() {
        start();
    }
    @Override
    public void act(Agent agent, WorldMap worldMap) {
        if(isRunning()){
            if(!isAtDestination(agent)){
                Move(agent);
            }
        }
    }
    private void Move(Agent agent) {
        //logic for the moves
        if (isAtDestination(agent)) {
            succeed();
        }
    }
    private boolean isAtDestination(Agent agent){
        return destX == agent.getPosition().getX() && destY == agent.getPosition().getY();
    }
    public void update() {
        if (routine.getState() == null) {
            // hasn't started yet so we start it
            routine.start();
        }
        routine.act(agent, worldMap);
    }
    public void start() {
        System.out.println(">>> Starting routine: " + this.getClass().getSimpleName());
        this.state = RoutineState.Running;
    }

    protected void succeed() {
        System.out.println(">>> Routine: " + this.getClass().getSimpleName() + " SUCCEEDED");
        this.state = RoutineState.Success;
    }

    protected void fail() {
        System.out.println(">>> Routine: " + this.getClass().getSimpleName() + " FAILED");
        this.state = RoutineState.Failure;
    }
}
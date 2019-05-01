package Agent;
import World.WorldMap;
/**
 * super class for the routines/ behaviours
 * to create new behaviour just extend this class
 * @author Thibaut Donis
 */
public abstract class Routine {

    public enum RoutineState {
        Success,
        Failure,
        walking
    }

    protected RoutineState state;

    protected Routine() { }

    public void start() {
        System.out.println(">>> Starting routine: " + this.getClass().getSimpleName());
        this.state = RoutineState.walking;
    }

    public abstract void reset();

    public abstract void act(Agent agent, WorldMap worldMap);


    protected void succeed() {
        System.out.println(">>> Routine: " + this.getClass().getSimpleName() + " SUCCEEDED");
        this.state = RoutineState.Success;
    }

    protected void fail() {
        System.out.println(">>> Routine: " + this.getClass().getSimpleName() + " FAILED");
        this.state = RoutineState.Failure;
    }

    public boolean isSuccess() {
        return state.equals(RoutineState.Success);
    }

    public boolean isFailure() {
        return state.equals(RoutineState.Failure);
    }

    public boolean isWalking() {
        return state.equals(RoutineState.walking);
    }

    public RoutineState getState() {
        return state;
    }


}
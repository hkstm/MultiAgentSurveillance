package Agent;

import World.WorldMap;
import javafx.geometry.Point2D;
import Agent.Agent;
import javafx.scene.shape.Shape;


import java.util.List;

import static Agent.Agent.BASE_SPEED;
import static Agent.Agent.locationToWorldgrid;
import static World.GameScene.SCALING_FACTOR;
/**
 * this routine makes the agent move from a point a to a target
 * @author Thibaut
 */
public class MoveTo extends Routine {
    static double destX;
    static double destY;
    public static WorldMap worldMap;
    protected double currentTime;
    protected double delta;
    protected double previousTime;
    private List<Shape> cones;
    private List<Agent> agents;
    private Guard guard;
    private Intruder intruder;
    double direction;

    @Override
    public void start() {
        super.start();
    }

    public MoveTo(double destX, double destY) {
        super();
        this.destX = destX;
        this.destY = destY;
    }

    public void reset() {
        start();
    }

    @Override
    public void act(Guard guard, WorldMap worldMap) {
        if (isWalking()) {
            if (!isAtDestination(guard)) {
                Move(guard);
            }
        }
    }

    public void Move(Guard guard) {
        delta = guard.delta;
        if (!isAtDestination(guard)) {
            guard.gameTree(delta);

            System.out.println("destx: " + destX + " destY: " + destY);
            if (!guard.firstRun) {
                if (seen(guard)) {
                    System.out.println("intruder seen");
                    guard.chasing = true;
                    Routine guard2 = Routines.sequence(
                            Routines.chase(guard, intruder)
                    );
                    guard.setRoutine(guard2);
                    // update blackboard
                    guard.blackboard.chaseIntruder(intruder);
                    guard.routine.start();
                }
                System.out.println("didnt find it yet");
            }
        }
        previousTime = currentTime;
    }

    private boolean isAtDestination(Guard guard) {
        return destX == locationToWorldgrid(guard.getPosition().getY()) &&
                destY == locationToWorldgrid(guard.getPosition().getX());
    }

    private boolean seen(Guard guard) {
        //  this.guard = guard;
//        //this.agents = guard.worldMap.getAgents();
//        this.cones = guard.worldMap.getAgentsCones();
//        // System.out.println("cone size: " + cones.size());
//        for (Agent intruder : guard.worldMap.getAgents()) {
//            if (intruder instanceof Intruder) {
//                if (cones.contains(intruder.getPosition())) return true;
//
//            }
//        }
        for (Agent intruder : guard.worldMap.getAgents()) {
            if (intruder instanceof Intruder) {
                if (guard.inVision(intruder.getPosition())) {
                    destX = locationToWorldgrid(intruder.getPosition().getY());
                    destY = locationToWorldgrid(intruder.getPosition().getX());
                    return true;

                }
            }
        }

        return false;
    }
}

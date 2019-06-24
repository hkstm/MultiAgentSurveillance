package Agent;

import World.WorldMap;
import javafx.geometry.Point2D;
import Agent.Agent;
import javafx.scene.shape.Shape;


import java.util.List;

import static Agent.Agent.BASE_SPEED;
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
        if(!isAtDestination(guard)) {
            guard.gameTree(delta);

            System.out.println("destx: " + destX + " destY: " + destY);
            if (!guard.firstRun) {
                if (seen(guard)) {
                    System.out.println("intruder seen");
                    Routine guard1 = Routines.sequence(
                            Routines.chase(guard,intruder)
                    );
                    guard.setRoutine(guard1);
                    guard.routine.start();
                    /*guard.updateDirection(direction);
                    destX = intruder.getPosition().getX();
                    destY = intruder.getPosition().getY();
                    direction = Math.toDegrees(Math.atan2((intruder.getPosition().getY() - guard.getPosition().getY()), (intruder.getPosition().getX() - guard.getPosition().getX())));
                    guard.gameTree(delta); */
                }
                System.out.println("didnt find it yet");
            }
        }
        else{
            if(guard.getPosition().getX() == 650){
                destX -= 630;
                destY += 30;
            }
            else{
                destX += 630;
                destY += 30;
            }

        }

        previousTime = currentTime;
    }

    private boolean isAtDestination(Guard guard) {
        return destX == guard.getPosition().getX() && destY == guard.getPosition().getY();
    }

    private boolean seen(Guard guard) {
        this.guard = guard;
        //this.agents = guard.worldMap.getAgents();
        this.cones = guard.worldMap.getAgentsCones();
        // System.out.println("cone size: " + cones.size());
        for (Agent intruder : guard.worldMap.getAgents()) {
            if (intruder instanceof Intruder) {
                if (cones.contains(intruder.getPosition())) return true;

            }
        }
        return false;
    }
}

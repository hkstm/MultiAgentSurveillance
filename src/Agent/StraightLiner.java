package Agent;
import javafx.scene.paint.Color;

import javafx.geometry.Point2D;
import java.awt.Point;
import java.util.Random;
import java.util.ArrayList;
import java.util.List;
import static World.GameScene.SCALING_FACTOR;
import static World.WorldMap.EMPTY;
import static World.WorldMap.WALL;

/**
 * A subclass of Agent for the Intruders
 * @author Benjamin, Kailhan
 */

public class StraightLiner extends Intruder{
    protected boolean tired;
    protected final long createdMillis = System.currentTimeMillis();
    protected int sprintCounter = 5;
    protected int walkCounter = 10; //check if this is right (might be 10 sec not 15)


    /**
     * An Intruder constructor with an empty internal map
     * @param position is a point containing the coordinates of the Intruderq
     * @param direction is the angle which the agent is facing, this spans from -180 to 180 degrees
     */

    public StraightLiner(Point2D position, double direction) {
        super(position, direction);
        this.viewingAngle = 45;
        this.visualRange[0] = 0;
        this.visualRange[1] = 7.5;
        this.color = Color.LIGHTGOLDENRODYELLOW;
        this.tired = false;
    }

    public void gameTreeIntruder(double timeStep)
    {
        double walkingDistance = (BASE_SPEED *SCALING_FACTOR*timeStep);
        double sprintingDistance = (SPRINT_SPEED *SCALING_FACTOR*timeStep);
        updateWalls();
        if(!frozen)
        {
            open();
        }
        if(oldTempGoal != null)
        {
            checkChangedStatus();
        }
        double elapsedTime = (System.currentTimeMillis()-startTime)/1000;
        if(elapsedTime > freezeTime) {
            frozen = false;
            startTime = 0;
            freezeTime = 0;
            oldTempGoal = tempGoal;
            int[][] blocks = aStarTerrain(knownTerrain);
            Astar pathFinder = new Astar(knownTerrain[0].length, knownTerrain.length, (int) (position.getX() / SCALING_FACTOR), (int) (position.getY() / SCALING_FACTOR), (int) goalPosition.getX(), (int) goalPosition.getY(), blocks, this, false);
            List<Node> path = pathFinder.findPath();
            if (!changed) {
                tempGoal = new Point2D((path.get(path.size() - 1).row * SCALING_FACTOR) + (SCALING_FACTOR / 2), (path.get(path.size() - 1).column * SCALING_FACTOR) + (SCALING_FACTOR / 2));
                if (path.size() > 1) {
                    previousTempGoal = new Point2D((path.get(path.size() - 2).row * SCALING_FACTOR) + (SCALING_FACTOR / 2), (path.get(path.size() - 2).column * SCALING_FACTOR) + (SCALING_FACTOR / 2));
                } else {
                    previousTempGoal = tempGoal;
                }
            }
            wallPhaseDetection();
            cornerCorrection();
            double divisor = Math.abs(tempGoal.getY() - position.getY());
            double preDivisor = Math.abs(previousTempGoal.getY() - tempGoal.getY());
            if (divisor == 0) {
                divisor++;
            } else if (preDivisor == 0) {
                preDivisor++;
            }
            double turnAngle = Math.toDegrees(Math.atan(Math.abs(tempGoal.getX() - position.getX()) / divisor));
            performTurn(turnAngle);
            updateDirection(turnAngle);
            //if (oldPos == null) {
            //    tempOldPos = new Point((int) (position.getX() / SCALING_FACTOR), (int) (position.getY() / SCALING_FACTOR));
            //    first = true;
            //} else {
            //    tempOldPos = new Point((int) (position.getX() / SCALING_FACTOR), (int) (position.getY() / SCALING_FACTOR));
            //    first = false;
            //}
            //System.out.println("wall cout" + wallCount);
            //System.out.println("sprint percent" + sprintPercent);

            if (this.inVision(goalPosition) && !tired) {
                move(walkingDistance);
            } else {
                if (legalMoveCheck(walkingDistance)) {
                    //  System.out.println("sprint percent: " + sprintPercent + "tired?: " + tired + "im walking");
                    move(walkingDistance);
                }
            }

            //modify = false;
            //if (tempOldPos.x != (int) (position.getX() / SCALING_FACTOR) || tempOldPos.y != (int) (position.getY() / SCALING_FACTOR)) {
            //    if (!first && oldPos.x == (int) (position.getX() / SCALING_FACTOR) && oldPos.y == (int) (position.getY() / SCALING_FACTOR)) {
            //        alternatingCounter++;
            //    } else {
            //        alternatingCounter = 0;
            //    }
            //}
            //if (alternatingCounter == 6) {
            //    alternatingCounter = 0;
            //    //modify = true;
            //    points[0] = oldPos;
            //    points[1] = tempOldPos;
            //}
            //oldPos = tempOldPos;
        }
    }
}

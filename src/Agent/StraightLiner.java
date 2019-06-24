package Agent;
import javafx.scene.paint.Color;

import javafx.geometry.Point2D;
import java.awt.Point;
import java.util.Random;
import java.util.ArrayList;
import java.util.List;
import static World.GameScene.SCALING_FACTOR;
import static World.WorldMap.EMPTY;

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

    public void gameTreeIntruder(double timeStep) {
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
        if(elapsedTime > freezeTime)
        {
            frozen = false;
            startTime = 0;
            freezeTime = 0;
            oldTempGoal = tempGoal;
            int[][] blocks = aStarTerrain(knownTerrain);
            Astar pathFinder = new Astar(knownTerrain[0].length, knownTerrain.length, (int)(position.getX()/SCALING_FACTOR), (int)(position.getY()/SCALING_FACTOR), (int)goalPosition.getX(), (int)goalPosition.getY(), blocks, this, false);
            List<Node> path = pathFinder.findPath();
            if(!changed)
            {
                tempGoal = new Point2D((path.get(path.size()-1).row*SCALING_FACTOR)+(SCALING_FACTOR/2), (path.get(path.size()-1).column*SCALING_FACTOR)+(SCALING_FACTOR/2));
                if (path.size() > 1) {
                    previousTempGoal = new Point2D((path.get(path.size() - 2).row * SCALING_FACTOR) + (SCALING_FACTOR / 2), (path.get(path.size() - 2).column * SCALING_FACTOR) + (SCALING_FACTOR / 2));
                }
                else{
                    previousTempGoal = tempGoal;
                }
            }
            wallPhaseDetection();
            cornerCorrection();
            double divisor = Math.abs(tempGoal.getY()-position.getY());
            double preDivisor = Math.abs(previousTempGoal.getY()-tempGoal.getY());
            if(divisor == 0)
            {
                divisor++;
            }
            else if (preDivisor == 0){
                preDivisor++;
            }
            double turnAngle = Math.toDegrees(Math.atan(Math.abs(tempGoal.getX()-position.getX())/divisor));
            double previousAngle = Math.toDegrees(Math.atan(Math.abs(previousTempGoal.getX()-tempGoal.getX())/preDivisor));
            double finalAngle = previousAngle - turnAngle;
            if(tempGoal.getX() >= position.getX() && tempGoal.getY() <= position.getY())
            {
                updateDirection(turnAngle-90);
            }
            else if(tempGoal.getX() >= position.getX() && tempGoal.getY() > position.getY())
            {
                updateDirection(90-turnAngle);
            }
            else if(tempGoal.getX() < position.getX() && tempGoal.getY() > position.getY())
            {
                updateDirection(90+turnAngle);
            }
            else if(tempGoal.getX() < position.getX() && tempGoal.getY() <= position.getY())
            {
                updateDirection(270-turnAngle);
            }
            if(!tired)
            {
                if(legalMoveCheck(sprintingDistance))
                {
                    long nowMillis = System.currentTimeMillis();
                    int countSec = (int)((nowMillis - this.createdMillis) / 1000);
                    if (countSec != sprintCounter){
                        move(sprintingDistance);
                    }
                    else{
                        tired = true;
                        sprintCounter = sprintCounter + 15;
                    }
                }
            }
            if (tired)
            {
                if(legalMoveCheck(walkingDistance))
                {
                    long nowMillis = System.currentTimeMillis();
                    int countSec = (int)((nowMillis - this.createdMillis) / 1000);
                    if (countSec != walkCounter) {
                        move(walkingDistance);
                    }
                    else{
                        tired = false;
                        walkCounter += 15; //HAO please check if theses are the correct resting times
                    }
                }
            }
        }
    }
}
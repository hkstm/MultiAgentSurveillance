package Agent;

import java.awt.*;
import java.awt.geom.Point2D;

import static World.GameScene.SCALING_FACTOR;

public class AreaOptimizer extends Guard {

    public double[][] worldAreaReward;
    public final int REWARD_FACTOR = 1;
    double xgoal;
    double ygoal;

    public AreaOptimizer(Point2D.Double position, double direction) {
        super(position, direction);
        this.worldAreaReward = new double[worldMap.getSize()][worldMap.getSize()];
        //this.knownTerrain = worldMap.getWorldGrid();
    }

    public void run() {
        previousTime = System.nanoTime();
        previousPosition = new Point2D.Double(position.getX(), position.getY());
        xgoal = MoveTo.destX;
        ygoal = MoveTo.destY;
        goalPosition = new Point2D.Double(xgoal, ygoal);
        while(!exitThread) {
            currentTime = System.nanoTime();
            delta = (currentTime - previousTime)/1e9; //makes it in seconds
            previousTime = currentTime;
            currentSpeed = ((position.distance(previousPosition)/SCALING_FACTOR)/delta);
            previousPosition.setLocation(position.getX(), position.getY());
            checkForAgentSound();

            updateGoalPosition();
            xGoal = getGoalPosition().getX();
            yGoal = getGoalPosition().getY();
        }
    }

    /**
     * updates the internal map of an Agent based on their field of vision based on sounds of sightings of other agents
     * or in the case of intruders, sighting of new terrain
     * @param radius is the distance an Agent can see in front of them
     * @param angle is the width of view of an Agent
     */
    public void updateWorldAreaReward(double radius, double angle, double delta) {
        double[][] tmpWorldAreaCopy = new double[worldAreaReward.length][worldAreaReward[0].length];
        for(int r = 0; r < worldAreaReward.length; r++) {
            for(int c = 0; c < worldAreaReward[0].length; c++) {
//                if(worldMap.notEnterableTile(worldMap.getTileState(r,c)))
                worldAreaReward[r][c] += (REWARD_FACTOR * delta);
                tmpWorldAreaCopy[r][c] = worldAreaReward[r][c];
            }
        }

    }

    public void updateGoalPosition() {
        //some logic with the worldMap and whatever algorithms we are using
        double xCurr = 200;
        double yCurr = 200;
        for(int r = 0; r < worldAreaReward.length; r++) {
            for(int c = 0; c < worldAreaReward[0].length; c++) {
                
            }
        }
        double xGoal = xCurr;
        double yGoal = yCurr;
        goalPosition.setLocation(xGoal, yGoal);
    }
}

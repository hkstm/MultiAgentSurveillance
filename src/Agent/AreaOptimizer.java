package Agent;

import World.WorldMap;

import java.awt.geom.Point2D;

import static World.GameScene.SCALING_FACTOR;

public class AreaOptimizer extends Guard {

    public double[][] worldAreaReward;

    public AreaOptimizer(Point2D.Double position, double direction) {
        super(position, direction);
        this.worldAreaReward = new double[worldMap.getSize()][worldMap.getSize()];
        //this.knownTerrain = worldMap.getWorldGrid();
    }

    public void run() {
        previousTime = System.nanoTime();
        previousPosition = new Point2D.Double(position.getX(), position.getY());
        goalPosition = new Point2D.Double(200, 200);
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

    public void updateGoalPosition() {
        //some logic with the worldMap and whatever algorithms we are using
        double x = 200;
        double y = 200;
        goalPosition.setLocation(x, y);
    }
}

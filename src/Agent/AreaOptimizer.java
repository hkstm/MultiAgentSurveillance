package Agent;

import java.awt.*;
import javafx.geometry.Point2D;

import static World.GameScene.SCALING_FACTOR;

public class AreaOptimizer extends Guard {

    private Point2DReward[][] worldAreaReward;
    private double score;
    public final double REWARD_FACTOR = 1;


    public AreaOptimizer(Point2D position, double direction) {
        super(position, direction);
        this.worldAreaReward = new Point2DReward[worldMap.getSize()][worldMap.getSize()];
        for(int r = 0; r < worldMap.getSize(); r++) {
            for(int c = 0; c < worldMap.getSize(); c++) {
                worldAreaReward[r][c] = new Point2DReward(c, r, 10);
            }
        }
        //this.knownTerrain = worldMap.getWorldGrid();
    }

    public void run() {
        previousTime = System.nanoTime();
        previousPosition = new Point2D(position.getX(), position.getY());
        goalPosition = new Point2D(200, 200);
        while(!exitThread) {
            currentTime = System.nanoTime();
            delta = (currentTime - previousTime)/1e9; //makes it in seconds
            previousTime = currentTime;
            createCone();
            updateWorldAreaReward(delta);
            direction = getMoveAngle();
            System.out.println(direction);
            currentSpeed = ((position.distance(previousPosition)/SCALING_FACTOR)/delta);
            previousPosition= new Point2D(position.getX(), position.getY());
            checkForAgentSound();
        }
    }

    public void updateWorldAreaReward(double delta) {
        for(int r = 0; r < worldAreaReward.length; r++) {
            for(int c = 0; c < worldAreaReward[0].length; c++) {
                //check if middle of tile is in cone
                if(viewingCone.contains(worldMap.convertArrayToWorld(c) + 0.5 * worldMap.convertArrayToWorld(1),
                        worldMap.convertArrayToWorld(r) + 0.5 * worldMap.convertArrayToWorld(1))) {
                    score += worldAreaReward[r][c].consumeReward();
                    worldAreaReward[r][c].resetReward();
//                    System.out.println("reward reset for r: " + r + " c: " + c);
                } else {
                    worldAreaReward[r][c].updateReward (REWARD_FACTOR * delta);
                }
            }
        }

    }

    public double getMoveAngle() {
        double x = 0;
        double y = 0;
        double totalReward = 0;
        for(int r = 0; r < worldAreaReward.length; r++) {
            for(int c = 0; c < worldAreaReward[0].length; c++) {
                totalReward += worldAreaReward[r][c].getReward();
                x += (worldMap.convertArrayToWorld(c)) * worldAreaReward[r][c].getReward();
                y += (worldMap.convertArrayToWorld(r)) * worldAreaReward[r][c].getReward();
            }
        }
        x /= totalReward;
        y /= totalReward;
        x += SCALING_FACTOR;
        y += SCALING_FACTOR;
        System.out.println("x: " + x + " y: " + y);
//        printWorldAreaReward();
        return Math.toDegrees(Math.atan2((y - position.getY()), (x - position.getX())));
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
        goalPosition = new Point2D(xGoal, yGoal);
    }

    public void printWorldAreaReward() {
        for(int r = 0; r < worldAreaReward.length; r++) {
            for(int c = 0; c < worldAreaReward.length; c++) {
                System.out.printf("%f", worldAreaReward[r][c].getReward());
            }
            System.out.println();
        }
    }
}

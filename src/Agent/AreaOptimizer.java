package Agent;

import javafx.geometry.Point2D;

import static World.GameScene.SCALING_FACTOR;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

/**
 * AreaOptimizer Guard bot
 * @Author Kailhan
 */

public class AreaOptimizer extends Guard {

    private Point2DReward[][] worldAreaReward;
    private double score;
    public final double REWARD_FACTOR = 0.1;
    private Point2D goalPosition;

    /**
     * Calls normal Guard constructor and set rewards area
     * @param position
     * @param direction
     */
    public AreaOptimizer(Point2D position, double direction) {
        super(position, direction);
        this.worldAreaReward = new Point2DReward[worldMap.getSize()][worldMap.getSize()];
        for(int r = 0; r < worldMap.getSize(); r++) {
            for(int c = 0; c < worldMap.getSize(); c++) {
                //initializing reward array with fixed flat reward
                worldAreaReward[r][c] = new Point2DReward(c, r, 10);
            }
        }
    }

    /**
     * Starts the thread and everything in the while loop will keep on executing untill you call worldMap.stopAgent(//this agent//)
     */
    public void run() {
        previousTime = System.nanoTime();
        previousPosition = new Point2D(position.getX(), position.getY());
        goalPosition = new Point2D(200, 200);
        //this loop will keep on getting executed as long as the correspond thread is running
        while(!exitThread) {
            executeAgentLogic();
        }
    }

    /**
     * Used instead of the run method if we want to manually control when the agent should update
     */
    public void forceUpdate() {
        if(firstRun) {
            previousTime = System.nanoTime();
            previousPosition = new Point2D(position.getX(), position.getY());
            firstRun = false;
        }
        executeAgentLogic();
    }

    /**
     * Logic that gets executed every tick
     */
    public void executeAgentLogic() {
        currentTime = System.nanoTime();
        delta = (currentTime - previousTime)/1e9; //makes it in seconds
        previousTime = currentTime;
        updateKnownTerrain();
        //update vision
        createCone();
        updateWorldAreaReward(delta);
        updateDirection(getMoveDirection());
        currentSpeed = ((position.distance(previousPosition)/SCALING_FACTOR)/delta);
        previousPosition= new Point2D(position.getX(), position.getY());
        checkForAgentSound();


        double walkingDistance = (1.4 * SCALING_FACTOR) * (delta);
        if (legalMoveCheck(walkingDistance)) {
            move(walkingDistance);
        }
    }

    public void updateWorldAreaReward(double delta) {
        for(int r = 0; r < worldAreaReward.length; r++) {
            for(int c = 0; c < worldAreaReward[0].length; c++) {
                //check if middle of tile is in cone
                if(viewingCone.contains(worldMap.convertArrayToWorld(c-1) + 1 * worldMap.convertArrayToWorld(1),
                        worldMap.convertArrayToWorld(r-1) + 1 * worldMap.convertArrayToWorld(1))) {
                    score += worldAreaReward[r][c].consumeReward();
                    worldAreaReward[r][c].resetReward();
//                    System.out.println("reward reset for r: " + r + " c: " + c);
                } else {
                    worldAreaReward[r][c].updateReward (REWARD_FACTOR * delta);
                }
            }
        }
    }

    /**
     * Calculates based on the reward area what the best point is to go
     * @return the angle between the best point to go and current position
     */
    public double getMoveDirection() {
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
//        int[][] blocks = aStarTerrain(knownTerrain);
//        Astar pathFinder = new Astar(knownTerrain[0].length, knownTerrain.length, (int)(position.getX()/SCALING_FACTOR), (int)(position.getY()/SCALING_FACTOR), (int)(x/SCALING_FACTOR), (int)(y/SCALING_FACTOR), blocks);
//        List<Node> path = new ArrayList<Node>();
//        path = pathFinder.findPath();
//        Point2D goal = new Point2D((path.get(path.size()-1).i*SCALING_FACTOR)+(SCALING_FACTOR/2), (path.get(path.size()-1).j*SCALING_FACTOR)+(SCALING_FACTOR/2));
        Point2D goal = new Point2D(x, y);
//        System.out.println("x: " + x + " y: " + y);
//        printWorldAreaReward();
        return Math.toDegrees(Math.atan2((goal.getY() - position.getY()), (goal.getX() - position.getX())));
    }

    /**
     * Diagnostic print of rewards area
     */
    public void printWorldAreaReward() {
        for(int r = 0; r < worldAreaReward.length; r++) {
            for(int c = 0; c < worldAreaReward.length; c++) {
                System.out.printf("%f", worldAreaReward[r][c].getReward());
            }
            System.out.println();
        }
    }
}

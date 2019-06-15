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
    public final double NOT_SEEN_REWARD = 100;
    public final double INTRUDER_BONUS_REWARD = 10;
    public final double RECENT_AREA_PENALTY = 0;
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
//        for(int r = 0; r < worldMap.getSize(); r++) {
//            for(int c = 0; c < 10; c++) {
//                //initializing reward array with fixed flat reward
//                worldAreaReward[r][c] = new Point2DReward(c, r, 100);
//            }
//        }
    }

    /**
     * Logic that gets executed every tick
     */
    public void executeAgentLogic() {
        updateWorldAreaReward(delta);
        updateDirection(getMoveDirection());
        double walkingDistance = (1.4 * SCALING_FACTOR) * (delta);
        if (legalMoveCheck(walkingDistance)) {
            move(walkingDistance);
        }
//        move(walkingDistance);
        updatePerformanceCriteria();
    }

    public void updateWorldAreaReward(double delta) {
        for(int r = 0; r < worldAreaReward.length; r++) {
            for(int c = 0; c < worldAreaReward[0].length; c++) {
                //check if middle of tile is in cone
                if(viewingCone.contains(worldMap.convertArrayToWorld(c-1) + 1 * worldMap.convertArrayToWorld(1),
                        worldMap.convertArrayToWorld(r-1) + 1 * worldMap.convertArrayToWorld(1))) {
                    for(Agent agent : worldMap.getAgents()) {
                        if((locationToWorldgrid(agent.getPosition().getX()) == c) && (locationToWorldgrid(agent.getPosition().getY()) == r)) {
                            worldAreaReward[r][c].updateReward(INTRUDER_BONUS_REWARD * delta);
                        }
                    }
                    score += worldAreaReward[r][c].consumeReward();
                    worldAreaReward[r][c].resetReward();
//                    worldAreaReward[r][c].updateReward(RECENT_AREA_PENALTY * delta);
//                    System.out.println("reward: " + worldAreaReward[r][c].getReward());
//                    System.out.println("reward reset for r: " + r + " c: " + c);
//                    System.out.println("viewingcone contains tile: " + worldMap.convertArrayToWorld(c-1) + 1 * worldMap.convertArrayToWorld(1) + " r/y: " + worldMap.convertArrayToWorld(r-1) + 1 * worldMap.convertArrayToWorld(1));
                } else {
                    if(!worldMap.isVisionObscuring(worldMap.getTileState(r, c))) {
//                        System.out.println("rewardnotseen: " + worldAreaReward[r][c].getReward());
                        worldAreaReward[r][c].updateReward(NOT_SEEN_REWARD * delta);
                    }
                }
//                System.out.println("reward: " + worldAreaReward[r][c].getReward());
            }
        }
    }

    /**
     * Calculates based on the reward area what the best point is to go
     * @return the angle between the best point to go and current position
     */
    public double getMoveDirection() {
        for(Agent intruder : worldMap.getAgents()) {
            if(intruder instanceof Intruder) {
                if(viewingCone.contains(intruder.getPosition())) return Math.toDegrees(Math.atan2((intruder.getPosition().getY() - position.getY()), (intruder.getPosition().getX() - position.getX())));
            }
        }
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
        x += worldMap.convertArrayToWorld(1)/2; //correction to middle of tile instead of left top
        y += worldMap.convertArrayToWorld(1)/2; //correction to middle of tile instead of left top
//        int[][] blocks = aStarTerrain(knownTerrain);
//        Astar pathFinder = new Astar(knownTerrain[0].length, knownTerrain.length, (int)(position.getX()/SCALING_FACTOR), (int)(position.getY()/SCALING_FACTOR), (int)(x/SCALING_FACTOR), (int)(y/SCALING_FACTOR), blocks);
//        List<Node> path = new ArrayList<Node>();
//        path = pathFinder.findPath();
//        Point2D goal = new Point2D((path.get(path.size()-1).row*SCALING_FACTOR)+(SCALING_FACTOR/2), (path.get(path.size()-1).column*SCALING_FACTOR)+(SCALING_FACTOR/2));
        Point2D goal = new Point2D(x, y);
//        System.out.println("x: " + x + " y: " + y);
//        printWorldAreaReward();
//        System.out.println("goalpoint x: " + x + " y: " + y);
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

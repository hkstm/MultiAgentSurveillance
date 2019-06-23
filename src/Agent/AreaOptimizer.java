package Agent;

import javafx.geometry.Point2D;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Shape;

import static World.GameScene.SCALING_FACTOR;
import static World.GameScene.random;
import static World.WorldMap.isVisionObscuring;

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
//        double previousWeight = 1;
//        double newWeight = 5;
//        double totalWeight = previousWeight + newWeight;
//        updateDirection(((direction*previousWeight)+(getMoveDirection()*newWeight)/totalWeight));
        updateDirection(getMoveDirection());
        double walkingDistance = (BASE_SPEED * SCALING_FACTOR) * (delta);
        Point2D newPosition = new Point2D((position.getX() + (walkingDistance * Math.cos(Math.toRadians(direction)))), (position.getY() + (walkingDistance * Math.sin(Math.toRadians(direction)))));
        if(!isVisionObscuring(worldMap.getTileState(locationToWorldgrid(newPosition.getY()), locationToWorldgrid(newPosition.getX())))) {
            position = newPosition;
        }
        updatePerformanceCriteria();
    }

    public void updateWorldAreaReward(double delta) {
        Shape worldAreaCone = createCone(visualRange[0], visualRange[1]*10, viewingAngle*2);
        Shape worldAreaConeTiny = createCone(visualRange[0], visualRange[1]*5, viewingAngle/4);
        for(int r = 0; r < worldAreaReward.length; r++) {
            for(int c = 0; c < worldAreaReward[0].length; c++) {
                //check if middle of tile is in cone
                if(worldAreaCone.contains(worldMap.convertArrayToWorld(c) + 0.5 * worldMap.convertArrayToWorld(1),
                        worldMap.convertArrayToWorld(r) + 0.5 * worldMap.convertArrayToWorld(1))) {
                    score += worldAreaReward[r][c].getReward();
//                    worldAreaReward[r][c].resetReward();
                    worldAreaReward[r][c].updateReward(-1 * NOT_SEEN_REWARD * delta);
//                    System.out.println("reward seen: " + worldAreaReward[r][c] + worldAreaReward[r][c].getReward());
                    for (Agent agent : worldMap.getAgents()) {
                        if (agent instanceof Intruder) {
                            if ((locationToWorldgrid(agent.getPosition().getX()) == c) && (locationToWorldgrid(agent.getPosition().getY()) == r)) {
                                worldAreaReward[r][c].updateReward(INTRUDER_BONUS_REWARD * delta);
                            }
                        }
                    }
//                    worldAreaReward[r][c].updateReward(RECENT_AREA_PENALTY * delta);
//                    System.out.println("reward: " + worldAreaReward[r][c].getReward());
//                    System.out.println("reward reset for r: " + r + " c: " + c);
//                    System.out.println("viewingcone contains tile: " + worldMap.convertArrayToWorld(c-1) + 1 * worldMap.convertArrayToWorld(1) + " r/y: " + worldMap.convertArrayToWorld(r-1) + 1 * worldMap.convertArrayToWorld(1));
                } else if(worldAreaCone.contains(worldMap.convertArrayToWorld(c-1) + 1 * worldMap.convertArrayToWorld(1),
                        worldMap.convertArrayToWorld(r-1) + 1 * worldMap.convertArrayToWorld(1))){
//                        worldAreaReward[r][c].updateReward(-2*NOT_SEEN_REWARD * delta);

                } else if(worldAreaConeTiny.contains(worldMap.convertArrayToWorld(c-1) + 1 * worldMap.convertArrayToWorld(1),
                    worldMap.convertArrayToWorld(r-1) + 1 * worldMap.convertArrayToWorld(1))){
//                    worldAreaReward[r][c].updateReward(2*NOT_SEEN_REWARD * delta);
                } else {
                    if(!isVisionObscuring(worldMap.getTileState(r, c))) {
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
//        x = worldMap.convertArrayToWorld(worldAreaReward.length) - x;
//        y = worldMap.convertArrayToWorld(worldAreaReward.length) - y;
        goalPosition = new Point2D(x, y);

        Point2D posFacing = new Point2D(position.getX() + (10 * Math.cos(Math.toRadians(direction))), position.getY() + (10 * Math.sin(Math.toRadians(direction))));
        double result = Math.toDegrees(Math.atan2(goalPosition.getY() - position.getY(), goalPosition.getX() - position.getX()) - Math.atan2(posFacing.getY() - position.getY(), posFacing.getX() - position.getX()));

        double result2 = ( result < 0) ? 360 + result : result;
        if(result <= 90 || prevGoalPosition == null) prevGoalPosition = new Point2D(goalPosition.getX(), goalPosition.getY());

//        System.out.println("degrees: " + result);
        int[][] blocks = aStarTerrain(knownTerrain);
        Astar pathFinder = new Astar(knownTerrain[0].length, knownTerrain.length, locationToWorldgrid(position.getX()), locationToWorldgrid(position.getY()),
                locationToWorldgrid(prevGoalPosition.getX()), locationToWorldgrid(prevGoalPosition.getY()), blocks, this);
        List<Node> path = pathFinder.findPath();
        goalPositionPath = new Point2D((path.get(path.size()-1).row*SCALING_FACTOR)+(SCALING_FACTOR/2), (path.get(path.size()-1).column*SCALING_FACTOR)+(SCALING_FACTOR/2));

        double angle = Math.toDegrees(Math.atan2(goalPositionPath.getY() - position.getY(), goalPositionPath.getX() - position.getX()) - Math.atan2(posFacing.getY() - position.getY(), posFacing.getX() - position.getX()));
        angle = (angle > 180) ? angle - 360 : angle;
        angle = (angle < -180) ? angle + 360 : angle;
        return this.direction+angle;
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

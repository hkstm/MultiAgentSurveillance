package Agent;

import com.sun.javafx.scene.paint.GradientUtils;
import javafx.geometry.Point2D;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Shape;

import static World.GameScene.SCALING_FACTOR;
import static World.GameScene.random;
import static World.WorldMap.*;
import javafx.geometry.Point2D;
import javafx.scene.shape.Shape;

import static World.GameScene.SCALING_FACTOR;
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
    public static final double NOT_SEEN_REWARD = 8;
    public static final double INTRUDER_BONUS_REWARD = 16;
    public static final double RECENT_AREA_PENALTY = 0;
    public static final double MED_INTEREST = 2;
    public static final double HIGH_INTEREST = 8;
    public static final double MAX_INTEREST = 16;
    public static final double AUDIO_REWARD = 8;
    private double explorationFactor = 1;
    private ArrayList<PointOfInterest> pointsOfInterest;

    /**
     * Calls normal Guard constructor and set rewards area
     * @param position
     * @param direction
     */
    public AreaOptimizer(Point2D position, double direction, double explorationFactor) {
        super(position, direction);
        this.worldAreaReward = new Point2DReward[worldMap.getSize()][worldMap.getSize()];
        this.pointsOfInterest = new ArrayList<PointOfInterest>();
        this.explorationFactor = explorationFactor;
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

    public AreaOptimizer(Point2D position, double direction){
        this(position, direction, 1);
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
        updateDirectionNoBlind(getMoveDirection());
        double walkingDistance = (BASE_SPEED * SCALING_FACTOR) * (delta);
        Point2D newPosition = new Point2D((position.getX() + (walkingDistance * Math.cos(Math.toRadians(direction)))), (position.getY() + (walkingDistance * Math.sin(Math.toRadians(direction)))));
        if(isEmpty(worldMap.getTileState(locationToWorldgrid(newPosition.getY()), locationToWorldgrid(newPosition.getX())))) {
            position = newPosition;
        }
        updatePerformanceCriteria();
    }

    /**
     * Updates the worldAreaReward different cones are used then the actual vision cones because of practical performance
     * @param delta timestep
     */
    public void updateWorldAreaReward(double delta) {
        Shape worldAreaCone = createCone(visualRange[0], visualRange[1]*10, viewingAngle*2);
        for(int r = 0; r < worldAreaReward.length; r++) {
            for(int c = 0; c < worldAreaReward[0].length; c++) {
                //check if middle of tile is in cone
                if(worldAreaCone.contains(worldMap.convertArrayToWorld(c) + 0.5 * worldMap.convertArrayToWorld(1),
                        worldMap.convertArrayToWorld(r) + 0.5 * worldMap.convertArrayToWorld(1))) {
                    score += worldAreaReward[r][c].getReward();
                    worldAreaReward[r][c].resetReward();
//                    worldAreaReward[r][c].updateReward(-1 * NOT_SEEN_REWARD * delta);
//                    System.out.println("reward seen: " + worldAreaReward[r][c] + worldAreaReward[r][c].getReward());
                    for (Agent agent : worldMap.getAgents()) {
                        if (agent instanceof Intruder) {
                            if ((locationToWorldgrid(agent.getPosition().getX()) == c) && (locationToWorldgrid(agent.getPosition().getY()) == r)) {
                                worldAreaReward[r][c].updateReward(INTRUDER_BONUS_REWARD * delta);
                            }
                        }
                    }
                    if(worldMap.getTileStatePhero(r, c) == MARKER_1){
                        worldAreaReward[r][c].updateReward(INTRUDER_BONUS_REWARD * delta);
                    }
                    if(worldMap.getTileStatePhero(r, c) == MARKER_2) {
                        worldAreaReward[r][c].resetReward();
                    } else if(isEmpty(worldMap.getTileState(r, c))) {
                        worldAreaReward[r][c].updateReward(NOT_SEEN_REWARD * delta);
                    }
                }
            }
        }
        updateWorldAreaRewardPOI(delta);
        updateWorldAreaRewardAudio(delta);
    }

    public void updateWorldAreaRewardAudio(double delta) {
        while(audioLogs.size() > 0) {
            Shape audioCone = createCone(visualRange[0], visualRange[1]*10,10, audioLogs.get(0).getDirection());
            for(int r = 0; r < worldAreaReward.length; r++) {
                for(int c = 0; c < worldAreaReward[0].length; c++) {
                    //check if middle of tile is in cone
                    if(audioCone.contains(worldMap.convertArrayToWorld(c) + 0.5 * worldMap.convertArrayToWorld(1),
                            worldMap.convertArrayToWorld(r) + 0.5 * worldMap.convertArrayToWorld(1))) {
                        worldAreaReward[r][c].updateReward(AUDIO_REWARD * delta);
                    }
                }
//                System.out.println("reward: " + worldAreaReward[r][c].getReward());
            }
            audioLogs.remove(0);

        }
    }

    /**
     * Adds an extra reward for points of interest this scales with how much of the map has been explored, the more it gets explored
     * the less focus on exploration and the more on visiting these important areas
     * @param delta timestep
     */
    public void updateWorldAreaRewardPOI(double delta){
        updatePointsOfInterest();
        int unexploredCounter = 0;
        int totalCounter = (knownTerrain.length) * (knownTerrain.length);
        for(int r = 0; r < knownTerrain.length; r++) {
            for(int c = 0; c < knownTerrain.length; c++) {
                if(knownTerrain[r][c] == UNEXPLORED) unexploredCounter++;
            }
        }
        for(PointOfInterest poi : pointsOfInterest) {
            worldAreaReward[poi.getY()][poi.getX()].updateReward(poi.getInterestFactor() * (((double)unexploredCounter/(double)totalCounter)/explorationFactor) * delta);
        }
    }

    /**
     * Calculates based on the reward area what the best point is to go
     * @return the angle between the best point to go and current position
     */
    public double getMoveDirection() {
        chasing = false;
        for(Agent agent: worldMap.getAgents()){
            if(agent instanceof Intruder && this.inVision(agent.getPosition())) {
                Point2D posFacing = new Point2D(position.getX() + (10 * Math.cos(Math.toRadians(direction))), position.getY() + (10 * Math.sin(Math.toRadians(direction))));
//        System.out.println("degrees: " + result);
                double angle = Math.toDegrees(Math.atan2(agent.getPosition().getY() - position.getY(), agent.getPosition().getX() - position.getX()) - Math.atan2(posFacing.getY() - position.getY(), posFacing.getX() - position.getX()));
                angle = (angle > 180) ? angle - 360 : angle;
                angle = (angle < -180) ? angle + 360 : angle;
                this.direction += angle;
                chasing = true;
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
//        System.out.println("result: "  + result2);
        if(result2 <= 90 || prevGoalPosition == null) prevGoalPosition = new Point2D(goalPosition.getX(), goalPosition.getY());

//        System.out.println("degrees: " + result);
        int[][] blocks = aStarTerrain(knownTerrain);
        Astar pathFinder = new Astar(knownTerrain[0].length, knownTerrain.length, locationToWorldgrid(position.getX()), locationToWorldgrid(position.getY()),
                locationToWorldgrid(prevGoalPosition.getX()), locationToWorldgrid(prevGoalPosition.getY()), blocks, this, false);
        List<Node> path = pathFinder.findPath();
        boolean exception =false;
        try{
            goalPositionPath = new Point2D((path.get(path.size()-1).row*SCALING_FACTOR)+(SCALING_FACTOR/2), (path.get(path.size()-1).column*SCALING_FACTOR)+(SCALING_FACTOR/2));
        } catch(Exception e){
//            System.out.println("OOB");
            exception = true;
            for(int r = 0; r < worldMap.getSize(); r++) {
                for(int c = 0; c < worldMap.getSize(); c++) {
                    //initializing reward array with fixed flat reward
                    worldAreaReward[r][c] = new Point2DReward(c, r, 10);
                }
            }
            pointsOfInterest.clear();
            prevGoalPosition = null;
        }
//        if(exception) System.out.println("continue");

        double angle = Math.toDegrees(Math.atan2(goalPositionPath.getY() - position.getY(), goalPositionPath.getX() - position.getX()) - Math.atan2(posFacing.getY() - position.getY(), posFacing.getX() - position.getX()));
        angle = (angle > 180) ? angle - 360 : angle;
        angle = (angle < -180) ? angle + 360 : angle;
        return (this.direction + angle);
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

    /**
     * Updates list of points of interest, structures, door, windows, open door and open window as those are place intruders
     * are to be thought to be add
     */
    public void updatePointsOfInterest() {
        for (int r = 0; r < worldAreaReward.length; r++) {
            for (int c = 0; c < worldAreaReward[0].length; c++) {
                //check if middle of tile is in cone
                if (this.inVision(worldMap.convertArrayToWorld(c) + 0.5 * worldMap.convertArrayToWorld(1),
                        worldMap.convertArrayToWorld(r) + 0.5 * worldMap.convertArrayToWorld(1))) {
                    int tileState = worldMap.getTileState(r, c);
                    PointOfInterest tmpPoint = null;
                    boolean alreadyAdded = false;
                    if(medInterest(tileState)) tmpPoint = new PointOfInterest(c, r, tileState, MED_INTEREST);
                    if(highInterest(tileState)) tmpPoint = new PointOfInterest(c, r, tileState, HIGH_INTEREST);
                    if(maxInterest(tileState)) tmpPoint = new PointOfInterest(c, r, tileState, MAX_INTEREST);
                    if(tmpPoint != null) {
                        for(PointOfInterest poi : pointsOfInterest) {
                            if(poi.equals(tmpPoint)) {
                                alreadyAdded = true;
                                break;
                            }
                        }
                        if(!alreadyAdded) pointsOfInterest.add(tmpPoint);
                    }
                }
            }
        }
    }

    public boolean highInterest(int toCheck) {
        if(toCheck == DOOR || toCheck == WINDOW) return true;
        return false;
    }

    public boolean maxInterest(int toCheck) {
        if(toCheck == OPEN_DOOR || toCheck == OPEN_WINDOW) return true;
        return false;
    }

    public boolean medInterest(int toCheck) {
        if(toCheck == STRUCTURE) return true;
        return false;
    }


}

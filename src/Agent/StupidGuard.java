package Agent;

import javafx.geometry.Point2D;

import java.sql.SQLOutput;
import java.util.List;
import java.util.Random;

import static World.GameScene.SCALING_FACTOR;
import static World.WorldMap.isEmpty;
import static World.WorldMap.isVisionObscuring;

public class StupidGuard extends Guard{

    private double directionToGo;
    private boolean turningToDirection;
    private boolean chasing;
    private static final Random random = new Random();

    public StupidGuard(Point2D position, double direction) {
        super(position, direction);
        this.turningToDirection = false;
        this.chasing = false;
    }

    public void executeAgentLogic() {
        double walkingDistance = (BASE_SPEED * SCALING_FACTOR) * (delta);
        chasing = false;
        for(Agent agent: worldMap.getAgents()){
            if(agent instanceof Intruder && viewingCone.contains(agent.getPosition())) {
                Point2D posFacing = new Point2D(position.getX() + (10 * Math.cos(Math.toRadians(direction))), position.getY() + (10 * Math.sin(Math.toRadians(direction))));
//        System.out.println("degrees: " + result);
                int[][] blocks = aStarTerrain(knownTerrain);
                Astar pathFinder = new Astar(knownTerrain[0].length, knownTerrain.length, locationToWorldgrid(position.getX()), locationToWorldgrid(position.getY()),
                        locationToWorldgrid(agent.getPosition().getX()), locationToWorldgrid(agent.getPosition().getY()), blocks, this, false);
                List<Node> path = pathFinder.findPath();
                goalPositionPath = new Point2D((path.get(path.size()-1).row*SCALING_FACTOR)+(SCALING_FACTOR/2), (path.get(path.size()-1).column*SCALING_FACTOR)+(SCALING_FACTOR/2));
                double angle = Math.toDegrees(Math.atan2(goalPositionPath.getY() - position.getY(), goalPositionPath.getX() - position.getX()) - Math.atan2(posFacing.getY() - position.getY(), posFacing.getX() - position.getX()));
                angle = (angle > 180) ? angle - 360 : angle;
                angle = (angle < -180) ? angle + 360 : angle;
                this.direction += angle;
                chasing = true;
            }
        }
        Point2D newPosition = null;
        if(!turningToDirection) {
            newPosition = new Point2D((position.getX() + (walkingDistance * Math.cos(Math.toRadians(direction)))), (position.getY() + (walkingDistance * Math.sin(Math.toRadians(direction)))));
            if(isEmpty(worldMap.getTileState(locationToWorldgrid(newPosition.getY()), locationToWorldgrid(newPosition.getX())))) {
                position = newPosition;
            } else {
                Point2D newPositionToGo = null;
                do {
                    System.out.println("direction not empty: " + directionToGo);
                    directionToGo = random.nextDouble() * 360;
                    newPositionToGo = new Point2D((position.getX() + (walkingDistance * Math.cos(Math.toRadians(directionToGo)))), (position.getY() + (walkingDistance * Math.sin(Math.toRadians(directionToGo)))));
                } while (!isEmpty(worldMap.getTileState(locationToWorldgrid(newPositionToGo.getY()), locationToWorldgrid(newPositionToGo.getX()))));
                turningToDirection = true;
            }
        } else {
            System.out.println("turning to direction: " + directionToGo);
            updateDirection(directionToGo);
            if(Math.abs(direction - directionToGo) < 0.1) turningToDirection = false;
        }
        updatePerformanceCriteria();
    }
}
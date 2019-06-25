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
    private static final Random random = new Random();

    public StupidGuard(Point2D position, double direction) {
        super(position, direction);
        this.turningToDirection = false;

    }

    public void executeAgentLogic() {
        double walkingDistance = (BASE_SPEED * SCALING_FACTOR) * (delta);
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
        Point2D newPosition = null;
        if(!turningToDirection) {
            newPosition = new Point2D((position.getX() + (walkingDistance * Math.cos(Math.toRadians(direction)))), (position.getY() + (walkingDistance * Math.sin(Math.toRadians(direction)))));
            if(isEmpty(worldMap.getTileState(locationToWorldgrid(newPosition.getY()), locationToWorldgrid(newPosition.getX())))) {
                position = newPosition;
            } else {
                Point2D newPositionToGo = null;
                do {
                    directionToGo = random.nextDouble() * 360;
                    newPositionToGo = new Point2D((position.getX() + (walkingDistance * Math.cos(Math.toRadians(directionToGo)))), (position.getY() + (walkingDistance * Math.sin(Math.toRadians(directionToGo)))));
                } while (!isEmpty(worldMap.getTileState(locationToWorldgrid(newPositionToGo.getY()), locationToWorldgrid(newPositionToGo.getX()))));
                turningToDirection = true;
            }
        } else {
//            System.out.println("turning to direction: " + directionToGo);
            updateDirection(directionToGo);

            double abs = Math.abs(direction - directionToGo);
//            System.out.println("Math.abs(direction - directionToGo): " + abs);
            if(abs < 0.1 || abs > 359.9) turningToDirection = false;
        }

        updatePerformanceCriteria();
    }
}
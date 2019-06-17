package Agent;

import World.WorldMap;
import javafx.geometry.Point2D;


import static Agent.Agent.BASE_SPEED;
import static World.GameScene.SCALING_FACTOR;
/**
 * this routine makes the agent move from a point a to a target
 * @author Thibaut Donis
 */
public class MoveTo extends Routine {
     static double destX;
     static double destY;
    public static WorldMap worldMap;
    protected double currentTime;
    protected double delta;
    protected boolean exitThread;
    protected double previousTime;
    protected Point2D previousPosition;
    protected volatile Point2D goalPosition;
    double currentSpeed;

    @Override
    public void start() {
        super.start();
    }

    public MoveTo(double destX, double destY) {
        super();
        this.destX = destX;
        this.destY = destY;
    }

    public void reset() {
        start();
    }
    @Override
    public void act(Guard guard, WorldMap worldMap) {
        if(isWalking()){
            if(!isAtDestination(guard)){
                Move(guard);
            }
        }
    }
    private void Move(Guard guard) {
        while(!isAtDestination(guard)){
            previousTime = System.nanoTime();
            previousPosition = new Point2D(guard.position.getX(), guard.position.getY());
            /**
             * DONT REMOVE THIS GOALPOSITION THING IT IS NECESSARY FOR SOME REASON
             */
            goalPosition = new Point2D(destX, destY);
            //goalPosition = new Point2D.Double(200, 200);
            // Intruder intruder = new Intruder(position, direction);


            while(!exitThread) {
                currentTime = System.nanoTime();
                delta = currentTime - previousTime;
                delta /= 1e9; //makes it in seconds
                previousTime = currentTime;
                currentSpeed = ((guard.position.distance(previousPosition)/SCALING_FACTOR)/delta);
                //System.out.println("currentSpeed:" + currentSpeed);
                previousPosition = new Point2D(guard.position.getX(), guard.position.getY());
                guard.checkForAgentSound();
                double walkingDistance = (BASE_SPEED * SCALING_FACTOR) * (delta);
                if(guard.legalMoveCheck(walkingDistance)) {
                    guard.move(walkingDistance);

                } else {
                    double turningAngle = Math.random() * 90 - 45; //should this not be Math.random() * 45 - 45, now it has the tendency to go clockwise I think
                    guard.updateDirection(turningAngle);

                }

            }

            previousTime = currentTime;
            System.out.println("guard position: " + guard.getPosition().getX() + " , " + guard.getPosition().getY());
        }
          succeed();

    }
    private boolean isAtDestination(Guard guard){
        return destX == guard.getPosition().getX() && destY == guard.getPosition().getY();
    }

}
package Agent;
import World.WorldMap;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static World.GameScene.SCALING_FACTOR;
import static World.WorldMap.*;

/**
 * This is the superclass of Intruder and Guard, which contains methods for actions
 * @author Benjamin, Kailhan Hokstam
 */

public class Agent implements Runnable{
    public static final double WALK_SPEED_SLOW = 0; //speed <0.5m/s
    public static final double WALK_SPEED_MEDIUM = 0.5; //speed >0.5 & <1 m/s
    public static final double WALK_SPEED_MEDIUMFAST = 1; //speed >1 & 2 m/s
    public static final double WALK_SPEED_FAST = 2; //speed >2m/s
    public static final double SOUNDRANGE_CLOSE = 1; //distance 1m
    public static final double SOUNDRANGE_MEDIUM = 3; //distance 3m
    public static final double SOUNDRANGE_MEDIUMFAR = 5; //distance 5m
    public static final double SOUNDRANGE_FAR = 10;  //distance 10m
    public static final double SOUND_NOISE_STDEV = 10;  //stndard dev of normal distributed noise
    private int counter = 0; //remove :)

    protected volatile Point2D.Double position;
    protected double direction;
    protected int[][] knownTerrain = new int[worldMap.getSize()][worldMap.getSize()];
    protected List<AudioLog> audioLogs = new ArrayList<AudioLog>();
    protected volatile double xGoal;
    protected volatile double yGoal;
    protected double convertedDistance;
    protected double currentSpeed;

    public static WorldMap worldMap;
    protected double currentTime;
    protected double delta;
    protected boolean exitThread;
    protected double previousTime;
    protected Point2D.Double previousPosition;
    protected volatile Point2D.Double goalPosition;

        /**
         * Constructor for Agent
         * @param position is a point containing the coordinates of an Agent
         * @param direction is the angle which the agent is facing, this spans from -180 to 180 degrees
         */

    public Agent(Point2D.Double position, double direction) {
        System.out.println("agent constructor called");
            this.position = position;
            this.direction = direction;
            this.goalPosition = position;
            for (int i = 0;i < knownTerrain.length;i++)
            {
                for (int j = 0;j < knownTerrain[0].length;j++)
                {
                    knownTerrain[i][j] = 0;
                }
            }
        }

    public void run() {
        counter++;
        if(counter == 1)
        {
            updateKnownTerrain(10, 45);
            for (int i = 0;i < this.knownTerrain.length;i++)
            {
                for (int j = 0;j < this.knownTerrain[0].length;j++)
                {
                    //System.out.print(this.knownTerrain[i][j]+" ");
                }
                System.out.println();
            }
        }
        previousTime = System.nanoTime();
        previousPosition = new Point2D.Double(position.getX(), position.getY());
        /**
         * DONT REMOVE THIS GOALPOSITION THING IT IS NECESSARY FOR SOME REASON
         */
        goalPosition = new Point2D.Double(200, 200);
        while(!exitThread) {
            currentTime = System.nanoTime();
            delta = currentTime - previousTime;
            delta /= 1e9; //makes it in seconds
            previousTime = currentTime;
            currentSpeed = ((position.distance(previousPosition)/SCALING_FACTOR)/delta);
            //System.out.println("currentSpeed:" + currentSpeed);
            previousPosition.setLocation(position.getX(), position.getY());
            checkForAgentSound();
            double walkingDistance = (1.4 * SCALING_FACTOR) * (delta);
            if(legalMoveCheck(walkingDistance)) {
                move(walkingDistance);
            } else {
                double turningAngle = Math.random()*90-45;
                turn(turningAngle);
            }

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

    /**
     * to update the direction which an agent is facing
     * @param angle is the angle which the agent will turn, positive for turning to the right, negative for turning to the left
     */

    public void turn(double angle)
    {
        direction = direction+angle;
        if(direction > 180) {
            direction = (direction-180)-180;
        } else if(direction < 180) {
            direction = (direction+180)+180;
        }
        //your code got stuck in a loop thing the above should be fine?
//        while (direction > 180 || direction < -180)
//        {
//            if (direction > 180)
//            {
//                direction = (direction-180)-180;
//            } else if (direction < 180)
//            {
//                direction = (direction+180)+180;
//            }
//            System.out.println("direction: " + direction);
//        }
    }

    /**
     * find out where an Agent will end up after a potential move
     * @param distance is the distance to be moved by the agent
     *                 depending on the time-step, speeds will need to be divided before being used as parameters
     * @param facingDirection is the angle which the Agent is turned (i.e. it's direction) for the potential move
     * @return a point at which the Agent would end up if this move were made
     */

    public Point2D.Double getMove(double distance, double facingDirection) {
        if (facingDirection > 0 && facingDirection <= 90)
        {
            double angle = facingDirection;
            double newXCoordinate = position.getX()+(distance*Math.sin(angle));
            double newYCoordinate = position.getY()-(distance*Math.cos(angle));
            Point2D.Double newLocation = new Point2D.Double(newXCoordinate, newYCoordinate);
            return newLocation;
        }
        else if (facingDirection > 90 && facingDirection <= 180)
        {
            double angle = 180-facingDirection;
            double newXCoordinate = position.getX()+distance*Math.sin(angle);
            double newYCoordinate = position.getY()+distance*Math.cos(angle);
            Point2D.Double newLocation = new Point2D.Double(newXCoordinate, newYCoordinate);
            return newLocation;
        }
        else if (facingDirection <= 0 && facingDirection > -90)
        {
            double angle = -facingDirection;
            double newXCoordinate = position.getX()-distance*Math.sin(angle);
            double newYCoordinate = position.getY()-distance*Math.cos(angle);
            Point2D.Double newLocation = new Point2D.Double(newXCoordinate, newYCoordinate);
            return newLocation;
        }
        //else if (facingDirection <= -90 || facingDirection > -180)
        else
        {
            double angle = 180+facingDirection;
            double newXCoordinate = position.getX()-distance*Math.sin(angle);
            double newYCoordinate = position.getY()+distance*Math.cos(angle);
            Point2D.Double newLocation = new Point2D.Double(newXCoordinate, newYCoordinate);
            return newLocation;
        }
    }

    /**
     * moves the agent
     * @param distance is the distance to be moved by the agent
     *                 depending on the time-step, speeds will need to be divided before being used as parameters
     *                 angle is not required here because it is an instance variable of the Agent
     */

    public void move(double distance)
    {
        this.position.setLocation(getMove(distance, direction));
        //System.out.println("location: " + this.position.toString() );
    }

    /**
     * checks whether there is an obstacle blocking the desired path
     * @param distance is the distance to be moved by the agent
     *                 depending on the time-step, speeds will need to be divided before being used as parameters
     * @return true if there are no obstacles in the way and false if there are obstacles in the way
     */

    public boolean legalMoveCheck(double distance) {
        Point2D.Double positionToCheck = new Point2D.Double(getMove(distance, direction).getX(), getMove(distance, direction).getY());
        int tileStatus;
        try {
            tileStatus = worldMap.coordinatesToCell(positionToCheck);
        }
        catch(Exception e) {
            System.out.println("Location accessed in array is out of bounds");
            return false;
        }
        if (tileStatus == STRUCTURE || tileStatus == SENTRY || tileStatus == WALL) {
            System.out.println("detected wall, sentry or structure in legal move check");
            return false;
        } else {
            return true;
        }
    }

    /**
     * updates the internal map of an Agent based on their field of vision based on sounds of sightings of other agents or in the case of intruders, sighting of new terrain
     * @param radius is the distance an Agent can see in front of them
     * @param angle is the width of view of an Agent
     */
    public void updateKnownTerrain(double radius, double angle)
    {
        //setting search bounds
        Point corner1 = new Point((int) position.getX(), (int) position.getY());
        Point2D.Double straight = getMove(radius, direction);
        Point corner2 = new Point((int) straight.getX(), (int) straight.getY());
        Point2D.Double left = getMove(radius, direction-(angle/2));
        Point corner3 = new Point((int) left.getX(), (int) left.getY());
        Point2D.Double right = getMove(radius, direction+(angle/2));
        Point corner4 = new Point((int) right.getX(), (int) right.getY());
        int XMax = Math.max((int) Math.max(corner1.getX(), corner2.getX()), (int) Math.max(corner3.getX(), corner4.getX()));
        int XMin = Math.min((int) Math.min(corner1.getX(), corner2.getX()), (int) Math.min(corner3.getX(), corner4.getX()));
        int YMax = Math.max((int) Math.max(corner1.getY(), corner2.getY()), (int) Math.max(corner3.getY(), corner4.getY()));
        int YMin = Math.min((int) Math.min(corner1.getY(), corner2.getY()), (int) Math.min(corner3.getY(), corner4.getY()));
        //checking if the points (each corner of each square in the bounds) is within the circle of search  THIS IS UDING COORDS??? NOT THE CORNERS!
        for (int i = YMin;i <= YMax;i++)
        {
            for (int j = XMin;j <= XMax;j++)
            {
                //top left corner
                if (Math.sqrt(((position.getX()-j)*(position.getX()-j))+((position.getY()-i)*(position.getY()))-i) <= radius && Math.atan((Math.abs(position.getX()-j))/(Math.abs(position.getY()-i)))+(Math.abs(direction)) <= angle/2)
                {
                    knownTerrain[i][j] = worldMap.getTileState(i, j);
                }
                //top right corner
                else if (Math.sqrt(((position.getX()-(j+1))*(position.getX()-(j+1)))+((position.getY()-i)*(position.getY()))-i) <= radius && Math.atan((Math.abs(position.getX()-(j+1)))/(Math.abs(position.getY()-i)))+(Math.abs(direction)) <= angle/2)
                {
                    knownTerrain[i][j] = worldMap.getTileState(i, j);
                }
                //bottom right corner
                else if (Math.sqrt(((position.getX()-(j+1))*(position.getX()-(j+1)))+((position.getY()-(i+1))*(position.getY()))-(i+1)) <= radius && Math.atan((Math.abs(position.getX()-(j+1)))/(Math.abs(position.getY()-(i+1))))+(Math.abs(direction)) <= angle/2)
                {
                    knownTerrain[i][j] = worldMap.getTileState(i, j);
                }
                //bottom left corner
                else if (Math.sqrt(((position.getX()-j)*(position.getX()-j))+((position.getY()-(i+1))*(position.getY()))-(i+1)) <= radius && Math.atan((Math.abs(position.getX()-j))/(Math.abs(position.getY()-(i+1))))+(Math.abs(direction)) <= angle/2)
                {
                    knownTerrain[i][j] = worldMap.getTileState(i, j);
                }
            }
        }
        int[][] actualTerrain = worldMap.getWorldGrid();
        //scan through and remove shaded areas
        for (int i = YMin;i <= YMax;i++)
        {
            for (int j = XMin;j <= XMax;j++)
            {
                //the condition below is never satisfied! fix this
                if (actualTerrain[i][j] == 1 || actualTerrain[i][j] == 2 || actualTerrain[i][j] == 3 || actualTerrain[i][j] == 5 || actualTerrain[i][j] == 7)
                {
                    for (int k = YMin;k <= YMax;k++)
                    {
                        for (int l = XMin;l <= XMax;l++)
                        {
                            if (Math.sqrt(((position.getX()-l)*(position.getX()-l))+((position.getY()-k)*(position.getY()))-k) > Math.sqrt(((position.getX()-j)*(position.getX()-j))+((position.getY()-i)*(position.getY()))-i))
                            {
                                if (direction > 0 && direction <= 90 && Math.atan((l-position.getX())/(k-position.getY())) >= Math.atan((j-position.getX())/(i-position.getY())) && Math.atan(((l+1)-position.getX())/((k-1)-position.getY())) <= Math.atan(((j+1)-position.getX())/((i-1)-position.getY())))
                                {
                                    knownTerrain[k][l] = actualTerrain[k][l];
                                }
                                else if (direction > 90 && direction <= 180 && Math.atan((k-position.getY())/((l+1)-position.getX())) >= Math.atan((i-position.getY())/((j+1)-position.getX())) && Math.atan(((k-1)-position.getY())/(l-position.getX())) <= Math.atan(((i+1)-position.getY())/(j-position.getX())))
                                {
                                    knownTerrain[k][l] = actualTerrain[k][l];
                                }
                                else if (direction <= 0 && direction > -90 && Math.atan(((l+1)-position.getX())/(k-position.getY())) <= Math.atan(((j+1)-position.getX())/(i-position.getY())) && Math.atan((l-position.getX())/((k-1)-position.getY())) >= Math.atan((j-position.getX())/((i-1)-position.getY())))
                                {
                                    knownTerrain[k][l] = actualTerrain[k][l];
                                }
                                else if (direction <= -90 && direction > -180 && Math.atan((k-position.getY())/(l-position.getX())) <= Math.atan((i-position.getY())/(j-position.getX())) && Math.atan(((k-1)-position.getY())/((l+1)-position.getX())) >= Math.atan(((i-1)-position.getY())/((j+1)-position.getX())))
                                {
                                    knownTerrain[k][l] = actualTerrain[k][l];
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Checks if we can hear other agents and if so add it personal memory with noise
     */
    public void checkForAgentSound(){
        Point2D tmpPoint = getMove(1000, direction);
        for(Agent agent : worldMap.getAgents()) {
            if(position.distance(agent.getPosition()) != 0) { //to not add hearing "ourselves" to our log tho a path that we have taken might be something that we want store in the end
                boolean soundHeard = false;
                double angleBetweenPoints = angleBetweenTwoPointsWithFixedPoint(tmpPoint.getX(), tmpPoint.getY(), agent.getPosition().getX(), agent.getPosition().getY(), position.getX(), position.getY());
                angleBetweenPoints += new Random().nextGaussian()*SOUND_NOISE_STDEV;
                if(position.distance(agent.getPosition()) < SOUNDRANGE_FAR && agent.currentSpeed > WALK_SPEED_FAST) {
                    soundHeard = true;
                } else if(position.distance(agent.getPosition()) < SOUNDRANGE_MEDIUMFAR && agent.currentSpeed > WALK_SPEED_MEDIUMFAST) {
                    soundHeard = true;
                } else if(position.distance(agent.getPosition()) < SOUNDRANGE_MEDIUM && agent.currentSpeed > WALK_SPEED_MEDIUM) {
                    soundHeard = true;
                } else if(position.distance(agent.getPosition()) < SOUNDRANGE_CLOSE && agent.currentSpeed > WALK_SPEED_SLOW) {
                    soundHeard = true;
                }
                if(soundHeard){
                    audioLogs.add(new AudioLog(System.nanoTime(), angleBetweenPoints, new Point2D.Double(position.getX(), position.getY())));
                    System.out.println("Agent heard sound");
                }
            }

        }
    }

    public static double angleBetweenTwoPointsWithFixedPoint(double point1X, double point1Y,
                                                             double point2X, double point2Y,
                                                             double fixedX, double fixedY) {

        double angle1 = Math.atan2(point1Y - fixedY, point1X - fixedX);
        double angle2 = Math.atan2(point2Y - fixedY, point2X - fixedX);

        return angle1 - angle2;
    }

    public Point2D.Double getPosition() {
        return position;
    }

    public Point2D.Double getGoalPosition() {
        return goalPosition;
    }

    public boolean isThreadStopped() {
        return exitThread;
    }

    public void setThreadStopped(boolean exit) {
        this.exitThread = exit;
    }
    public synchronized void setPosition(Point2D.Double position) {
        this.position = position;
    }

    public double getCurrentSpeed() {
        return currentSpeed;
    }

    public void setCurrentSpeed(double currentSpeed) {
        this.currentSpeed = currentSpeed;
    }

    public List<AudioLog> getAudioLogs() {
        return audioLogs;
    }

    public void setAudioLogs(List<AudioLog> audioLogs) {
        this.audioLogs = audioLogs;
    }

    public double getDirection() {
        return direction;
    }

    public void setDirection(double direction) {
        this.direction = direction;
    }
}


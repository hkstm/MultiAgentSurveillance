package Agent;
import World.GameScene;
import World.WorldMap;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.geometry.Point2D;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Shape;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static Agent.MoveTo.destX;
import static Agent.MoveTo.destY;
import static World.GameScene.*;
import static World.GameScene.ASSUMED_WORLDSIZE;
import static World.StartWorldBuilder.WINDOW_SIZE;
import static World.GameScene.SCALING_FACTOR;

import static World.WorldMap.*;

/**
 * This is the superclass of Intruder and Guard, which contains methods for actions
 * @author Benjamin, Kailhan
 */

public class Agent implements Runnable {
    public static final double WALK_SPEED_SLOW = 0; //speed <0.5m/s
    public static final double WALK_SPEED_MEDIUM = 0.5; //speed >0.5 & <1 m/s
    public static final double WALK_SPEED_MEDIUMFAST = 1; //speed >1 & 2 m/s
    public static final double WALK_SPEED_FAST = 2; //speed >2m/s
    public static final double SOUNDRANGE_CLOSE = 1; //distance 1m
    public static final double SOUNDRANGE_MEDIUM = 3; //distance 3m
    public static final double SOUNDRANGE_MEDIUMFAR = 5; //distance 5m
    public static final double SOUNDRANGE_FAR = 10;  //distance 10m
    public static final double SOUND_NOISE_STDEV =  10;  //stndard dev of normal distributed noise
    public static final int AMOUNT_OF_VISION_TENTACLES = 100;
    public static final int TENTACLE_INCREMENTS = 1000;
    public static final double MAX_TURNING_PER_SECOND = 180;

    protected volatile Point2D position;
    protected double direction;
    protected int[][] knownTerrain = new int[worldMap.getSize()][worldMap.getSize()];
    protected List<AudioLog> audioLogs = new ArrayList<AudioLog>();
    protected volatile double xGoal;
    protected volatile double yGoal;
    protected double convertedDistance;
    protected double currentSpeed;
    protected Color color;

    protected Shape viewingCone;
    protected double viewingAngle;
    protected double[] visualRange;

    public static WorldMap worldMap;
    protected double currentTime;
    protected double delta;
    protected boolean exitThread;
    protected double previousTime;

    protected Point2D previousPosition;
    protected volatile Point2D goalPosition;

    protected boolean firstRun;

    protected boolean turning;
    protected boolean sprinting;
    protected double startingAngle;
    protected double endAngle;
    private boolean goalSet = false;



    /**
     * Constructor for Agent
     * @param position is a point containing the coordinates of an Agent
     * @param direction is the angle which the agent is facing, this spans from -180 to 180 degrees
     */

    public Agent(Point2D position, double direction) {
        System.out.println("agent constructor called");
        this.position = position;
        this.direction = direction;
        this.goalPosition = position;
        this.color = Color.LIGHTSEAGREEN;
        for(int i = 0; i < knownTerrain[0].length; i++) {
            for(int j = 0; j < knownTerrain.length; j++) {
                if(worldMap.worldGrid[i][j] == 4) {
                    goalPosition = new Point2D(j, i);
                    goalSet = true;
                }
            }
        }
        if(goalSet == false) {
            System.out.println("No Target");
        }
        //this.goalPosition = position;
        this.visualRange = new double[2];
        this.firstRun = true;
        for (int i = 0; i < knownTerrain.length; i++) {
            for (int j = 0; j < knownTerrain[0].length;j++) {
                knownTerrain[i][j] = 8;
            }
        }
    }

    /**
     * Default run method, should not really be used (e.g. should be overwritten in subclasses and those should be instantiated)
     */
    public void run() {
        previousTime = System.nanoTime();
        previousPosition = new Point2D(position.getX(), position.getY());
        /**
         * DONT REMOVE THIS GOALPOSITION THING IT IS NECESSARY FOR SOME REASON
         */

        goalPosition = new Point2D(200, 200);
        while(!exitThread) { {
            for (int i = 0; i < knownTerrain.length; i++) {
                for (int j = 0; j < knownTerrain[0].length; j++) {
                    //System.out.print(knownTerrain[row][column] + " knownterrain");
                }
            }
        }
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
     * Default agent logic, should not really be used (e.g. should be overwritten in subclasses and those should be instantiated)
     */
    public void executeAgentLogic() {
        goalPosition = new Point2D(200, 200);
        //DONT PRINT EMPTY STRINGS THANKS
        currentTime = System.nanoTime();
        delta = currentTime - previousTime;
        delta /= 1e9; //makes it in seconds
        previousTime = currentTime;
        currentSpeed = ((position.distance(previousPosition) / SCALING_FACTOR) / delta);
        //System.out.println("currentSpeed:" + currentSpeed);
        previousPosition = new Point2D(position.getX(), position.getY());
        updateKnownTerrain();

        checkForAgentSound();
        double walkingDistance = (1.4 * SCALING_FACTOR) * (delta);
        if (legalMoveCheck(walkingDistance)) {
            move(walkingDistance);
//            System.out.println("moving");
        } else {
            updateDirection(Math.random() * 360);
//            System.out.println("turning");
        }
        updateGoalPosition();
        xGoal = getGoalPosition().getX();
        yGoal = getGoalPosition().getY();
    }

    /**
     * Idk if this is still used but it removing it used to break stuff
     */
    public void updateGoalPosition() {
        //some logic with the worldMap and whatever algorithms we are using
        double x = 200;
        double y = 200;
        goalPosition = new Point2D(x, y);
    }

    /**
     * to update the direction which an agent is facing
     * @param turningAngle is the turningAngle which the agent will turn, positive for turning to the right, negative for turning to the left
     */

    public void turn(double turningAngle) {
        direction = direction+turningAngle;
        //if(direction > 180) {
        //    direction = (direction-180)-180;
        //} else if(direction < 180) {
        //    direction = (direction+180)+180;
        //}
        while (direction > 360 || direction < 0)
        {
            if (direction > 360)
            {
                direction = direction-360;
            } else if (direction < 0)
            {
                direction = direction+360;
            }
            //System.out.println("direction: " + direction);
        }
    }

    /**
     * Method that you should use when trying to update an agents direction
     * Limits the amount you can turn
     * @param directionToGo direction you are trying to face (in degrees 0 - 360, I think)
     */
    public void updateDirection(double directionToGo) {
        if(!sprinting) {
            double maxTurn = MAX_TURNING_PER_SECOND * delta;
            double toTurn = Math.abs(directionToGo - direction);
            double turn = Math.min(maxTurn, toTurn);
            if(directionToGo > direction) {
                direction += turn;
            } else {
                direction -= turn;
            }
        } else {
            System.out.println("logic for turning while sprinting needs to be implemented rip");
        }


    }

    /**
     * find out where an Agent will end up after a potential move
     * @param distance is the distance to be moved by the agent
     *                 depending on the time-step, speeds will need to be divided before being used as parameter
     * @return a point at which the Agent would end up if this move were made
     */

    public Point2D getMove(double distance, double facingDirection) {
        double xEnd = position.getX() + (distance * Math.cos(Math.toRadians(facingDirection)));
        double yEnd = position.getY() + (distance * Math.sin(Math.toRadians(facingDirection)));
        return new Point2D(xEnd, yEnd);
    }

    /**
     * moves the agent
     * @param distance is the distance to be moved by the agent
     *                 depending on the time-step, speeds will need to be divided before being used as parameters
     *                 angle is not required here because it is an instance variable of the Agent
     */
    public void move(double distance) {
        //System.out.println("direction before move: "+direction);
        //double y = position.getY();
        position = getMove(distance, direction);
        //System.out.println(position.getY()-y);
        //System.out.println("location: " + this.position.toString() );
    }

    /**
     * checks whether there is an obstacle blocking the desired path
     * @param distance is the distance to be moved by the agent
     *                 depending on the time-step, speeds will need to be divided before being used as parameters
     * @return true if there are no obstacles in the way and false if there are obstacles in the way
     */

    public boolean legalMoveCheck(double distance) {
        Point2D tmpMove = getMove(distance, direction);
        Point2D positionToCheck = new Point2D(tmpMove.getX(), tmpMove.getY());
        int tileStatus = EMPTY;
        if(((int)positionToCheck.getY())/SCALING_FACTOR < 0 || ((int)positionToCheck.getY())/SCALING_FACTOR > (worldMap.getSize()-1) || ((int)positionToCheck.getX())/SCALING_FACTOR < 0 || ((int)positionToCheck.getX())/SCALING_FACTOR > (worldMap.getSize()-1)) {
            System.out.println(worldMap.getSize());
            System.out.println("Location accessed in array is out of bounds");
            return false;
        } else {
            tileStatus = worldMap.coordinatesToCell(positionToCheck);
        }
        if (tileStatus == STRUCTURE || tileStatus == SENTRY || tileStatus == WALL) {
            //System.out.println("detected wall, sentry or structure in legal move check");
            return false;
        }
        return true;
    }

    /**
     * Checks if a tile is contained in an agents cone and if so adds it to the knownTerrain array
     */
    public void updateKnownTerrain(){
        for(int r = 0; r < worldMap.getSize(); r++) {
            for(int c = 0; c < worldMap.getSize(); c++){
                if(viewingCone.contains(worldMap.convertArrayToWorld(c) + 0.5 * worldMap.convertArrayToWorld(1), //changed from *0.5 to *1
                        worldMap.convertArrayToWorld(r) + 0.5 * worldMap.convertArrayToWorld(1))) { //changed from *0.5 to *1
                    knownTerrain[r][c] = worldMap.getTileState(r, c);
                    //System.out.println("r: "+r+" c: "+c);
                    //System.out.println(worldMap.getTileState(r, c)+" "+knownTerrain[r][c]);
//                    System.out.println("reward reset for r: " + r + " c: " + c);
                }
            }
        }
        //System.out.println();
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
                    audioLogs.add(new AudioLog(System.nanoTime(), angleBetweenPoints, new Point2D(position.getX(), position.getY())));
                    System.out.println("Agent heard sound");
                }
            }

        }
    }

    public boolean sees(int r, int c) {
        return viewingCone.contains((c*(ASSUMED_WORLDSIZE/(double)worldMap.getSize())*SCALING_FACTOR), (r*(ASSUMED_WORLDSIZE/(double)worldMap.getSize())*SCALING_FACTOR));
    }

    /**
     * This method updaes an agents viewing cone, not checked yet it if works when in sentry/min visual range is not zero
     * commented out code might be necessary in that case
     * Needs to be called direct or indirectly (WorldMap.createCones())
     */
    public void createCone() {
        double x = position.getX();
        double y = position.getY();
        double visualRangeMin = visualRange[0] * SCALING_FACTOR; //max visionRange
        double visualRangeMax = visualRange[1] * SCALING_FACTOR; //max visionRange
//        double xRightTop = x + (2 * visualRangeMax * Math.cos(Math.toRadians(direction + viewingAngle/2)));//truncatedtriangle works weird shape bounded by circle so making triangle bigger shouldn't matter
//        double yRightTop = y + (2 * visualRangeMax * Math.sin(Math.toRadians(direction + viewingAngle/2)));//truncatedtriangle works weird shape bounded by circle so making triangle bigger shouldn't matter
//        double xLeftTop = x +  (2 * visualRangeMax * Math.cos(Math.toRadians(direction - viewingAngle/2)));//truncatedtriangle works weird shape bounded by circle so making triangle bigger shouldn't matter
//        double yLeftTop = y +  (2 * visualRangeMax * Math.sin(Math.toRadians(direction - viewingAngle/2)));//truncatedtriangle works weird shape bounded by circle so making triangle bigger shouldn't matter
//        double xRightBot = (visualRangeMin != 0) ? (x + (visualRangeMin * Math.cos(Math.toRadians(direction + viewingAngle/2)))) : x;
//        double yRightBot = (visualRangeMin != 0) ? (y + (visualRangeMin * Math.sin(Math.toRadians(direction + viewingAngle/2)))) : y;
//        double xLeftBot = (visualRangeMin != 0) ? (x + (visualRangeMin * Math.cos(Math.toRadians(direction - viewingAngle/2)))) : x;
//        double yLeftBot = (visualRangeMin != 0) ? (y + (visualRangeMin * Math.sin(Math.toRadians(direction - viewingAngle/2)))) : y;
//        Circle circle = new Circle(x, y, visualRangeMax);
//        double[] points = new double[]{
//                xLeftBot, yLeftBot,
//                xRightBot, yRightBot,
//                xRightTop, yRightTop,
//                xLeftTop, yLeftTop,
//        };
//        Polygon truncatedTriangle = new Polygon(points);
//        Shape cone = Shape.intersect(circle, truncatedTriangle);
////        double[] collisionPoints = new double[(AMOUNT_OF_VISION_TENTACLES + 2) * 2];
//        boolean obstructed = false;
        double[] collisionPoints = new double[((AMOUNT_OF_VISION_TENTACLES) * 2)];
        for(int i = 1; i < AMOUNT_OF_VISION_TENTACLES; i++) {
            tentacleincrementloop:
            for(int j = 1; j < TENTACLE_INCREMENTS; j++) {
                Line tentacle = new Line();
                double xLeftBotLine = x;
                double yLeftBotLine = y;
                if(visualRangeMin != 0) {
                    xLeftBotLine = x + (visualRangeMin * Math.cos(Math.toRadians((direction - viewingAngle/2) + (viewingAngle/AMOUNT_OF_VISION_TENTACLES)*i)));
                    yLeftBotLine = y + (visualRangeMin * Math.sin(Math.toRadians((direction - viewingAngle/2) + (viewingAngle/AMOUNT_OF_VISION_TENTACLES)*i)));
                    tentacle.setStartX(xLeftBotLine);
                    tentacle.setStartY(yLeftBotLine);
                } else {
                    tentacle.setStartX(x);
                    tentacle.setStartY(y);
                }
                double xLeftTopLine = x + (visualRangeMax * (double)j/(double)(TENTACLE_INCREMENTS-1) * Math.cos(Math.toRadians((direction - viewingAngle/2) + (viewingAngle/(AMOUNT_OF_VISION_TENTACLES-1))*i)));
                double yLeftTopLine = y + (visualRangeMax * (double)j/(double)(TENTACLE_INCREMENTS-1) * Math.sin(Math.toRadians((direction - viewingAngle/2) + (viewingAngle/(AMOUNT_OF_VISION_TENTACLES-1))*i)));
                xLeftTopLine = (Math.abs(x - xLeftTopLine) < Math.abs(x - xLeftBotLine)) ? xLeftBotLine : xLeftTopLine;
                yLeftTopLine = (Math.abs(y - yLeftTopLine) < Math.abs(y - yLeftBotLine)) ? yLeftBotLine : yLeftTopLine;
                tentacle.setEndX(xLeftTopLine);
                tentacle.setEndY(yLeftTopLine);
                if(worldMap.isVisionObscuring(worldMap.getWorldGrid()[locationToWorldgrid(yLeftTopLine)][locationToWorldgrid(xLeftTopLine)]) || j == TENTACLE_INCREMENTS-1) {
//                    if(column != TENTACLE_INCREMENTS-1) obstructed = true;
                    collisionPoints[((i-1)*2)+0] = xLeftTopLine; //(i-1 instead of i because outer for loop starts at 1)
                    collisionPoints[((i-1)*2)+1] = yLeftTopLine;
                    knownTerrain[locationToWorldgrid(yLeftTopLine)][locationToWorldgrid(xLeftTopLine)] = worldMap.getWorldGrid()[locationToWorldgrid(yLeftTopLine)][locationToWorldgrid(xLeftTopLine)];
                    break tentacleincrementloop;
                }
            }
        }
        collisionPoints[collisionPoints.length-2] = x + (visualRangeMin * Math.cos(Math.toRadians(direction - viewingAngle/2)));
        collisionPoints[collisionPoints.length-1] = y + (visualRangeMin * Math.sin(Math.toRadians(direction - viewingAngle/2)));
        collisionPoints[collisionPoints.length-4] = x + (visualRangeMin * Math.cos(Math.toRadians(direction + viewingAngle/2)));
        collisionPoints[collisionPoints.length-3] = y + (visualRangeMin * Math.sin(Math.toRadians(direction + viewingAngle/2)));

        Polygon cutout = new Polygon(collisionPoints);
//        if(obstructed) cone = Shape.subtract(cone, cutout);
//        cone = cutout;
//        cone.setSmooth(true);
//        cone.setFill(color);
//        viewingCone = cone;
        viewingCone = cutout;
        viewingCone.setFill(color);
    }

    public Shape getCone() {
        return viewingCone;
    }

    /**
     * Converts world location to array, for example when you want to check if some coordinate you are trying to access
     * is a wall or not
     * @param toBeConverted x or y world coordinate (assuming we use a square world so calculations for x and y are the same)
     * @return column or row that can be looked up in worldArray
     */
    public static int locationToWorldgrid(double toBeConverted) {
        return (int)(toBeConverted * (1/((ASSUMED_WORLDSIZE/worldMap.getWorldGrid().length)*SCALING_FACTOR)));
    }

    /**
     * NOT SURE IF THIS ACTUALLY WORKS, assumpitions w.r.t. 0 degrees being right and the angle being in degrees might not be right
     * @param point1X
     * @param point1Y
     * @param point2X
     * @param point2Y
     * @param fixedX
     * @param fixedY
     * @return
     */
    public static double angleBetweenTwoPointsWithFixedPoint(double point1X, double point1Y,
                                                             double point2X, double point2Y,
                                                             double fixedX, double fixedY) {

        double angle1 = Math.atan2(point1Y - fixedY, point1X - fixedX);
        double angle2 = Math.atan2(point2Y - fixedY, point2X - fixedX);

        return angle1 - angle2;
    }

    public Point2D getPosition() {
        return position;
    }

    public Point2D getGoalPosition() {
        return goalPosition;
    }

    public boolean isThreadStopped() {
        return exitThread;
    }

    public void setThreadStopped(boolean exit) {
        this.exitThread = exit;
    }
    public synchronized void setPosition(Point2D position) {
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

    public double getViewingAngle() {
        return viewingAngle;
    }

    public void setViewingAngle(double viewingAngle) {
        this.viewingAngle = viewingAngle;
    }

    public double[] getVisualRange() {
        return visualRange;
    }

    public void setVisualRange(double[] visualRange) {
        this.visualRange = visualRange;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }


    public void turnToFace(double angle)
    {
        direction = angle;
    }

    public int[][] aStarTerrain(int[][] terrain) {
        List<Point> walls = new ArrayList<Point>();
        for(int i = 0; i < terrain.length; i++)
        {
            for(int j = 0; j < terrain[0].length; j++)
            {
                if(terrain[i][j] == 1 || terrain[i][j] == 5 || terrain[i][j] == 7)
                {
                    Point wall = new Point(i, j);
                    walls.add(wall); //WALL
                }
            }
        }
        //if(walls.size() == 0)
        //{
        //    System.out.println("adding corner");
        //    Point corner = new Point(0, 0);
        //    walls.add(corner);
        //    System.out.println(walls.size());
        //}
        int[][] blocks = new int[walls.size()][2];
        for(int i = 0; i < walls.size(); i++)
        {
            blocks[i][0] = (int)walls.get(i).getY();
            blocks[i][1] = (int)walls.get(i).getX();
        }
        return blocks;
    }

}

package Agent;

import World.WorldMap;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Shape;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static World.GameScene.ASSUMED_WORLDSIZE;
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
    public static final double STRUCTURE_VIS_RANGE = 10;
    public static final double SENTRY_VIS_RANGE = 18;
    public static final int AMOUNT_OF_VISION_TENTACLES = 100;
    public static final int TENTACLE_INCREMENTS = 1000;
    public static final double MAX_TURNING_PER_SECOND = 180; //degrees
    public static final double MAX_NONBLIND_TURNING_PER_SECOND = 45; //degrees
    public static final double MAX_TURNING_WHILE_SPRINTING = 10;
    public static final double TIME_BLINDED = 0.5;
    public static final double MIN_TIME_BEFORE_SHORT_DETECT_IN_DECREASEDVIS = 10;//seconds
    public static final double DECREASE_IN_VISION = 0.5; //used for the 50% reduction in vision when seeing a decreased visibility location
    public static final double DISTANCE_TO_CATCH = 0.5; //meters
    public static final double BASE_SPEED = 1.4; //m/s
    public static final double SPRINT_SPEED = 3.0; //m/s

    protected volatile Point2D position;
    protected double direction;
    protected int[][] knownTerrain;
    protected List<AudioLog> audioLogs = new ArrayList<AudioLog>();
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

    protected long startTimeFastTurn;
    protected double previousDirection;
    protected boolean blind;
    protected boolean hiddenInDecreasedVis;
    protected boolean shortDetectionRange;
    protected long startTimeDecreasedVis;

    protected boolean turnedMaxWhileSprinting;
    protected double turningLeft;

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
        this.knownTerrain = new int[worldMap.getSize()][worldMap.getSize()];
        for(int i = 0; i < knownTerrain[0].length; i++) {
            for(int j = 0; j < knownTerrain.length; j++) {
                if(worldMap.worldGrid[i][j] == TARGET) {
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
                knownTerrain[i][j] = UNEXPLORED;
            }
        }
    }

    /**
     * Default run method
     */
    public void run() {
        previousTime = System.nanoTime();
        previousPosition = new Point2D(position.getX(), position.getY());
        previousDirection = direction;
        firstRun = false;
        while(!exitThread) {
            executeGeneralAgentLogic();
        }
    }

    /**
     * Used instead of the run method if we want to manually control when the agent should update
     */
    public void forceUpdate() {
        if(firstRun) {
            previousTime = System.nanoTime();
            previousPosition = new Point2D(position.getX(), position.getY());
            previousDirection = direction;
            firstRun = false;
        }
        executeGeneralAgentLogic();
    }

    public void executeGeneralAgentLogic() {
        System.out.print("check 1 ");
        currentTime = System.nanoTime();
        delta = currentTime - previousTime;
        delta /= 1e9; //makes it in seconds
        previousTime = currentTime;
        previousDirection = direction;
        //System.out.println("currentSpeed:" + currentSpeed);
        previousPosition = new Point2D(position.getX(), position.getY());
        if(!blind) {
            createCone();
            updateKnownTerrain();
        }
        /**
         * this is the point where the logic of your bot gets called
         */
        //printKnownTerrain();
        executeAgentLogic();
        /**
         *
         */
        checkForAgentSound();
        if((Math.abs(previousDirection - direction) * delta) > (MAX_NONBLIND_TURNING_PER_SECOND * delta)) {
            startTimeFastTurn = System.nanoTime();
            blind = true;
        } else if((System.nanoTime() - startTimeFastTurn)/1e9 > (TIME_BLINDED + delta)) blind = false; //TIME_BLINDED in seconds so have to convert nanoTime()
        if(worldMap.checkTile(locationToWorldgrid(position.getX()), locationToWorldgrid(position.getX()), DECREASED_VIS_RANGE) && !hiddenInDecreasedVis){
            hiddenInDecreasedVis = true;
            startTimeDecreasedVis = System.nanoTime();
        }
        if(!sprinting) {
            turningLeft = MAX_TURNING_WHILE_SPRINTING;
        } else {
            turningLeft -= (Math.abs(previousDirection-direction));
        }
        if(turningLeft <= 0) turnedMaxWhileSprinting = true;
        else turnedMaxWhileSprinting = false;
        if(hiddenInDecreasedVis && ((System.nanoTime() - startTimeDecreasedVis)/1e9) > MIN_TIME_BEFORE_SHORT_DETECT_IN_DECREASEDVIS) shortDetectionRange = true;
        else shortDetectionRange = false;
        if(!hiddenInDecreasedVis) shortDetectionRange = false;
        currentSpeed = ((position.distance(previousPosition) / SCALING_FACTOR) / delta);
        System.out.println("check 3");
    }

    /**
     * Default agent logic
     */
    public void executeAgentLogic() {
        double walkingDistance = (BASE_SPEED * SCALING_FACTOR) * (delta);
        Point2D newPosition = new Point2D((position.getX() + (walkingDistance * Math.cos(Math.toRadians(direction)))), (position.getY() + (walkingDistance * Math.sin(Math.toRadians(direction)))));
        if(!isVisionObscuring(worldMap.getTileState(locationToWorldgrid(newPosition.getY()), locationToWorldgrid(newPosition.getX())))) {
            position = newPosition;
        } else {
            updateDirection(Math.random() * 360);
//            System.out.println("turning");
        }
    }

    /**
     * Method that you should use when trying to update an agents direction
     * Limits the amount you can turn
     * @param directionToGo direction you are trying to face (in degrees 0 - 360, I think)
     */
    public void updateDirection(double directionToGo) {
        if(!turnedMaxWhileSprinting) {
            double maxTurn = 10* delta;
            double toTurn = Math.abs(directionToGo - direction);
            double turn = Math.min(maxTurn, toTurn);
            if(directionToGo > direction) {
                direction += turn;
            } else {
                direction -= turn;
            }
        } else {
            System.out.println("you have turned to match while sprinting");
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
        updateSpecificTerrain(STRUCTURE, STRUCTURE_VIS_RANGE);
        updateSpecificTerrain(SENTRY, SENTRY_VIS_RANGE);
        //System.out.println();
    }

    /**
     * Certain structures can be seen from further so you know where they are but not what the status is
     * @param tileType
     * @param visRange
     */
    private void updateSpecificTerrain(int tileType, double visRange) {
        Shape cone = createCone(visualRange[0], visRange);
        for(int r = 0; r < worldMap.getSize(); r++) {
            for(int c = 0; c < worldMap.getSize(); c++){
                if(isStructure(worldMap.getTileState(r, c))) {
                    if(cone.contains(worldMap.convertArrayToWorld(c) + 0.5 * worldMap.convertArrayToWorld(1), //changed from *0.5 to *1
                            worldMap.convertArrayToWorld(r) + 0.5 * worldMap.convertArrayToWorld(1))) { //changed from *0.5 to *1
                        knownTerrain[r][c] = worldMap.getTileState(r,c);
                    }
                } else if (tileType == SENTRY){
                    if(worldMap.getTileState(r, c) == SENTRY) {
                        if(cone.contains(worldMap.convertArrayToWorld(c) + 0.5 * worldMap.convertArrayToWorld(1), //changed from *0.5 to *1
                                worldMap.convertArrayToWorld(r) + 0.5 * worldMap.convertArrayToWorld(1))) { //changed from *0.5 to *1
                            knownTerrain[r][c] = SENTRY;
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
                double angleBetweenPoints = Math.toDegrees(Math.atan2((agent.getPosition().getY() - position.getY()), (agent.getPosition().getX() - position.getX())));
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
        viewingCone = createCone(visualRange[0], visualRange[1]);
    }

    public Shape createCone(double minVisRange, double maxVisRange) {
        double x = position.getX();
        double y = position.getY();
        double visualRangeMin = minVisRange * SCALING_FACTOR; //max visionRange
        double visualRangeMax = maxVisRange * SCALING_FACTOR; //max visionRange
        double[] collisionPoints = new double[((AMOUNT_OF_VISION_TENTACLES) * 2)];

        for(int i = 1; i < AMOUNT_OF_VISION_TENTACLES; i++) {
            double decreaseInVision = 0;
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
                if(worldMap.checkTile(locationToWorldgrid(yLeftTopLine), locationToWorldgrid(xLeftTopLine), DECREASED_VIS_RANGE)) {
                    decreaseInVision += (1*DECREASE_IN_VISION);
                }
                if(isVisionObscuring(worldMap.getWorldGrid()[locationToWorldgrid(yLeftTopLine)][locationToWorldgrid(xLeftTopLine)]) || j >= TENTACLE_INCREMENTS-(decreaseInVision)-1) {
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
        cutout.setFill(color);
        return cutout;
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

    public void blindCheck(){

    }

    public void printKnownTerrain()
    {
        for(int i = 0; i < knownTerrain.length; i++)
        {
            for(int j = 0; j < knownTerrain.length; j++)
            {
                System.out.print(knownTerrain[i][j]+" ");
            }
            System.out.println();
        }
        System.out.println();
        System.out.println();
    }

}

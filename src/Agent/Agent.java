package Agent;

import World.WorldMap;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Shape;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static World.GameScene.*;
import static World.WorldMap.*;

/**
 * This is the superclass of Intruder and Guard, which contains methods for actions
 * @author Benjamin, Kailhan, Thibaut
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
    public static final double SOUND_NOISE_STDEV =  10;  //stndard dev of normal distributed noise
    public static final double STRUCTURE_VIS_RANGE = 10;
    public static final double SENTRY_VIS_RANGE = 18;
    public static final int AMOUNT_OF_VISION_TENTACLES = 100;
    public static final int TENTACLE_INCREMENTS = 100;
    public static final double MAX_TURNING_PER_SECOND = 180; //degrees
    public static final double MAX_NONBLIND_TURNING_PER_SECOND = 45; //degrees
    public static final double MAX_TURNING_WHILE_SPRINTING = 10;
    public static final double TIME_BLINDED = 0.5;
    public static final double MIN_TIME_BEFORE_SHORT_DETECT_IN_DECREASEDVIS = 10;//seconds
    public static final double DECREASE_IN_VISION = 0.5; //used for the 50% reduction in vision when seeing a decreased visibility location
    public static final double DISTANCE_TO_CATCH = 0.5; //meters
    public static final double BASE_SPEED = 1.4; //m/s
    public static final double SPRINT_SPEED = 3.0; //m/s
    protected Point2D oldTempGoal;
    protected Point2D tempGoal;
    protected Point2D previousTempGoal;
    protected double freezeTime = 0;
    protected double startTime = 0;
    protected boolean frozen = false;
    protected boolean changed = false;
    protected List<Point> tempWalls = new ArrayList<Point>();
    protected boolean blind = false;
    protected boolean rePath = false;

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
    protected boolean pathChanged = false;
    protected boolean reAdded = false;

    protected Point2D previousPosition;
    protected volatile Point2D goalPosition;
    protected Point2D prevGoalPosition;
    protected Point2D goalPositionPath;

    protected boolean firstRun;

    protected boolean turning;
    protected boolean sprinting;

    protected long startTimeFastTurn;
    protected double previousDirection;
    protected boolean hiddenInDecreasedVis;
    protected boolean shortDetectionRange;
    protected long startTimeDecreasedVis;

    protected boolean turnedMaxWhileSprinting;
    protected double turningLeft;

    protected Point[] points = new Point[2];
    protected boolean doorNoise;
    protected boolean windowNoise;

    /**
     * Constructor for Agent
     * @param position is a point containing the coordinates of an Agent
     * @param direction is the angle which the agent is facing, this spans from -180 to 180 degrees
     */

    public Agent(Point2D position, double direction) {
        this.position = position;
        this.direction = direction;
        this.color = Color.LIGHTSEAGREEN;
        this.knownTerrain = new int[worldMap.getSize()][worldMap.getSize()];
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
        currentTime = System.nanoTime();
        delta = currentTime - previousTime;
        delta /= 1e9; //makes it in seconds
        delta *= SIMULATION_SPEEDUP_FACTOR;
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
        } else if((System.nanoTime() - startTimeFastTurn)/1e9 > (TIME_BLINDED/SIMULATION_SPEEDUP_FACTOR + delta)) blind = false; //TIME_BLINDED in seconds so have to convert nanoTime()
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
        if(hiddenInDecreasedVis && ((System.nanoTime() - startTimeDecreasedVis)/1e9) > MIN_TIME_BEFORE_SHORT_DETECT_IN_DECREASEDVIS/SIMULATION_SPEEDUP_FACTOR) shortDetectionRange = true;
        else shortDetectionRange = false;
        if(!hiddenInDecreasedVis) shortDetectionRange = false;
        currentSpeed = ((position.distance(previousPosition) / SCALING_FACTOR) / delta);
    }

    /**
     * Default agent logic
     */
    public void executeAgentLogic() {
        double walkingDistance = (BASE_SPEED * SCALING_FACTOR) * (delta);
        Point2D newPosition = new Point2D((position.getX() + (walkingDistance * Math.cos(Math.toRadians(direction)))), (position.getY() + (walkingDistance * Math.sin(Math.toRadians(direction)))));
        if(isEmpty(worldMap.getTileState(locationToWorldgrid(newPosition.getY()), locationToWorldgrid(newPosition.getX())))) {
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
        updateDirectionGeneral(directionToGo, MAX_TURNING_PER_SECOND);
    }

    public void updateDirectionNoBlind(double directionToGo) {
        updateDirectionGeneral(directionToGo, MAX_NONBLIND_TURNING_PER_SECOND);
    }

    private void updateDirectionGeneral(double directionToGo, double maxNonblindTurningPerSecond) {
        if(!turnedMaxWhileSprinting) {
            double maxTurn = maxNonblindTurningPerSecond * delta;
            double angle = directionToGo - direction;
            angle = (angle > 180) ? angle - 360 : angle;
            angle = (angle < -180) ? angle + 360 : angle;
            if(Math.abs(angle) > maxTurn) {
                if(angle < 0) direction -= maxTurn;
                else direction += maxTurn;
            } else this.direction += angle;
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
                if(this.inVision(worldMap.convertArrayToWorld(c) + 0.5 * worldMap.convertArrayToWorld(1), //changed from *0.5 to *1
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
                    if(cone.contains(worldMap.convertArrayToWorld(c) + 0.5 * worldMap.convertArrayToWorld(1),
                            worldMap.convertArrayToWorld(r) + 0.5 * worldMap.convertArrayToWorld(1))) {
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
        for(Agent agent : worldMap.getAgents()) {
            if(position.distance(agent.getPosition()) != 0) { //to not add hearing "ourselves" to our log tho a path that we have taken might be something that we want store in the end
                boolean soundHeard = false;
                double soundDirection = Math.toDegrees(Math.atan2((agent.getPosition().getY() - position.getY()), (agent.getPosition().getX() - position.getX())));
                soundDirection += new Random().nextGaussian()*SOUND_NOISE_STDEV;
                if(position.distance(agent.getPosition()) < SCALING_FACTOR * SOUNDRANGE_FAR && agent.currentSpeed > WALK_SPEED_FAST) {
                    soundHeard = true;
                } else if(position.distance(agent.getPosition()) < SCALING_FACTOR * SOUNDRANGE_MEDIUMFAR && agent.currentSpeed > WALK_SPEED_MEDIUMFAST) {
                    soundHeard = true;
                } else if(position.distance(agent.getPosition()) < SCALING_FACTOR * SOUNDRANGE_MEDIUM && agent.currentSpeed > WALK_SPEED_MEDIUM) {
                    soundHeard = true;
                } else if(position.distance(agent.getPosition()) < SCALING_FACTOR * SOUNDRANGE_CLOSE && agent.currentSpeed > WALK_SPEED_SLOW) {
                    soundHeard = true;
                } else if(agent.doorNoise && position.distance(agent.getPosition()) < SCALING_FACTOR * SOUNDRANGE_MEDIUMFAR){
                    soundHeard = true;
                } else if(agent.windowNoise && position.distance(agent.getPosition()) < SCALING_FACTOR * SOUNDRANGE_FAR) {
                    soundHeard = true;
                }
                if(soundHeard){
                    audioLogs.add(new AudioLog(System.nanoTime(), soundDirection, new Point2D(position.getX(), position.getY())));

                }
            }

        }
    }

    public boolean sees(int r, int c) {
        return this.inVision((c*(ASSUMED_WORLDSIZE/(double)worldMap.getSize())*SCALING_FACTOR), (r*(ASSUMED_WORLDSIZE/(double)worldMap.getSize())*SCALING_FACTOR));
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
        return createCone(minVisRange, maxVisRange, this.viewingAngle);
    }

    public Shape createCone(double minVisRange, double maxVisRange, double viewingAngle) {
        return createCone(minVisRange, maxVisRange, this.viewingAngle, this.direction);
    }

    public Shape createCone(double minVisRange, double maxVisRange, double viewingAngle, double direction) {
        double x = position.getX();
        double y = position.getY();
        double visualRangeMin = minVisRange * SCALING_FACTOR; //max visionRange
        double visualRangeMax = maxVisRange * SCALING_FACTOR; //max visionRange
        double[] collisionPoints = new double[((AMOUNT_OF_VISION_TENTACLES) * 2)];
        for(int i = 1; i < AMOUNT_OF_VISION_TENTACLES; i++) {
            double decreaseInVision = 0;
            for(int j = 1; j < TENTACLE_INCREMENTS; j++) {
                double xLeftBotLine = x;
                double yLeftBotLine = y;
                if(visualRangeMin != 0) {
                    double angdeg = (direction - viewingAngle / 2) + (viewingAngle / AMOUNT_OF_VISION_TENTACLES) * i;
                    xLeftBotLine = x + (visualRangeMin * Math.cos(Math.toRadians(angdeg)));
                    yLeftBotLine = y + (visualRangeMin * Math.sin(Math.toRadians(angdeg)));
                }
                double angdeg = (direction - viewingAngle / 2) + (viewingAngle / (AMOUNT_OF_VISION_TENTACLES - 1)) * i;
                double xLeftTopLine = x + (visualRangeMax * (double)j/(double)(TENTACLE_INCREMENTS-1) * Math.cos(Math.toRadians(angdeg)));
                double yLeftTopLine = y + (visualRangeMax * (double)j/(double)(TENTACLE_INCREMENTS-1) * Math.sin(Math.toRadians(angdeg)));
                if(visualRangeMin != 0) {
                    xLeftTopLine = (Math.abs(x - xLeftTopLine) < Math.abs(x - xLeftBotLine)) ? xLeftBotLine : xLeftTopLine;
                    yLeftTopLine = (Math.abs(y - yLeftTopLine) < Math.abs(y - yLeftBotLine)) ? yLeftBotLine : yLeftTopLine;
                }
                if(worldMap.checkTile(locationToWorldgrid(yLeftTopLine), locationToWorldgrid(xLeftTopLine), DECREASED_VIS_RANGE)) {
                    decreaseInVision += (1*DECREASE_IN_VISION);
                }
                if(isVisionObscuring(worldMap.getWorldGrid()[locationToWorldgrid(yLeftTopLine)][locationToWorldgrid(xLeftTopLine)]) || j >= TENTACLE_INCREMENTS-(decreaseInVision)-1) {
                    collisionPoints[((i-1)*2)+0] = xLeftTopLine; //(i-1 instead of i because outer for loop starts at 1)
                    collisionPoints[((i-1)*2)+1] = yLeftTopLine;
                    knownTerrain[locationToWorldgrid(yLeftTopLine)][locationToWorldgrid(xLeftTopLine)] = worldMap.getTileState(locationToWorldgrid(yLeftTopLine), locationToWorldgrid(xLeftTopLine));
                    break;
                }
            }
        }
        collisionPoints[collisionPoints.length-1] = y + (visualRangeMin * Math.sin(Math.toRadians(direction - viewingAngle/2)));
        collisionPoints[collisionPoints.length-2] = x + (visualRangeMin * Math.cos(Math.toRadians(direction - viewingAngle/2)));
        collisionPoints[collisionPoints.length-3] = y + (visualRangeMin * Math.sin(Math.toRadians(direction + viewingAngle/2)));
        collisionPoints[collisionPoints.length-4] = x + (visualRangeMin * Math.cos(Math.toRadians(direction + viewingAngle/2)));


        Polygon cutout = new Polygon(collisionPoints);
        cutout.setFill(color);
        return cutout;
//        return new Polygon(collisionPoints);
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
        if(toBeConverted < 0) return 0;
        if(toBeConverted > worldMap.getSize()*SCALING_FACTOR) return worldMap.getSize()-1;
        return (int)(toBeConverted * (1/((ASSUMED_WORLDSIZE/worldMap.getSize())*SCALING_FACTOR)));
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
                if(terrain[i][j] == STRUCTURE || terrain[i][j] == SENTRY || terrain[i][j] == WALL || terrain[i][j] == DOOR || terrain[i][j] == WINDOW)
                {
                    Point wall = new Point(i, j);
                    walls.add(wall);
                }
            }
        }
        int[][] blocks = new int[walls.size()][2];
        for(int i = 0; i < walls.size(); i++)
        {
            blocks[i][0] = (int)walls.get(i).getY();
            blocks[i][1] = (int)walls.get(i).getX();
        }
        return blocks;
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


    public Point2D getGoalPositionPath() {
        return goalPositionPath;
    }

    public Point2D getPrevGoalPosition() {
        return prevGoalPosition;
    }

    public void checkChangedStatus()
    {
        Point2D pointToCheck = new Point2D(tempGoal.getX(), tempGoal.getY());
        Point2D currentPos = new Point2D(position.getX(), position.getY());
        if(changed && checkApproximateEquality(currentPos, pointToCheck))
        {
            changed = false;
        }
    }

    public boolean checkApproximateEquality(Point2D p1, Point2D p2)
    {
        if(p1.getX() <= p2.getX()+1 && p1.getX() >= p2.getX()-1 && p1.getY() <= p2.getY()+1 && p1.getY() >= p2.getY()-1)
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    public boolean isObstruction(int y, int x)
    {
        if(knownTerrain[y][x] == 1 || knownTerrain[y][x] == 5 || knownTerrain[y][x] == 7)
        {
            return true;
        }
        return false;
    }

    public void cornerCorrection() {
        if(oldTempGoal.getX()+10 == tempGoal.getX() && oldTempGoal.getY()-10 == tempGoal.getY())
        {
            if(isObstruction((int)(oldTempGoal.getY()/SCALING_FACTOR), (int)((oldTempGoal.getX()+10)/SCALING_FACTOR)))
            {
                tempGoal = new Point2D(oldTempGoal.getX(), oldTempGoal.getY()-10);
                changed = true;
            }
            else if(isObstruction((int)((oldTempGoal.getY()-10)/SCALING_FACTOR), (int)(oldTempGoal.getX()/SCALING_FACTOR)))
            {
                tempGoal =  new Point2D(oldTempGoal.getX()+10, oldTempGoal.getY());
                changed = true;
            }
        }
        else if(oldTempGoal.getX()-10 == tempGoal.getX() && oldTempGoal.getY()+10 == tempGoal.getY())
        {
            if(isObstruction((int)((oldTempGoal.getY()+10)/SCALING_FACTOR), (int)(oldTempGoal.getX()/SCALING_FACTOR)))
            {
                tempGoal = new Point2D(oldTempGoal.getX()-10, oldTempGoal.getY());
                changed = true;
            }
            else if(isObstruction((int)(oldTempGoal.getY()/SCALING_FACTOR), (int)((oldTempGoal.getX()-10)/SCALING_FACTOR)))
            {
                tempGoal = new Point2D(oldTempGoal.getX(), oldTempGoal.getY()+10);
                changed = true;
            }
        }
        else if(oldTempGoal.getX()+10 == tempGoal.getX() && oldTempGoal.getY()+10 == tempGoal.getY())
        {
            if(isObstruction((int)((oldTempGoal.getY()+10)/SCALING_FACTOR), (int)(oldTempGoal.getX()/SCALING_FACTOR)))
            {
                tempGoal = new Point2D(oldTempGoal.getX()+10, oldTempGoal.getY());
                changed = true;
            }
            else if(isObstruction((int)(oldTempGoal.getY()/SCALING_FACTOR), (int)((oldTempGoal.getX()+10)/SCALING_FACTOR)))
            {
                tempGoal = new Point2D(oldTempGoal.getX(), oldTempGoal.getY()+10);
                changed = true;
            }
        }
        else if(oldTempGoal.getX()-10 == tempGoal.getX() && oldTempGoal.getY()-10 == tempGoal.getY())
        {
            if (isObstruction((int)(oldTempGoal.getY()/SCALING_FACTOR), (int)((oldTempGoal.getX()-10)/SCALING_FACTOR)))
            {
                tempGoal = new Point2D(oldTempGoal.getX(), oldTempGoal.getY()-10);
                changed = true;
            }
            else if (isObstruction((int)((oldTempGoal.getY()-10)/SCALING_FACTOR), (int)(oldTempGoal.getX()/SCALING_FACTOR)))
            {
                tempGoal = new Point2D(oldTempGoal.getX()-10, oldTempGoal.getY());
                changed = true;
            }
        }
    }

    public void wallPhaseDetection()
    {
        pathChanged = false;
        if(oldTempGoal.getX()+10 == tempGoal.getX() && oldTempGoal.getY()-10 == tempGoal.getY() && isObstruction((int) (oldTempGoal.getY() / SCALING_FACTOR), (int) ((oldTempGoal.getX() + 10) / SCALING_FACTOR)) && isObstruction((int) ((oldTempGoal.getY() - 10) / SCALING_FACTOR), (int) (oldTempGoal.getX() / SCALING_FACTOR)))
        {
            Point tempWall = new Point((int) (tempGoal.getX() / SCALING_FACTOR), (int) (tempGoal.getY() / SCALING_FACTOR));
            tempWalls.add(tempWall);
            knownTerrain[(int) tempWall.getY()][(int) tempWall.getX()] = 7;
            pathChanged = true;
            updatePath();
        }
        if(oldTempGoal.getX()-10 == tempGoal.getX() && oldTempGoal.getY()+10 == tempGoal.getY() && isObstruction((int)((oldTempGoal.getY()+10)/SCALING_FACTOR), (int)(oldTempGoal.getX()/SCALING_FACTOR)) && isObstruction((int)(oldTempGoal.getY()/SCALING_FACTOR), (int)((oldTempGoal.getX()-10)/SCALING_FACTOR)))
        {
            Point tempWall = new Point((int) (tempGoal.getX() / SCALING_FACTOR), (int) (tempGoal.getY() / SCALING_FACTOR));
            tempWalls.add(tempWall);
            knownTerrain[(int) tempWall.getY()][(int) tempWall.getX()] = 7;
            pathChanged = true;
            updatePath();
        }
        if(oldTempGoal.getX()+10 == tempGoal.getX() && oldTempGoal.getY()+10 == tempGoal.getY() && isObstruction((int)((oldTempGoal.getY()+10)/SCALING_FACTOR), (int)(oldTempGoal.getX()/SCALING_FACTOR)) && isObstruction((int)(oldTempGoal.getY()/SCALING_FACTOR), (int)((oldTempGoal.getX()+10)/SCALING_FACTOR)))
        {
            Point tempWall = new Point((int)(tempGoal.getX() / SCALING_FACTOR), (int)(tempGoal.getY() / SCALING_FACTOR));
            tempWalls.add(tempWall);
            knownTerrain[(int)tempWall.getY()][(int)tempWall.getX()] = 7;
            pathChanged = true;
            updatePath();
        }
        if(oldTempGoal.getX()-10 == tempGoal.getX() && oldTempGoal.getY()-10 == tempGoal.getY() && isObstruction((int)(oldTempGoal.getY()/SCALING_FACTOR), (int)((oldTempGoal.getX()-10)/SCALING_FACTOR)) && isObstruction((int)((oldTempGoal.getY()-10)/SCALING_FACTOR), (int)(oldTempGoal.getX()/SCALING_FACTOR)))
        {
            Point tempWall = new Point((int) (tempGoal.getX() / SCALING_FACTOR), (int) (tempGoal.getY() / SCALING_FACTOR));
            tempWalls.add(tempWall);
            knownTerrain[(int) tempWall.getY()][(int) tempWall.getX()] = 7;
            pathChanged = true;
            updatePath();
        }
    }

    public void updatePath()
    {
        int[][] blocks = aStarTerrain(knownTerrain);
        Astar pathFinder = new Astar(knownTerrain[0].length, knownTerrain.length, (int)(position.getX()/SCALING_FACTOR), (int)(position.getY()/SCALING_FACTOR), (int)goalPosition.getX(), (int)goalPosition.getY(), blocks, this, false);
        List<Node> path = pathFinder.findPath();
        if(!changed)
        {
            tempGoal = new Point2D((path.get(path.size()-1).row*SCALING_FACTOR)+(SCALING_FACTOR/2), (path.get(path.size()-1).column*SCALING_FACTOR)+(SCALING_FACTOR/2));

            if (path.size() > 1) {
                previousTempGoal = new Point2D((path.get(path.size() - 2).row * SCALING_FACTOR) + (SCALING_FACTOR / 2), (path.get(path.size() - 2).column * SCALING_FACTOR) + (SCALING_FACTOR / 2));
            }
            else{
                previousTempGoal = tempGoal;
            }
        }
        if(pathChanged)
        {
            wallPhaseDetection();
        }
        cornerCorrection();
    }

    public void updateWalls()
    {
        if(tempWalls.size() > 0)
        {
            for(int i = 0 ; i < tempWalls.size() ; i++)
            {
                reAdded = false;
                knownTerrain[tempWalls.get(i).y][tempWalls.get(i).x] = worldMap.getWorldGrid()[tempWalls.get(i).y][tempWalls.get(i).y];
                int[][] phaseDetectionBlocks = aStarTerrain(knownTerrain);
                Astar phaseDetectionPathFinder = new Astar(knownTerrain[0].length, knownTerrain.length, (int)(position.getX()/SCALING_FACTOR), (int)(position.getY()/SCALING_FACTOR), (int)goalPosition.getX(), (int)goalPosition.getY(), phaseDetectionBlocks, this, false);
                List<Node> phaseDetectionPath = phaseDetectionPathFinder.findPath();
                for(int j = 0 ; j < phaseDetectionPath.size() ; j++)
                {
                    if(phaseDetectionPath.get(j).row == tempWalls.get(i).x && phaseDetectionPath.get(j).column == tempWalls.get(i).y)
                    {
                        knownTerrain[tempWalls.get(i).y][tempWalls.get(i).x] = 7;
                        reAdded = true;
                        break;
                    }
                }
                if(!reAdded)
                {
                    tempWalls.remove(i);
                }
            }
        }
    }

    public int[][] getKnownTerrain()
    {
        return knownTerrain;
    }

    public int[][] getWorldGrid()
    {
        return worldMap.worldGrid;
    }

    public void performTurn(double turnAngle)
    {
        if(tempGoal.getX() >= position.getX() && tempGoal.getY() <= position.getY())
        {
            turnToFace(turnAngle-90);
        }
        else if(tempGoal.getX() >= position.getX() && tempGoal.getY() > position.getY())
        {
            turnToFace(90-turnAngle);
        }
        else if(tempGoal.getX() < position.getX() && tempGoal.getY() > position.getY())
        {
            turnToFace(90+turnAngle);
        }
        else if(tempGoal.getX() < position.getX() && tempGoal.getY() <= position.getY())
        {
            turnToFace(270-turnAngle);
        }
    }

    public boolean inVision(Point2D location) {
        if(blind) return false;
        else return viewingCone.contains(location);
    }

    public boolean inVision(double x, double y) {
        if(blind) return false;
        else return viewingCone.contains(x, y);
    }

    public void clearAudioLog()
    {
        audioLogs.clear();
    }
}

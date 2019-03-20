package Agent;
import World.WorldMap;
import java.awt.Point;
import java.awt.geom.Point2D;

/**
 * This is the superclass of Intruder and Guard, which contains methods for actions
 * @author Benjamin
 */

public class Agent implements Runnable{
    protected volatile Point2D.Double position;
    protected double direction;
    protected int[][] knownTerrain;

    //protected volatile double xCurrent;
    //protected volatile double yCurrent;
    protected volatile double xGoal;
    protected volatile double yGoal;

    public static WorldMap worldMap;
        double currentTime;
        double delta;
//        ;
//        ;
        protected boolean exitThread;
        protected double previousTime;
        protected volatile Point2D.Double goalPosition;

        /**
         * Constructor for Agent
         * @param position is a point containing the coordinates of an Agent
         * @param direction is the angle which the agent is facing, this spans from -180 to 180 degrees
         */

    public Agent(Point2D.Double position, double direction)
        {
            this.position = position;
            this.direction = direction;
            this.goalPosition = position;
        }

        public void run() {
        double deltaScaling = 0.0001; //arbitrary as fuck dependent on how fast we are allowed to walk and how big the actual world is
        previousTime = System.nanoTime();
        goalPosition = new Point2D.Double(25, 25);
        while(!exitThread) {
            currentTime = System.nanoTime();
            //ISSUES - delta should be in seconds? check this
            delta = currentTime - previousTime;
            //delta /= 1e6; //makes it ms
            double walkingDistance = (1.4/delta);
            if (legalMoveCheck(walkingDistance))
            {
                move(walkingDistance);
            }
            else
            {
                double turningAngle = Math.random()*90-45;
                turn(turningAngle);
            }
            previousTime = currentTime;
            //xCurrent = getPosition().getX();
            //yCurrent = getPosition().getY();
            updateGoalPosition();
            xGoal = getGoalPosition().getX();
            yGoal = getGoalPosition().getY();
//            System.out.println(
//                    "xCurrent: " + xCurrent + " yCurrent: " + yCurrent);
//            System.out.println(
//                    "xGoal: " + xGoal + " yGoal: " + yGoal);
//            System.out.println("delta: " + (delta) + "ms");
            //position.setLocation((xCurrent + ((xGoal - xCurrent) * (delta * deltaScaling))), (yCurrent + ((yGoal - yCurrent) * (delta * deltaScaling))));
//            System.out.println("xCurrent" + xCurrent);
//            System.out.println("yCurrent" + yCurrent);
        }
    }

    public void updateGoalPosition() {
        //some logic with the worldMap and whatever algorithms we are using
        double x = 100;
        double y = 500;
        goalPosition.setLocation(x, y);
    }

    /**
     * to update the direction which an agent is facing
     * @param angle is the angle which the agent will turn, positive for turning to the right, negative for turning to the left
     */

    public void turn(double angle)
    {
        direction = direction+angle;
        while (direction > 180 || direction < -180)
        {
            if (direction > 180)
            {
                direction = (direction-180)-180;
            }
            if (direction < 180)
            {
                direction = (direction+180)+180;
            }
        }
    }

    /**
     * find out where an Agent will end up after a potential move
     * @param distance is the distance to be moved by the agent
     *                 depending on the time-step, speeds will need to be divided before being used as parameters
     * @param facingDirection is the angle which the Agent is turned (i.e. it's direction) for the potential move
     * @return a point at which the Agent would end up if this move were made
     */

    public Point2D.Double getMove(double distance, double facingDirection)
    {
        double convertedDistance = convert();
        //System.out.println("x: "+position.getX()+"   y: "+position.getY());
        //System.out.println("convertedDistance: "+convertedDistance+" distance :"+distance);
        if (facingDirection > 0 && facingDirection <= 90)
        {
            double angle = facingDirection;
            double newXCoordinate = position.getX()+distance*Math.sin(angle)*convertedDistance;
            double newYCoordinate = position.getY()-distance*Math.cos(angle)*convertedDistance;
            //.println(newXCoordinate+" "+newYCoordinate);
            Point2D.Double newLocation = new Point2D.Double(newXCoordinate, newYCoordinate);
            return newLocation;
        }
        else if (facingDirection > 90 && facingDirection <= 180)
        {
            double angle = 180-facingDirection;
            double newXCoordinate = position.getX()+distance*Math.sin(angle)*convertedDistance;
            double newYCoordinate = position.getY()+distance*Math.cos(angle)*convertedDistance;
            System.out.println(newXCoordinate+" "+newYCoordinate);
            Point2D.Double newLocation = new Point2D.Double(newXCoordinate, newYCoordinate);
            return newLocation;
        }
        else if (facingDirection <= 0 && facingDirection > -90)
        {
            double angle = -facingDirection;
            double newXCoordinate = position.getX()-distance*Math.sin(angle)*convertedDistance;
            double newYCoordinate = position.getY()-distance*Math.cos(angle)*convertedDistance;
            System.out.println(newXCoordinate+" "+newYCoordinate);
            Point2D.Double newLocation = new Point2D.Double(newXCoordinate, newYCoordinate);
            return newLocation;
        }
        //else if (facingDirection <= -90 || facingDirection > -180)
        else
        {
            double angle = 180+facingDirection;
            double newXCoordinate = position.getX()-distance*Math.sin(angle)*convertedDistance;
            double newYCoordinate = position.getY()+distance*Math.cos(angle)*convertedDistance;
            System.out.println(newXCoordinate+" "+newYCoordinate);
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
        //System.out.println(position.getX()+" "+position.getY());
    }

    /**
     * checks whether there is an obstacle blocking the desired path
     * @param distance is the distance to be moved by the agent
     *                 depending on the time-step, speeds will need to be divided before being used as parameters
     * @return true if there are no obstacles in the way and false if there are obstacles in the way
     */

    public boolean legalMoveCheck(double distance) {
        Point2D.Double positionToCheck = new Point2D.Double (getMove(distance, direction).getX()/convert(), getMove(distance, direction).getY()/convert());
        if (coordinatesToCell(positionToCheck) == 1 || coordinatesToCell(positionToCheck) == 5 || coordinatesToCell(positionToCheck) == 7) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * check what type of terrain is at a given point
     * @param location is the point at which the terrain type is desired
     * @return an integer describing the terrain type in the worldGrid corresponding to the input location
     */

    public int coordinatesToCell(Point2D.Double location)
    {
        int xIndex = ((int) location.getX())+(worldMap.getSize()/2);
        int yIndex = ((int) location.getY())+(worldMap.getSize()/2);
        return worldMap.getTileState(xIndex, yIndex);
    }

    /**
     * updates the internal map of an Agent based on their field of vision based on sounds of sightings of other agents or in the case of intruders, sighting of new terrain
     * @param radius is the distance an Agent can see in front of them
     * @param angle is the width of view of an Agent
     */

    public void updateKnownTerrain(double radius, double angle)
    {
        //setting search bounds
        int[][] tempTerrainKnowledge = knownTerrain;
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
                    tempTerrainKnowledge[i][j] = worldMap.getTileState(i, j);
                }
                //top right corner
                else if (Math.sqrt(((position.getX()-(j+1))*(position.getX()-(j+1)))+((position.getY()-i)*(position.getY()))-i) <= radius && Math.atan((Math.abs(position.getX()-(j+1)))/(Math.abs(position.getY()-i)))+(Math.abs(direction)) <= angle/2)
                {
                    tempTerrainKnowledge[i][j] = worldMap.getTileState(i, j);
                }
                //bottom right corner
                else if (Math.sqrt(((position.getX()-(j+1))*(position.getX()-(j+1)))+((position.getY()-(i+1))*(position.getY()))-(i+1)) <= radius && Math.atan((Math.abs(position.getX()-(j+1)))/(Math.abs(position.getY()-(i+1))))+(Math.abs(direction)) <= angle/2)
                {
                    tempTerrainKnowledge[i][j] = worldMap.getTileState(i, j);
                }
                //bottom left corner
                else if (Math.sqrt(((position.getX()-j)*(position.getX()-j))+((position.getY()-(i+1))*(position.getY()))-(i+1)) <= radius && Math.atan((Math.abs(position.getX()-j))/(Math.abs(position.getY()-(i+1))))+(Math.abs(direction)) <= angle/2)
                {
                    tempTerrainKnowledge[i][j] = worldMap.getTileState(i, j);
                }
            }
        }
        //scan through and remove shaded areas
        for (int i = YMin;i <= YMax;i++)
        {
            for (int j = XMin;j <= XMax;j++)
            {
                if (tempTerrainKnowledge[i][j] == 1 || tempTerrainKnowledge[i][j] == 2 || tempTerrainKnowledge[i][j] == 3 || tempTerrainKnowledge[i][j] == 5 || tempTerrainKnowledge[i][j] == 7)
                {
                    for (int k = YMin;k <= YMax;k++)
                    {
                        for (int l = XMin;l <= XMax;l++)
                        {
                            if (Math.sqrt(((position.getX()-l)*(position.getX()-l))+((position.getY()-k)*(position.getY()))-k) > Math.sqrt(((position.getX()-j)*(position.getX()-j))+((position.getY()-i)*(position.getY()))-i))
                            {
                                if (direction > 0 && direction <= 90 && Math.atan((l-position.getX())/(k-position.getY())) >= Math.atan((j-position.getX())/(i-position.getY())) && Math.atan(((l+1)-position.getX())/((k-1)-position.getY())) <= Math.atan(((j+1)-position.getX())/((i-1)-position.getY())))
                                {
                                    tempTerrainKnowledge[k][l] = knownTerrain[k][l];
                                }
                                else if (direction > 90 && direction <= 180 && Math.atan((k-position.getY())/((l+1)-position.getX())) >= Math.atan((i-position.getY())/((j+1)-position.getX())) && Math.atan(((k-1)-position.getY())/(l-position.getX())) <= Math.atan(((i+1)-position.getY())/(j-position.getX())))
                                {
                                    tempTerrainKnowledge[k][l] = knownTerrain[k][l];
                                }
                                else if (direction <= 0 && direction > -90 && Math.atan(((l+1)-position.getX())/(k-position.getY())) <= Math.atan(((j+1)-position.getX())/(i-position.getY())) && Math.atan((l-position.getX())/((k-1)-position.getY())) >= Math.atan((j-position.getX())/((i-1)-position.getY())))
                                {
                                    tempTerrainKnowledge[k][l] = knownTerrain[k][l];
                                }
                                else if (direction <= -90 && direction > -180 && Math.atan((k-position.getY())/(l-position.getX())) <= Math.atan((i-position.getY())/(j-position.getX())) && Math.atan(((k-1)-position.getY())/((l+1)-position.getX())) >= Math.atan(((i-1)-position.getY())/((j+1)-position.getX())))
                                {
                                    tempTerrainKnowledge[k][l] = knownTerrain[k][l];
                                }
                            }
                        }
                    }
                }
            }
        }
        knownTerrain = tempTerrainKnowledge;
    }

    public Point2D.Double getPosition() {
        return position;
    }

    public Point2D.Double getGoalPosition() {
        return goalPosition;
    }

    //
//    public void setPosition(Point position) {
//        this.position = position;
//    }

    public boolean isThreadStopped() {
        return exitThread;
    }

    public void setThreadStopped(boolean exit) {
        this.exitThread = exit;
    }
    public synchronized void setPosition(Point2D.Double position) {
        this.position = position;
    }

    //this method must be cleaned up (its not a good way of accessing worldSizeSelection by creating a temporary object
    public double convert()
    {
        World.SettingsScene temp = new World.SettingsScene();
        //double size = wordSizeSelection
        return temp.getSize()/worldMap.getSize();
    }
}


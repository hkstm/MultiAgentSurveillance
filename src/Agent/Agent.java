package Agent;
import World.WorldMap;
import java.awt.*;

/**
 * This is the superclass of Intruder and Guard, which contains methods for actions
 * @author Benjamin
 */

public class Agent {
    private Point position;
    private double direction;
    private int[][] knownTerrain;
    private World.WorldMap WorldMap;

    /**
     * Constructor for Agent
     * @param position is a point containing the coordinates of an Agent
     * @param direction is the angle which the agent is facing, this spans from -180 to 180 degrees
     */

    public Agent(Point position, double direction)
    {
        this.position = position;
        this.direction = direction;
    }

    /**
     * to update the direction which an agent is facing
     * @param angle is the angle which the agent will turn, positive for turning to the right, negative for turning to the left
     */

    public void turn(double angle)
    {
        this.direction = this.direction+angle;
        while (this.direction > 180 || this.direction < -180)
        {
            if (this.direction > 180)
            {
                this.direction = (this.direction-180)-180;
            }
            if (this.direction < 180)
            {
                this.direction = (this.direction+180)+180;
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

    public Point getMove(double distance, double facingDirection)
    {
        if (facingDirection > 0 && facingDirection <= 90)
        {
            double angle = 90-facingDirection;
            double newXCoordinate = this.position.getX()+distance*Math.cos(angle);
            double newYCoordinate = this.position.getY()+distance*Math.sin(angle);
            Point newLocation = new Point();
            newLocation.setLocation(newXCoordinate, newYCoordinate);
            return newLocation;
        }
        else if (facingDirection > 90 && facingDirection <= 180)
        {
            double angle = 180-facingDirection;
            double newXCoordinate = this.position.getX()+distance*Math.sin(angle);
            double newYCoordinate = this.position.getY()-distance*Math.cos(angle);
            Point newLocation = new Point();
            newLocation.setLocation(newXCoordinate, newYCoordinate);
            return newLocation;
        }
        else if (facingDirection <= 0 && facingDirection > -90)
        {
            double angle = -1*(180-facingDirection);
            double newXCoordinate = this.position.getX()-distance*Math.sin(angle);
            double newYCoordinate = this.position.getY()-distance*Math.cos(angle);
            Point newLocation = new Point();
            newLocation.setLocation(newXCoordinate, newYCoordinate);
            return newLocation;
        }
        //else if (facingDirection <= -90 || facingDirection > -180)
        else
        {
            double angle = -1*(90+facingDirection);
            double newXCoordinate = this.position.getX()-distance*Math.cos(angle);
            double newYCoordinate = this.position.getY()+distance*Math.sin(angle);
            Point newLocation = new Point();
            newLocation.setLocation(newXCoordinate, newYCoordinate);
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
        this.position.setLocation(getMove(distance, this.direction));
    }

    /**
     * checks whether there is an obstacle blocking the desired path
     * @param distance is the distance to be moved by the agent
     *                 depending on the time-step, speeds will need to be divided before being used as parameters
     * @return true if there are no obstacles in the way and false if there are obstacles in the way
     */

    public boolean legalMoveCheck(double distance) {
        Point positionToCheck = getMove(distance, this.direction);
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

    public int coordinatesToCell(Point location)
    {
        int xIndex = (int) location.getX();
        int yIndex = (int) -location.getY();
        return WorldMap.getTileState(xIndex, yIndex);
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
        Point corner1 = new Point((int) this.position.getX(), (int) this.position.getY());
        Point straight = getMove(radius, this.direction);
        Point corner2 = new Point((int) straight.getX(), (int) straight.getY());
        Point left = getMove(radius, this.direction-(angle/2));
        Point corner3 = new Point((int) left.getX(), (int) left.getY());
        Point right = getMove(radius, this.direction+(angle/2));
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
                if (Math.sqrt(((this.position.getX()-j)*(this.position.getX()-j))+((this.position.getY()-i)*(this.position.getY()))-i) <= radius && Math.atan((Math.abs(this.position.getX()-j))/(Math.abs(this.position.getY()-i)))+(Math.abs(this.direction)) <= angle/2)
                {
                    tempTerrainKnowledge[i][j] = this.WorldMap.getTileState(i, j);
                }
                //top right corner
                else if (Math.sqrt(((this.position.getX()-(j+1))*(this.position.getX()-(j+1)))+((this.position.getY()-i)*(this.position.getY()))-i) <= radius && Math.atan((Math.abs(this.position.getX()-(j+1)))/(Math.abs(this.position.getY()-i)))+(Math.abs(this.direction)) <= angle/2)
                {
                    tempTerrainKnowledge[i][j] = this.WorldMap.getTileState(i, j);
                }
                //bottom right corner
                else if (Math.sqrt(((this.position.getX()-(j+1))*(this.position.getX()-(j+1)))+((this.position.getY()-(i+1))*(this.position.getY()))-(i+1)) <= radius && Math.atan((Math.abs(this.position.getX()-(j+1)))/(Math.abs(this.position.getY()-(i+1))))+(Math.abs(this.direction)) <= angle/2)
                {
                    tempTerrainKnowledge[i][j] = this.WorldMap.getTileState(i, j);
                }
                //bottom left corner
                else if (Math.sqrt(((this.position.getX()-j)*(this.position.getX()-j))+((this.position.getY()-(i+1))*(this.position.getY()))-(i+1)) <= radius && Math.atan((Math.abs(this.position.getX()-j))/(Math.abs(this.position.getY()-(i+1))))+(Math.abs(this.direction)) <= angle/2)
                {
                    tempTerrainKnowledge[i][j] = this.WorldMap.getTileState(i, j);
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
                            if (Math.sqrt(((this.position.getX()-l)*(this.position.getX()-l))+((this.position.getY()-k)*(this.position.getY()))-k) > Math.sqrt(((this.position.getX()-j)*(this.position.getX()-j))+((this.position.getY()-i)*(this.position.getY()))-i))
                            {
                                if (direction > 0 && direction <= 90 && Math.atan((l-this.position.getX())/(k-this.position.getY())) >= Math.atan((j-this.position.getX())/(i-this.position.getY())) && Math.atan(((l+1)-this.position.getX())/((k-1)-this.position.getY())) <= Math.atan(((j+1)-this.position.getX())/((i-1)-this.position.getY())))
                                {
                                    tempTerrainKnowledge[k][l] = knownTerrain[k][l];
                                }
                                else if (direction > 90 && direction <= 180 && Math.atan((k-this.position.getY())/((l+1)-this.position.getX())) >= Math.atan((i-this.position.getY())/((j+1)-this.position.getX())) && Math.atan(((k-1)-this.position.getY())/(l-this.position.getX())) <= Math.atan(((i+1)-this.position.getY())/(j-this.position.getX())))
                                {
                                    tempTerrainKnowledge[k][l] = knownTerrain[k][l];
                                }
                                else if (direction <= 0 && direction > -90 && Math.atan(((l+1)-this.position.getX())/(k-this.position.getY())) <= Math.atan(((j+1)-this.position.getX())/(i-this.position.getY())) && Math.atan((l-this.position.getX())/((k-1)-this.position.getY())) >= Math.atan((j-this.position.getX())/((i-1)-this.position.getY())))
                                {
                                    tempTerrainKnowledge[k][l] = knownTerrain[k][l];
                                }
                                else if (direction <= -90 && direction > -180 && Math.atan((k-this.position.getY())/(l-this.position.getX())) <= Math.atan((i-this.position.getY())/(j-this.position.getX())) && Math.atan(((k-1)-this.position.getY())/((l+1)-this.position.getX())) >= Math.atan(((i-1)-this.position.getY())/((j+1)-this.position.getX())))
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
}

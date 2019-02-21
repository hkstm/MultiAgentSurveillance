package Agent;

import World.WorldMap;

import java.awt.*;

public class Agent { //they should already know the layout!
    private Point position;
    private double direction;
    private int[][] knownTerrain;
    private World.WorldMap WorldMap;
    public Agent(Point position, double direction)
    {
        this.position = position;
        this.direction = direction;
    }
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
        else if (facingDirection > 90 && facingDirection <= 180)
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
    public void move(double distance)
    {
        this.position.setLocation(getMove(distance, this.direction));
    }
    public boolean legalMoveCheck(double distance) {
        Point positionToCheck = getMove(distance, this.direction);
        if (coordinatesToCell(positionToCheck) == 1 || coordinatesToCell(positionToCheck) == 5 || coordinatesToCell(positionToCheck) == 7) {
            return false;
        } else {
            return true;
        }
    }
    public int coordinatesToCell(Point location)
    {
        int xIndex = (int) location.getX();
        int yIndex = (int) location.getY();
        return WorldMap.getTileState(xIndex, yIndex);
    }
    public void updateKnownTerrain() //must add that you cannot see through walls!
    {
        //setting search bounds
        Point corner1 = new Point((int) this.position.getX(), (int) this.position.getY());
        Point straight = getMove(6, this.direction);
        Point corner2 = new Point((int) straight.getX(), (int) straight.getY());
        Point left = getMove(6, this.direction-22.5);
        Point corner3 = new Point((int) left.getX(), (int) left.getY());
        Point right = getMove(6, this.direction+22.5);
        Point corner4 = new Point((int) right.getX(), (int) right.getY());
        //creating search bounds based on these points
        int XMax = Math.max((int) Math.max(corner1.getX(), corner2.getX()), (int) Math.max(corner3.getX(), corner4.getX()));
        int XMin = Math.min((int) Math.min(corner1.getX(), corner2.getX()), (int) Math.min(corner3.getX(), corner4.getX()));
        int YMax = Math.max((int) Math.max(corner1.getY(), corner2.getY()), (int) Math.max(corner3.getY(), corner4.getY()));
        int YMin = Math.min((int) Math.min(corner1.getY(), corner2.getY()), (int) Math.min(corner3.getY(), corner4.getY()));
        //checking if the points (each corner of each square in the bounds) is within the circle of search
        for (int i = YMin;i <= YMax;i++)
        {
            for (int j = XMin;j <= XMax;j++)
            {
                //NOTE this can be done more efficiently, if a corner is within range immediately add all surrounding squares to knownTerrain rather than rechecking corners for adjacent squares
                //either this ^ or add a small value to each corner to check if its really IN the square rather than on the boarder
                //to check all corners
                //top left
                if (Math.sqrt(((this.position.getX()-j)*(this.position.getX()-j))+((this.position.getY()-i)*(this.position.getY()))-i) <= 6 && ((Math.atan((Math.abs(this.position.getX()-j))/(Math.abs(this.position.getY()-i))))+(Math.abs(this.direction))) <= 22.5)
                {
                    knownTerrain[i][j] = this.WorldMap.getTileState(i, j);
                }
                else if (Math.sqrt(((this.position.getX()-(j+1))*(this.position.getX()-(j+1)))+((this.position.getY()-i)*(this.position.getY()))-i) <= 6 && ((Math.atan((Math.abs(this.position.getX()-(j+1))/(Math.abs(this.position.getY()-i))))+(Math.abs(this.direction))) <= 22.5))
                {
                    knownTerrain[i][j] = this.WorldMap.getTileState(i, j);
                }
                else if (Math.sqrt(((this.position.getX()-(j+1))*(this.position.getX()-(j+1)))+((this.position.getY()-(i+1))*(this.position.getY()))-(i+1)) <= 6 && ((Math.atan((Math.abs(this.position.getX()-(j+1))/(Math.abs(this.position.getY()-(i+1)))))+(Math.abs(this.direction))) <= 22.5))
                {
                    knownTerrain[i][j] = this.WorldMap.getTileState(i, j);
                }
                else if (Math.sqrt(((this.position.getX()-j)*(this.position.getX()-j))+((this.position.getY()-(i+1))*(this.position.getY()))-(i+1)) <= 6 && ((Math.atan((Math.abs(this.position.getX()-j))/(Math.abs(this.position.getY()-(i+1)))))+(Math.abs(this.direction))) <= 22.5)
                {
                    knownTerrain[i][j] = this.WorldMap.getTileState(i, j);
                }
            }
        }
    }
}

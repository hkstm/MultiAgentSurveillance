package Agent;

import World.WorldMap;

import java.awt.*;

public class Guard { //they should already know the layout!
    private Point position;
    private double direction;
    private World.WorldMap WorldMap;
    public Guard(Point position, double direction)
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
    public Point getMove(double distance)
    {
        if (this.direction > 0 && this.direction <= 90)
        {
            double angle = 90-this.direction;
            double newXCoordinate = this.position.getX()+distance*Math.cos(angle);
            double newYCoordinate = this.position.getY()+distance*Math.sin(angle);
            Point newLocation = new Point();
            newLocation.setLocation(newXCoordinate, newYCoordinate);
            return newLocation;
        }
        else if (this.direction > 90 && this.direction <= 180)
        {
            double angle = 180-this.direction;
            double newXCoordinate = this.position.getX()+distance*Math.sin(angle);
            double newYCoordinate = this.position.getY()-distance*Math.cos(angle);
            Point newLocation = new Point();
            newLocation.setLocation(newXCoordinate, newYCoordinate);
            return newLocation;
        }
        else if (this.direction > 90 && this.direction <= 180)
        {
            double angle = -1*(180-this.direction);
            double newXCoordinate = this.position.getX()-distance*Math.sin(angle);
            double newYCoordinate = this.position.getY()-distance*Math.cos(angle);
            Point newLocation = new Point();
            newLocation.setLocation(newXCoordinate, newYCoordinate);
            return newLocation;
        }
        //else if (this.direction <= -90 || this.direction > -180)
        else
        {
            double angle = -1*(90+this.direction);
            double newXCoordinate = this.position.getX()-distance*Math.cos(angle);
            double newYCoordinate = this.position.getY()+distance*Math.sin(angle);
            Point newLocation = new Point();
            newLocation.setLocation(newXCoordinate, newYCoordinate);
            return newLocation;
        }
    }
    public void move(double distance)
    {
        this.position.setLocation(getMove(distance));
    }

    public boolean legalMoveCheck(double distance)
    {
        Point positionToCheck = getMove(distance);
        if (coordinatesToCell(positionToCheck) == 1 || coordinatesToCell(positionToCheck) == 5 || coordinatesToCell(positionToCheck) == 7)
        {
            return false;
        }
        else
        {
            return true;
        }
    }
    public int coordinatesToCell(Point location)
    {
        int xIndex = (int) location.getX();
        int yIndex = (int) location.getY();
        return WorldMap.getTileState(xIndex, yIndex);
    }
}

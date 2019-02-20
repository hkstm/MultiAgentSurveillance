package Agent;

import java.lang.Math;
import World.WorldMap;
import java.awt.*;

public class Intruder {
    private Point position;
    private double direction;
    private int[][] knownTerrain;
    private World.WorldMap WorldMap;
    private boolean tired; //after sprinting update this
    public Intruder(Point position, double direction)
    {
        this.position = position;
        this.direction = direction;
        this.tired = false;
        for (int i = 1;i < 200;i++)
        {
            for (int j = 1;j<200;j++)
                knownTerrain[i][j] = 8; //8 indicates unexplored terrain
        }
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
        else if (facingDirection > 90 && this.facingDirection <= 180)
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
    public void move(double distance, double facingDirection)
    {
        this.position.setLocation(getMove(distance, this.direction));
    }

    public boolean legalMoveCheck(double distance)
    {
        Point positionToCheck = getMove(distance, this.direction);
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
    public void updateKnownTerrain() //must add that you cannot see through walls!
    {
        //setting search bounds
        Point corner1 = new Point((int) this.position.getX(), (int) this.position.getY());
        Point straight = getMove(7.5, this.direction);
        Point corner2 = new Point((int) straight.getX(), (int) straight.getY());
        Point left = getMove(7.5, this.direction-22.5);
        Point corner3 = new Point((int) left.getX(), (int) left.getY());
        Point right = getMove(7.5, this.direction+22.5);
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
                //to check all corners
                if (this.WorldMap) //
            }
        }
    }
}

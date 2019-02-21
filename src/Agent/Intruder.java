package Agent;

import java.lang.Math;
import World.WorldMap;
import java.awt.*;

public class Intruder extends Agent{
    private Point position;
    private double direction;
    private int[][] knownTerrain;
    private World.WorldMap WorldMap;
    private boolean tired; //after sprinting update this
    public Intruder(Point position, double direction)
    {
        super(position, direction);
        this.tired = false;
        for (int i = 1;i < 200;i++)
        {
            for (int j = 1;j<200;j++)
                knownTerrain[i][j] = 8; //8 indicates unexplored terrain
        }
    }
    //@overwrite
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
                //NOTE this can be done more efficiently, if a corner is within range immediately add all surrounding squares to knownTerrain rather than rechecking corners for adjacent squares
                //either this ^ or add a small value to each corner to check if its really IN the square rather than on the boarder
                //to check all corners
                //top left
                if (Math.sqrt(((this.position.getX()-j)*(this.position.getX()-j))+((this.position.getY()-i)*(this.position.getY()))-i) <= 7.5 && ((Math.atan((Math.abs(this.position.getX()-j))/(Math.abs(this.position.getY()-i))))+(Math.abs(this.direction))) <= 22.5)
                {
                    knownTerrain[i][j] = this.WorldMap.getTileState(i, j);
                }
                else if (Math.sqrt(((this.position.getX()-(j+1))*(this.position.getX()-(j+1)))+((this.position.getY()-i)*(this.position.getY()))-i) <= 7.5 && ((Math.atan((Math.abs(this.position.getX()-(j+1))/(Math.abs(this.position.getY()-i))))+(Math.abs(this.direction))) <= 22.5))
                {
                    knownTerrain[i][j] = this.WorldMap.getTileState(i, j);
                }
                else if (Math.sqrt(((this.position.getX()-(j+1))*(this.position.getX()-(j+1)))+((this.position.getY()-(i+1))*(this.position.getY()))-(i+1)) <= 7.5 && ((Math.atan((Math.abs(this.position.getX()-(j+1))/(Math.abs(this.position.getY()-(i+1)))))+(Math.abs(this.direction))) <= 22.5))
                {
                    knownTerrain[i][j] = this.WorldMap.getTileState(i, j);
                }
                else if (Math.sqrt(((this.position.getX()-j)*(this.position.getX()-j))+((this.position.getY()-(i+1))*(this.position.getY()))-(i+1)) <= 7.5 && ((Math.atan((Math.abs(this.position.getX()-j))/(Math.abs(this.position.getY()-(i+1)))))+(Math.abs(this.direction))) <= 22.5)
                {
                    knownTerrain[i][j] = this.WorldMap.getTileState(i, j);
                }
            }
        }
    }
}

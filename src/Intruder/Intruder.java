package Intruder;
import java.awt.Point;
public class Intruder
{
    private final double MAX_DISTANCE = 0.0014; //this assumes updating every millisecond
    private Point position;
    private double direction;
    public Intruder(Point position, double direction)
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
    public void move(double distance)
    {
        if (distance > MAX_DISTANCE)
        {
            distance = MAX_DISTANCE;
        }
        if (distance < 0)
        {
            distance = 0;
        }
        if (this.direction > 0 && this.direction <= 90)
        {
            double angle = 90-this.direction;
            double newXCoordinate = this.position.getX()+distance*Math.cos(angle);
            double newYCoordinate = this.position.getY()+distance*Math.sin(angle);
            this.position.setLocation(newXCoordinate, newYCoordinate);
        }
        else if (this.direction > 90 && this.direction <= 180)
        {
            double angle = 180-this.direction;
            double newXCoordinate = this.position.getX()+distance*Math.sin(angle);
            double newYCoordinate = this.position.getY()-distance*Math.cos(angle);
            this.position.setLocation(newXCoordinate, newYCoordinate);
        }
        else if (this.direction > 90 && this.direction <= 180)
        {
            double angle = -1*(180-this.direction);
            double newXCoordinate = this.position.getX()-distance*Math.sin(angle);
            double newYCoordinate = this.position.getY()-distance*Math.cos(angle);
            this.position.setLocation(newXCoordinate, newYCoordinate);
        }
        else if (this.direction <= -90 || this.direction > -180)
        {
            double angle = -1*(90+this.direction);
            double newXCoordinate = this.position.getX()-distance*Math.cos(angle);
            double newYCoordinate = this.position.getY()+distance*Math.sin(angle);
            this.position.setLocation(newXCoordinate, newYCoordinate);
        }
    }
}

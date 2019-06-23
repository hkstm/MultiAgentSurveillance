package Agent;

import java.awt.*;
import javafx.geometry.Point2D;

public class AudioLog {
    private long timeHeard;
    private double direction; //in degrees
    private Point2D locationHeard;

    public AudioLog(long timeHeard, double direction, Point2D locationHeard){
        this.timeHeard = timeHeard;
        this.direction = direction;
        this.locationHeard = locationHeard;
    }

    public double getDirection() {
        return direction;
    }

    public long getTimeHeard() {
        return timeHeard;
    }

    public Point2D getLocationHeard() {
        return locationHeard;
    }
}
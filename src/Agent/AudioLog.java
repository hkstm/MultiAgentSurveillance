package Agent;

import java.awt.*;
import java.awt.geom.Point2D;

public class AudioLog {
    private long timeHeard;
    private double direction;
    private Point2D locationHeard;

    public AudioLog(long timeHeard, double direction, Point2D locationHeard){
        this.timeHeard = timeHeard;
        this.direction = direction;
        this.locationHeard = locationHeard;
    }
}

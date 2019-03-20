package Agent;

import javafx.scene.shape.Circle;

import java.awt.*;
import java.awt.geom.Point2D;

public class AgentCircle extends Circle {

    public static final int AGENT_RADIUS = 3;
    public AgentCircle(Point2D.Double position) {
        super(position.getX(), position.getY(), AGENT_RADIUS);
    }

}

package Agent;

import javafx.scene.shape.Circle;

import java.awt.*;
import java.awt.geom.Point2D;

/**
 * Specific Circle class cause OOB yuh, if you dont want the agents to clip in the walls this is where you gotta be
 * @author Kailhan Hokstam
 */

public class AgentCircle extends Circle {

    public static final int AGENT_RADIUS = 3; //determines size of displaced circle/dot in world

    public AgentCircle(Point2D.Double position) {
        super(position.getX(), position.getY(), AGENT_RADIUS);
    }

}

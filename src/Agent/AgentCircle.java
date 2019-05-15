package Agent;

import javafx.scene.shape.Circle;

import java.awt.*;
import javafx.geometry.Point2D;

/**
 * Specific Circle class cause OOB yuh, if you dont want the agents to clip in the walls this is where you gotta be
 * @author Kailhan Hokstam
 */

public class AgentCircle extends Circle {

    public static final int AGENT_RADIUS = 1; //determines size of displaced circle/dot in world

    public AgentCircle(Agent agent) {
        super(agent.getPosition().getX(), agent.getPosition().getY(), AGENT_RADIUS);
        setFill(agent.getColor());
        setSmooth(true);
    }

}

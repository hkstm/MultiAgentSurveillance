package Agent;

import javafx.scene.shape.Circle;

import java.awt.*;

public class AgentCircle extends Circle {

    public static final int AGENT_RADIUS = 10;
    public AgentCircle(Point position) {
        super(position.getX(), position.getY(), AGENT_RADIUS);
    }

}

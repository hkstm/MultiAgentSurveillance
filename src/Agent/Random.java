package Agent;

import javafx.geometry.Point2D;
import javafx.scene.paint.Color;

import static World.GameScene.SCALING_FACTOR;

public class Random extends Agent{

    private boolean tired;
    private double runTime = 5;
    private double walkTime = 10;

    public Random(Point2D position, double direction) {
        super(position, direction);
        this.viewingAngle = 45;
        this.visualRange[0] = 0;
        this.visualRange[1] = 7.5;
        this.color = Color.RED;
        this.tired = false;
    }

    public void randomIntruder(double timeStep){
        double walkingDistance = (BASE_SPEED *SCALING_FACTOR*timeStep);
        double sprintingDistance = (SPRINT_SPEED *SCALING_FACTOR*timeStep);

        //updateDirection()
        if (!tired) {
            if (legalMoveCheck(sprintingDistance)) {
                runTime = runTime - timeStep;
                move(sprintingDistance);
                if(runTime < 0) {
                    tired = true;
                    runTime = 5;
                }
            }
        }
        else if (tired)
        {
            if(legalMoveCheck(walkingDistance))
            {
                walkTime -= timeStep;
                move(walkingDistance);
                if(walkTime < 0){
                    tired = false;
                    walkTime = 10;
                }
            }
        }
        else if (this.inVision(goalPosition) && !tired){
            move(walkingDistance);
        }
        else {
            if (legalMoveCheck(walkingDistance)){
                move(walkingDistance);
            }
        }

    }
}

package Agent;

import World.GameScene;
import World.WorldMap;
import javafx.scene.shape.Shape;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import static Agent.MoveTo.destX;
import static Agent.MoveTo.destY;


public class Chase extends Routine {
    private MoveTo moveTo;
    private List<Shape> cones;
    private List<Agent> agents;
    static Guard guard;
    private List<AudioLog> audioLogs;
    boolean seen = false;
    private Intruder intruder;

    @Override
    public void start() {
        super.start();
        //this.moveTo.start();
    }

    public  Chase(Guard guard, Intruder intruder) {
        super();
        this.guard = guard;
        this.intruder = intruder;
        chasing(guard);
    }
    public void chasing(Guard guard) {
        destX = intruder.getPosition().getX();
        destY = intruder.getPosition().getY();
        guard.gameTree(guard.delta);
        System.out.println("chasing");

    }


    public void reset() {
        start();
    }

    public void act(Guard guard, WorldMap worldMap) {
        if(isWalking()){
            chasing(guard);
        }

    }
        private boolean isAtDestination(Guard guard){
        return destX == guard.getPosition().getX() && destY == guard.getPosition().getY();}

 /*   public void patrol(Guard guard){
        double fromX = 50.0;
        double fromY = 500.0;

        double toX = 400.0;
        double toY = 500.0;

        //starting at coords fromX,fromY
        if(seen) {
            if (guard.position.getX() != fromX && guard.position.getY() != fromY) {
                guard.goalPosition.setLocation(fromX, fromY);
                this.moveTo = new MoveTo(fromX, fromY);

                patrol(guard);
            } else {
                guard.goalPosition.setLocation(toX, toY);
                this.moveTo = new MoveTo(toX, toY);
                patrol(guard);
                //and so on
            }
        }
        else{
            guard.run();
        }

    }*/


}
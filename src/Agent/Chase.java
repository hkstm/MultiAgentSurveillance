package Agent;

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
    private Guard guard1;

    public void chase(Guard guard, Intruder intruder) {

        guard1 = new Guard((new Point2D.Double(guard.getPosition().getX(), guard.getPosition().getY())), 90);
        //Routine routine = new Routine(guard1);
        //Guard patrol = new Guard();
        //Guard explorer = new Guard();
        //Guard stationair = new Guard();

        //if (intruder.getPosition() instanceof this.cones){
        this.cones = guard.worldMap.getAgentsCones();
        this.agents = guard.worldMap.getAgents();
        for (int i = 0; i < cones.size(); i++) {
            if (agents.get(i) instanceof Intruder) {
                if (cones.get(i).contains(intruder.getPosition().getX(), intruder.getPosition().getY())) {
                    this.moveTo = new MoveTo(intruder.position.getX(), intruder.position.getY());
                }


                //guard1.checkForAgentSound(true))

            /*if (guard.type(stationary)) {
                //make noise
                double Iy = intruder.getPosition().getY();
                patrol.MoveTo(intruder.getPosition().getX(), intruder.getPosition().getY());
                explorer.MoveTo(Intruder.getX(), Intruder.getY());

            } else {
               */


            } else {

                // if(guard.type(patrolling)){
                //     patrol(guard);
                //else if(guard.type(exploratory)){
                //Wander.act(guard1.WorldMap);
                patrol(guard1);
                //   }else{
                //aka stationary -> keep looking around?
                //    guard.updateVisualRange();
                //}
            }
        }

    }

    public void reset() {
        start();
    }
    public void act(Guard guard, WorldMap worldMap) {
        if(isWalking()){
            if(!isAtDestination(guard)){
                patrol(guard1);
            }
        }
    } private boolean isAtDestination(Guard guard){
        return destX == guard.getPosition().getX() && destY == guard.getPosition().getY();}

    public void patrol(Guard guard1){
        double fromX = 50.0;
        double fromY = 500.0;

        double toX = 400.0;
        double toY = 500.0;

        //starting at coords fromX,fromY

        if (guard1.position.getX() != fromX && guard1.position.getY() != fromY) {
            guard1.goalPosition.setLocation(fromX, fromY);
            this.moveTo = new MoveTo(fromX, fromY);

            patrol(guard1);
        } else {
            guard1.goalPosition.setLocation(toX, toY);
            this.moveTo = new MoveTo(toX, toY);
            patrol(guard1);
            //and so on
        }

    }

}
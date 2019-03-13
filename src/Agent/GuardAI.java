package Agent;
import Agent.Guard;
import Agent.Agent;
import World.WorldMap;

import java.awt.geom.Point2D;

public class GuardAI {
    int minDegree = 0;
    int maxDegree = 180;
    double distance;

    /**
     *
     * @param guard guard object
     * makes the guard move randomly.
     */
    public void randomMoves(Guard guard){
        guard.direction = randomWithRange(minDegree, maxDegree);
        guard.move(walkableDistance(guard));
    }
    public int randomWithRange(int min, int max)
    {
        int range = (max - min) + 1;
        return (int)(Math.random() * range) + min;
    }
    public double walkableDistance(Guard guard){
        distance = 1;
        while(guard.legalMoveCheck(distance)){
            distance+=1;
        }
        return distance-1;
    }

}


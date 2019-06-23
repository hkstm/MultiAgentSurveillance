package Agent;

import javafx.geometry.Point2D;
import java.util.ArrayList;
import java.util.LinkedList;

/* This class serves as indirect (stigmergic) communication between agents.
 *
 * Agents have a data structure representing the trail left behind from walking about e.g. a queue or linked list
 * The current tile an agent is on should NOT be apart of its queue, but a "candidate" for the queue's head.
 *
 *
 * Must be able to check if the current tile has pheromones on it.
 * Must be able to identify the agent who "dropped" the found pheromones.
 *
 * The most recent agent that traverses [x,y] leaves their pheromone there - not one-to-many.
 *
 * @author Olive, Costi
 */
public class Pheromone
{
    private Agent owner; // derive type and coordinates from this
    private int coordinates; // coords in terms of grid cells
    public static ArrayList<LinkedList> allTrails;
    private Pheromone onPath;


    public Pheromone(Agent a)
    {
        owner = a;
        coordinates = owner.currentCoordinates;
        allTrails.add(owner.trail);
        onPath = null;
    }

    public void createTrail(){
       if ( Agent.getType() != intruder){
           //create queue
           //check()
           // if false then
           //colour the tile
           //add tile to queue
       }
    }
          
    /* Checks if an agents current coordinate intersects with any hormones
     *
     * @params ccoords the current coordinates of an agent to be checked for intersection
     * @return true: pheromones are at ccords
     */
    public static Agent checkForPheromones(int ccoords)
    {
        // Check for intersection
        for (int i = 0; i < allTrails.size(); i++)
            for (int j = 0; j < allTrails.get(i).size(); j++)
                if (ccoords == allTrails.get(i).get(j))
                {
                    return allTrails.get(i).get(j).owner; // maybe replace owner with getOwner() ?
                }

        return null;
    }


    // is this even needed?
    public Agent getOwner()
    {
        return owner;
    }


    public setPCoordinates(int coords)
    {
        this.coordinates = coords;
    }
}
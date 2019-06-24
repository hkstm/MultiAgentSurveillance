package Agent;

import javafx.geometry.Point2D;

import java.util.ArrayList;

import static Agent.MoveTo.destX;
import static Agent.MoveTo.destY;
import static World.GameScene.ASSUMED_WORLDSIZE;
import static World.GameScene.SCALING_FACTOR;

/* This class is the blackboard class for the guard
 * share information about the any intruders found
 *
 * @author Olive, Costi
 */
public class Blackboard
{
    private static Blackboard bir = null; // Singleton shared by all.

    private ArrayList<Guard> guards;
    private static boolean seenIntruder; // When true, all guards enter chase mode.
    private static Intruder intruder;


    // get intruder
    // get destX and destY
    // new path for guards
    // update path


    private Blackboard()
    {
        guards = new ArrayList<Guard>();
        seenIntruder = false;
    }


    // Ensures that only one instance ever exists.
    public static Blackboard getBlackboard()
    {
        if (bir == null)
            bir = new Blackboard();

        return bir;
    }


    public void setSeen(boolean status, Intruder iTwo)
    {
        seenIntruder = status;
        if (seenIntruder)
        {
            intruder = iTwo;
            //setIntruderCoords();



            //for (int i = 0; i < guards.size(); i++)
            for(Guard g : guards)
            {
                double changedestX = g.locationToWorldgrid(intruder.getPosition().getX());
                double changedestY = g.locationToWorldgrid(intruder.getPosition().getY());
                destX += changedestX;
                destY += changedestY;
                new MoveTo(destX,destY);

            }
            // Change behaviour for guards
        }
    }


//    public static void triggerChase()
//    {
//        System.out.println("intruder seen");
//        Routine routine = Routines.sequence(Routines.chase( XXX, Blackboard.getIntruder()));
//        this.setRoutine(routine);
//        this.routine.start();
//    }

    // Getters and Setters. Accessible only by those holding onto the singleton instance.
//    public boolean getSeen()   {   return seenIntruder;    }
//    private static Agent getIntruder() {   return intruder;    }
//
//    public Point2D getCoords()   {   return posOfIntruder;    }
//    public void setCoords(Point2D intruderPos)    {    posOfIntruder = intruderPos;    }

}

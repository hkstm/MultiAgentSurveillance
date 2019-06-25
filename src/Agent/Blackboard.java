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

    private static ArrayList<Guard> guards;
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
    public static Blackboard getBlackboard(Guard guard)
    {
        if (bir == null)
            bir = new Blackboard();

        guards.add(guard);

        return bir;
    }



    public void setSeen(boolean status)
    {
        seenIntruder = status;
    }

    public void setSeen(boolean status, Intruder iTwo)
    {
        seenIntruder = status;
        if (seenIntruder)
        {
            intruder = iTwo;

            for(Guard g : guards)
            {
                /*double changedestX = Guard.locationToWorldgrid(intruder.getPosition().getX());
                double changedestY = Guard.locationToWorldgrid(intruder.getPosition().getY());
                destX += changedestX;
                destY += changedestY;*/

                Routine guard2 = Routines.sequence( Routines.chase(g, intruder));
                g.setRoutine(guard2);
                g.routine.start();

            }
            // Change behaviour for guards
        }
    }


    public void chaseIntruder(Intruder iTwo)
    {
        intruder = iTwo;

        for(Guard g : guards)
        {
            double changedestX = Guard.locationToWorldgrid(intruder.getPosition().getX());
            double changedestY = Guard.locationToWorldgrid(intruder.getPosition().getY());
            destX += changedestX;
            destY += changedestY;

            Routine guard2 = Routines.sequence( Routines.chase(g, intruder));
            g.setRoutine(guard2);
            g.routine.start();

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
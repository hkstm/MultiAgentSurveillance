package Agent;

/* This class is the blackboard class for the guard
 * share information about the any intruders found
 *
 * @author Olive, Costi
 */
public class Blackboard
{
    private static Blackboard bir = null; // Singleton shared by all.

    private static boolean seenIntruder; // When true, all guards enter chase mode.
    private static Point2D posOfIntruder; //---> to get the intruders coords we just need to call Intruder.getPosition();



    private Blackboard()
    {
        seenIntruder = false;
    }


    // Ensures that only one instance ever exists.
    public static Blackboard getBlackboard()
    {
        if (bir == null)
            bir = new Blackboard();

        return bir;
    }


    // Getters and Setters. Accessible only by those holding onto the singleton instance.
    public boolean getSeen()   {   return seenIntruder;    }
    public void setSeen(boolean status)   {   seenIntruder = status;    }

    public Point2D getCoords()   {   return coordinatesOfIntruder;    }
    public void setCoords(Point2D intruderPos)    {    posOfIntruder = intruderPos;    }
}
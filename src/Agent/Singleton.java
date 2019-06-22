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
    private static int[] coordinatesOfIntruder; //---> to get the intruders coords we just need to call Intruder.getPosition();


    private Blackboard()
    {
        seenIntruder = false;
        coordinatesOfIntruder = new int[2];
    }


    // Ensures that only one instance ever exists.
    public static Blackboard getBlackboard()
    {
        if (bir == null)
            bir = new Blackboard();

        return bir;
    }


    // Getters and Setters. Accessible only by those holding onto the singleton instance.
    private static boolean getSeen()   {   return seenIntruder;    }
    private static void setSeen(boolean status)   {   seenIntruder = status;    }

    private static int[] getCoords()   {   return coordinatesOfIntruder;    }
    private static void setCoords(int x, int y)
    {
        coordinatesOfIntruder[0] = x;
        coordinatesOfIntruder[1] = y;
    }
}

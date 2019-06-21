package Agent;

/* This class is the blackboard class for the guard
 * share information about the any intruders found
 *
 * @author Olive
 */
public class Singleton
{
    private static Singleton bir = null; // Singleton shared by all.
    private static boolean seenIntruder; // When true, all guards enter chase mode.
    private static int[] coordinatesOfIntruder;


    private Singleton()
    {
        seenIntruder = false;
        coordinatesOfIntruder = new int[2];
    }


    // Ensures that only one instance ever exists.
    public static Singleton getSingleton()
    {
        if (bir == null)
            bir = new Singleton();

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

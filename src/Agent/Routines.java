package Agent;

import World.WorldMap;

public class Routines {

    public static Routine sequence(Routine... routines) {
        Sequence sequence = new Sequence();
        for (Routine routine : routines) {
            sequence.addRoutine(routine);
        }
        return sequence;
    }

    public static Routine selector(Routine... routines) {
        Selector selector = new Selector();
        for (Routine routine : routines) {
            selector.addRoutine(routine);
        }
        return selector;
    }

    public static Routine moveTo(int x, int y) {
        return new MoveTo(x, y);
    }

    public static Routine repeatInfinite(Routine routine) {
        return new DoAgain(routine);
    }

    public static Routine repeat(Routine routine, int times) {
        return new DoAgain(routine, times);
    }

    public static Routine wander(WorldMap worldMap) {
        return new Wander(worldMap);
    }

  /*  public static Routine chase() {
        return new Chase();
    }*/

}

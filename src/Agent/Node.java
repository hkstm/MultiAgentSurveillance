package Agent;

public class Node {

    public int i,j;
    public Node parent;
    public int heuristic;
    public int fCost;
    public boolean solution;

    public Node(int i, int j){
        this.i = i;
        this.j = j;
    }

    public String toString(){
        return "[" + i + ", "+ j + "]";
    }
}

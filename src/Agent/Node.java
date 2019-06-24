package Agent;

public class Node {

    public int row, column;
    public Node parent;
    public double heuristic;
    public double fCost;
    public boolean solution;

    public Node(int row, int column){
        this.row = row;
        this.column = column;
    }

    public Node()
    {
    }

    public String toString(){
        return "[" + row + ", "+ column + "]";
    }
}

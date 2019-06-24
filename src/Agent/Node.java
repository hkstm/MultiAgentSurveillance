package Agent;

public class Node {

    public int row, column;
    public Node parent;
    public int heuristic;
    public int fCost;
    public boolean solution;

    public Node(int row, int column){
        this.row = row;
        this.column = column;
    }

    public String toString(){
        return "[" + row + ", "+ column + "]";
    }
}

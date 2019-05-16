package Agent;

import java.util.ArrayList;
import java.util.PriorityQueue;

public class Astar {

    public static final int diaCost = 14;
    public static final int vhCost = 10;
    private Node[][] grid;
    private PriorityQueue<Node>openCell;
    private boolean[][]closeCell;
    private int si, sj;
    private int ei, ej;

    public Astar(int width, int height, int si, int sj, int ei, int ej, int[][]blocks){
        this.si = si;
        this.sj = sj;
        this.ei = ei;
        this.ej = ej;
        grid = new Node[width][height];
        closeCell = new boolean[width][height];
        openCell = new PriorityQueue<>((Node n1, Node n2) ->{
           return n1.fCost < n2.fCost ? -1 : n1.fCost > n2.fCost ? 1:0;
        });

       // Node startNode = new Node(si, sj);
        //Node endNode = new Node(ei, ej);

        for (int i = 0; i < width ; i++) {
            for (int j = 0; j < height; j++) {
                grid[i][j] = new Node(i, j);
                grid[i][j].heuristic = Math.abs(i - ei) + Math.abs(j - ej);
                grid[i][j].solution = false;
            }
        }

        grid[si][sj].fCost = 0;

        for (int i = 0; i < blocks.length; i++){
            addBlocks(blocks[i][0], blocks[i][1]);
        }
    }
    public void addBlocks(int i, int j){
        grid[i][j] = null;
    }

    public void updateCost(Node current, Node next, int cost){
        if (next == null || closeCell[next.i][next.j]){
            return;
        }
        int nextFinalCost = next.heuristic + cost;
        boolean inOpen = openCell.contains(next);

        if (!inOpen || nextFinalCost < next.fCost){
            next.fCost = nextFinalCost;
            next.parent = current;

            if (!inOpen){
                openCell.add(next);
            }
        }
    }

    public void updateNeighbour(){
        openCell.add(grid[si][sj]);
        Node current;
        Node next;

        while(true){
            current = openCell.poll();

            if (current == null){
                break;
            }

            closeCell[current.i][current.j] = true;

            if (current.equals(grid[ei][ej])){
                return;
            }

            if (current.i - 1 >= 0){
                next = grid[current.i - 1][current.j];
                updateCost(current, next, current.fCost + vhCost);

                if (current.j - 1 >= 0){
                    next = grid[current.i - 1][current.j - 1];
                    updateCost(current, next, current.fCost + diaCost);
                }

                if (current.j + 1 < grid[0].length) {
                    next = grid[current.i - 1][current.j + 1];
                    updateCost(current, next, current.fCost + diaCost);
                }
            }

            if (current.i + 1 < grid.length){
                next =  grid[current.i + 1][current.j ];
                updateCost(current, next, current.fCost + vhCost);

                if (current.j - 1 >= 0){
                    next = grid[current.i + 1][current.j - 1];
                    updateCost(current, next, current.fCost + diaCost);
                }
                if (current.j + 1 < grid.length){
                    next = grid[current.i + 1][current.j + 1];
                    updateCost(current, next, current.fCost + diaCost);
                }
            }

            if (current.j - 1 >= 0){
                next = grid[current.i][current.j - 1];
                updateCost(current, next, current.fCost + vhCost);
            }
            if (current.j + 1 < grid[0].length){
                next = grid[current.i][current.j + 1];
                updateCost(current, next, current.fCost + vhCost);
            }
        }
    }

    public ArrayList<Node> findPath(){
        updateNeighbour();
        ArrayList<Node> path = new ArrayList<Node>();
        if (closeCell[ei][ej]) {
            System.out.println("path");
            Node current = grid[ei][ej];
            System.out.println(current);
            grid[current.i][current.j].solution = true;

            while (current.parent != null) {
                path.add(current.parent);
                System.out.println("->" + current.parent);
                grid[current.parent.i][current.parent.j].solution = true;
                current = current.parent;

                System.out.println(path);
                //System.out.println("\n");

                /*for (int i = 0; i < grid.length; i++) {
                    for (int j = 0; j < grid[0].length; j++) {
                        if (i == si && j == sj) {
                            System.out.print("SO");
                        } else if (i == ei && j == ej) {
                            System.out.print("ED");
                        } else if (grid[i][j] != null) {
                            System.out.printf("%-3d", 0);
                        } else {
                            System.out.print("BL");
                        }
                    }
                    System.out.println();
                }*/
                //System.out.println();
            }
        }
        else{
            //System.out.println("no path");
            }
        return path;
    }

    public void display(){
        System.out.println("grid");

        for (int i = 0; i < grid.length; i++){
            for (int j = 0; j < grid[0].length; j++){
                if (i == si && j == sj){
                    System.out.print("SO");
                }
                else if (i == ei && j == ej){
                    System.out.print("ED");
                }
                else if (grid[i][j] != null){
                    System.out.printf("%-3d", 0);
                }
                else{
                    System.out.print("BL");
                }
            }
            System.out.println();
        }
        System.out.println();
    }

    public static void main(String[] args){
        int [][] blocks = {{0, 4}, {2, 2}, {3, 1}, {3, 3}, {2, 1}, {2, 3}};
        Astar astar = new Astar(5, 5, 0, 0, 3, 2, blocks);

        //astar.display();
        //astar.updateNeighbour();
        astar.findPath();
    }


}

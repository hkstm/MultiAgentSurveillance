package Agent;

import java.util.ArrayList;
import java.util.PriorityQueue;
import static World.GameScene.ASSUMED_WORLDSIZE;


public class Astar {

    public static final int diaCost = 14;
    public static final int vhCost = 10;
    private Node[][] grid;
    private PriorityQueue<Node>openCell;
    private boolean[][]closeCell;
    private int si, sj;
    private int ei, ej;

    public Astar(int width, int height, int si, int sj, int ei, int ej, int[][] blocks, Agent agent){
        this.si = si;
        this.sj = sj;
        this.ei = ei;
        this.ej = ej;
        grid = new Node[width][height];
        closeCell = new boolean[width][height];
        openCell = new PriorityQueue<>((Node n1, Node n2) -> Double.compare(n1.fCost, n2.fCost));
        for (int i = 0; i < width ; i++) {
            for (int j = 0; j < height; j++) {
                grid[i][j] = new Node(i, j);
                grid[i][j].heuristic = Math.abs(i - ei) + Math.abs(j - ej);
                grid[i][j].solution = false;
                if(agent instanceof Intruder);
                {
                    grid[i][j].heuristic += addWeight(agent.getKnownTerrain(), j, i);
                }
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

    public void updateCost(Node current, Node next, double cost){
        if (next == null || closeCell[next.row][next.column]){
            return;
        }
        double nextFinalCost = next.heuristic + cost;
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

            closeCell[current.row][current.column] = true;

            if (current.equals(grid[ei][ej])){
                return;
            }

            if (current.row - 1 >= 0){
                next = grid[current.row - 1][current.column];
                updateCost(current, next, current.fCost + vhCost);

                if (current.column - 1 >= 0){
                    next = grid[current.row - 1][current.column - 1];
                    updateCost(current, next, current.fCost + diaCost);
                }

                if (current.column + 1 < grid[0].length) {
                    next = grid[current.row - 1][current.column + 1];
                    updateCost(current, next, current.fCost + diaCost);
                }
            }

            if (current.row + 1 < grid.length){
                next =  grid[current.row + 1][current.column];
                updateCost(current, next, current.fCost + vhCost);

                if (current.column - 1 >= 0){
                    next = grid[current.row + 1][current.column - 1];
                    updateCost(current, next, current.fCost + diaCost);
                }
                if (current.column + 1 < grid.length){
                    next = grid[current.row + 1][current.column + 1];
                    updateCost(current, next, current.fCost + diaCost);
                }
            }

            if (current.column - 1 >= 0){
                next = grid[current.row][current.column - 1];
                updateCost(current, next, current.fCost + vhCost);
            }
            if (current.column + 1 < grid[0].length){
                next = grid[current.row][current.column + 1];
                updateCost(current, next, current.fCost + vhCost);
            }
        }
    }

    public ArrayList<Node> findPath(){
        updateNeighbour();
        ArrayList<Node> path = new ArrayList<Node>();
        if (closeCell[ei][ej]) {
            Node current = grid[ei][ej];
            grid[current.row][current.column].solution = true;
            while (current.parent != null) {
                path.add(current);
                grid[current.parent.row][current.parent.column].solution = true;
                current = current.parent;
            }
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

    public double addWeight(int[][] knownTerrain, int row, int column)
    {
        double weightToAdd = 0;
        if(coverCheck(knownTerrain, row+1, column-1))
        {
            //System.out.println("1");
            weightToAdd -= 6;
        }
        if(coverCheck(knownTerrain, row+1, column))
        {
            //System.out.println("2");
            weightToAdd -= 6;
        }
        if(coverCheck(knownTerrain, row, column-1))
        {
            //System.out.println("3");
            weightToAdd -= 6;
        }
        if(coverCheck(knownTerrain, row-1, column-1))
        {
            //System.out.println("4");
            weightToAdd -= 6;
        }
        if(coverCheck(knownTerrain, row-1, column))
        {
            //System.out.println("5");
            weightToAdd -= 6;
        }
        if(coverCheck(knownTerrain, row-1, column+1))
        {
            //System.out.println("6");
            weightToAdd -= 6;
        }
        if(coverCheck(knownTerrain, row, column+1))
        {
            //System.out.println("7");
            weightToAdd -= 6;
        }
        if(coverCheck(knownTerrain, row+1, column+1))
        {
            //System.out.println("8");
            weightToAdd -= 6;
        }

        for(int i = row ; i < row+16 ; i++)
        {
            for(int j = column ; j < column+16 ; j++)
            {
                if(i >= 0 && j >= 0 && i < ASSUMED_WORLDSIZE && j < ASSUMED_WORLDSIZE && knownTerrain[i][j] == 5)
                {
                    weightToAdd += 15;
                }
            }
        }
        for(int i = row ; i < row+16 ; i++)
        {
            for(int j = column ; j > column-16 ; j--)
            {
                if(i >= 0 && j >= 0 && i < ASSUMED_WORLDSIZE && j < ASSUMED_WORLDSIZE && knownTerrain[i][j] == 5)
                {
                    weightToAdd += 15;
                }
            }
        }
        for(int i = row ; i > row-16 ; i--)
        {
            for(int j = column ; j < column+16 ; j++)
            {
                if(i >= 0 && j >= 0 && i < ASSUMED_WORLDSIZE && j < ASSUMED_WORLDSIZE && knownTerrain[i][j] == 5)
                {
                    weightToAdd += 15;
                }
            }
        }
        for(int i = row ; i > row-16 ; i--)
        {
            for(int j = column ; j > column-16 ; j--)
            {
                if(i >= 0 && j >= 0 && i < ASSUMED_WORLDSIZE && j < ASSUMED_WORLDSIZE && knownTerrain[i][j] == 5)
                {
                    weightToAdd += 15;
                }
            }
        }

        if(knownTerrain[row][column] == 6)
        {
            //System.out.println("9");
            weightToAdd -= 10;
        }
        else if(knownTerrain[row][column] == 8)
        {
            //System.out.println("10");
            weightToAdd -= 1;
        }
        else if(knownTerrain[row][column] == 4)
        {
            //System.out.println("11");
            weightToAdd -= 999;
        }
        else if(knownTerrain[row][column] == 2)
        {
            //System.out.println("13");
            weightToAdd += 7;
        }
        else if(knownTerrain[row][column] == 3)
        {
            //System.out.println("14");
            weightToAdd += 8;
        }
        return weightToAdd;
    }

    public boolean coverCheck(int[][] knownTerrain, int row, int column)
    {
        if(row >= 0 && column >= 0 && row < ASSUMED_WORLDSIZE && column < ASSUMED_WORLDSIZE && (knownTerrain[row][column] == 1 || knownTerrain[row][column] == 2 || knownTerrain[row][column] == 3 || knownTerrain[row][column] == 7 || knownTerrain[row][column] == 5))
        {
            return true;
        }
        return false;
    }
}

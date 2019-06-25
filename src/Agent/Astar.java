package Agent;

import World.WorldMap;
import java.util.List;
import java.util.ArrayList;
import java.util.PriorityQueue;
import static World.GameScene.ASSUMED_WORLDSIZE;
import static World.GameScene.SCALING_FACTOR;
import static World.WorldMap.STRUCTURE;
import static World.WorldMap.DOOR;
import static World.WorldMap.WINDOW;
import static World.WorldMap.TARGET;
import static World.WorldMap.SENTRY;
import static World.WorldMap.DECREASED_VIS_RANGE;
import static World.WorldMap.WALL;
import static World.WorldMap.UNEXPLORED;
import javafx.geometry.Point2D;

import java.awt.Point;
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
    private Agent agent;
    private static List<Point> pointsToModify = new ArrayList<Point>();
    private static List<Point> pointsToModifyNoise = new ArrayList<Point>();

    public Astar(int width, int height, int si, int sj, int ei, int ej, int[][] blocks, Agent agent, boolean modify){
        this.si = si;
        this.sj = sj;
        this.ei = ei;
        this.ej = ej;
        this.agent = agent;
        if(modify)
        {
            pointsToModify.add(agent.points[0]);
            pointsToModify.add(agent.points[1]);
            pointsToModify.add(new Point(agent.points[0].x + 1, agent.points[0].y));
            pointsToModify.add(new Point(agent.points[0].x + 1, agent.points[0].y + 1));
            pointsToModify.add(new Point(agent.points[0].x, agent.points[0].y + 1));
            pointsToModify.add(new Point(agent.points[0].x - 1, agent.points[0].y + 1));
            pointsToModify.add(new Point(agent.points[0].x - 1, agent.points[0].y));
            pointsToModify.add(new Point(agent.points[0].x - 1, agent.points[0].y - 1));
            pointsToModify.add(new Point(agent.points[0].x, agent.points[0].y - 1));
            pointsToModify.add(new Point(agent.points[0].x + 1, agent.points[0].y - 1));

            for(int i = 0 ; i < agent.getAudioLogs().size() ; i ++)
            {
                double actualDirection = agent.audioLogs.get(i).getDirection();
                if(actualDirection < 0)
                {
                    actualDirection += 360;
                }
                Point2D source = agent.getMove(50, actualDirection);
                Point gridSource = new Point(agent.locationToWorldgrid(source.getX()), agent.locationToWorldgrid(source.getY()));
                pointsToModifyNoise.add(new Point(gridSource.x, gridSource.y));
                pointsToModifyNoise.add(new Point(gridSource.x+1, gridSource.y));
                pointsToModifyNoise.add(new Point(gridSource.x+1, gridSource.y+1));
                pointsToModifyNoise.add(new Point(gridSource.x, gridSource.y+1));
                pointsToModifyNoise.add(new Point(gridSource.x-1, gridSource.y+1));
                pointsToModifyNoise.add(new Point(gridSource.x-1, gridSource.y));
                pointsToModifyNoise.add(new Point(gridSource.x-1, gridSource.y-1));
                pointsToModifyNoise.add(new Point(gridSource.x, gridSource.y-1));
                pointsToModifyNoise.add(new Point(gridSource.x+1, gridSource.y-1));
            }
            agent.clearAudioLog();

        }
        grid = new Node[width][height];
        closeCell = new boolean[width][height];
        openCell = new PriorityQueue<>((Node n1, Node n2) -> Double.compare(n1.fCost, n2.fCost));
        for (int i = 0; i < width ; i++) {
            for (int j = 0; j < height; j++) {
                grid[i][j] = new Node(i, j);
                grid[i][j].heuristic = Math.abs(i - ei) + Math.abs(j - ej);
                grid[i][j].solution = false;
                if(agent.getClass() == Intruder.class)
                {
                    grid[i][j].heuristic += addWeight(agent.getKnownTerrain(), j, i);
                }
            }
        }
        if(agent.getClass() == Intruder.class)
        {
            for(int i = 0 ; i < pointsToModify.size() ; i++)
            {
                if(agent.getWorldGrid()[pointsToModify.get(i).y][pointsToModify.get(i).x] != WALL && agent.getWorldGrid()[pointsToModify.get(i).y][pointsToModify.get(i).x] != SENTRY && agent.getWorldGrid()[pointsToModify.get(i).y][pointsToModify.get(i).x] != STRUCTURE)
                {
                    //if(!nextToEntrance(new Point(pointsToModify.get(i).x, pointsToModify.get(i).y)))
                    //{
                    grid[pointsToModify.get(i).x][pointsToModify.get(i).y].heuristic += 50;
                    //}
                    //System.out.println(grid[pointsToModify.get(i).x][pointsToModify.get(i).y].heuristic);
                }
            }
            for(int i = 0 ; i < pointsToModifyNoise.size() ; i++)
            {
                if(agent.getWorldGrid()[pointsToModifyNoise.get(i).y][pointsToModifyNoise.get(i).x] != WALL && agent.getWorldGrid()[pointsToModifyNoise.get(i).y][pointsToModifyNoise.get(i).x] != SENTRY && agent.getWorldGrid()[pointsToModifyNoise.get(i).y][pointsToModifyNoise.get(i).x] != STRUCTURE)
                {
                    grid[pointsToModifyNoise.get(i).x][pointsToModifyNoise.get(i).y].heuristic += 30;
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
            weightToAdd -= 5;
        }
        if(coverCheck(knownTerrain, row+1, column))
        {
            weightToAdd -= 5;
        }
        if(coverCheck(knownTerrain, row, column-1))
        {
            weightToAdd -= 5;
        }
        if(coverCheck(knownTerrain, row-1, column-1))
        {
            weightToAdd -= 5;
        }
        if(coverCheck(knownTerrain, row-1, column))
        {
            weightToAdd -= 5;
        }
        if(coverCheck(knownTerrain, row-1, column+1))
        {
            weightToAdd -= 5;
        }
        if(coverCheck(knownTerrain, row, column+1))
        {
            weightToAdd -= 5;
        }
        if(coverCheck(knownTerrain, row+1, column+1))
        {
            weightToAdd -= 5;
        }
        for(int i = row ; i < row+16 ; i++)
        {
            for(int j = column ; j < column+16 ; j++)
            {
                if(i >= 0 && j >= 0 && i < ASSUMED_WORLDSIZE && j < ASSUMED_WORLDSIZE && knownTerrain[i][j] == SENTRY)
                {
                    weightToAdd += 30;
                }
            }
        }
        for(int i = row ; i < row+16 ; i++)
        {
            for(int j = column ; j > column-16 ; j--)
            {
                if(i >= 0 && j >= 0 && i < ASSUMED_WORLDSIZE && j < ASSUMED_WORLDSIZE && knownTerrain[i][j] == SENTRY)
                {
                    weightToAdd += 30;
                }
            }
        }
        for(int i = row ; i > row-16 ; i--)
        {
            for(int j = column ; j < column+16 ; j++)
            {
                if(i >= 0 && j >= 0 && i < ASSUMED_WORLDSIZE && j < ASSUMED_WORLDSIZE && knownTerrain[i][j] == SENTRY)
                {
                    weightToAdd += 30;
                }
            }
        }
        for(int i = row ; i > row-16 ; i--)
        {
            for(int j = column ; j > column-16 ; j--)
            {
                if(i >= 0 && j >= 0 && i < ASSUMED_WORLDSIZE && j < ASSUMED_WORLDSIZE && knownTerrain[i][j] == SENTRY)
                {
                    weightToAdd += 30;
                }
            }
        }
        if(knownTerrain[row][column] == DECREASED_VIS_RANGE)
        {
            weightToAdd -= 10;
        }
        else if(knownTerrain[row][column] == UNEXPLORED)
        {
            weightToAdd -= 5;
        }
        else if(knownTerrain[row][column] == TARGET)
        {
            weightToAdd -= 999;
        }
        else if(knownTerrain[row][column] == DOOR)
        {
            weightToAdd -= 10;
        }
        else if(knownTerrain[row][column] == WINDOW)
        {
            weightToAdd -= 10;
        }
        return weightToAdd;
    }

    public boolean coverCheck(int[][] knownTerrain, int row, int column)
    {
        if(row >= 0 && column >= 0 && row < ASSUMED_WORLDSIZE && column < ASSUMED_WORLDSIZE && (knownTerrain[row][column] == STRUCTURE || knownTerrain[row][column] == DOOR || knownTerrain[row][column] == WINDOW || knownTerrain[row][column] == WALL || knownTerrain[row][column] == SENTRY))
        {
            return true;
        }
        return false;
    }

    //public boolean nextToEntrance(Point point)
    //{
    //    if(agent.getWorldGrid()[point.x+1][point.y] == DOOR || agent.getWorldGrid()[point.x+1][point.y] == WINDOW)
    //    {
    //        return true;
    //    }
    //    else if()
    //}
}

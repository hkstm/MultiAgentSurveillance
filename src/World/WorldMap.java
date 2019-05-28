package World;
import Agent.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;

import javafx.geometry.Point2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static World.GameScene.ASSUMED_WORLDSIZE;
import static World.GameScene.SCALING_FACTOR;

/**
 * WorldMap data structure
 * @author Kailhan Hokstam
 */

public class WorldMap implements Serializable {

    public int[][] worldGrid;
    public static final int EMPTY = 0;
    public static final int STRUCTURE = 1;
    public static final int DOOR = 2;
    public static final int OPEN_DOOR = 22;
    public static final int WINDOW = 3;
    public static final int OPEN_WINDOW = 33;
    public static final int TARGET = 4;
    public static final int SENTRY = 5;
    public static final int DECREASED_VIS_RANGE = 6;
    public static final int WALL = 7;
    public static final int UNEXPLORED = 8;
    public static final int GUARD = 9;
    public static final int INTRUDER = 10;
    public static final int SOUND = 11;
    public static final int MARKER_1 = 41;
    public static final int MARKER_2 = 42;
    public static final int MARKER_3 = 43;
    public static final int MARKER_4 = 44;
    public static final int MARKER_5 = 45;
    private List<Agent> agents = new ArrayList<>();
    private List<Thread> agentThreads = new ArrayList<>();
    private List<Shape> agentsCones = new ArrayList<>();
    private List<Shape> worldGridShapes = new ArrayList<>();

    private int size;

    //public WorldMap() {
    //    this(200);
    //}

    public WorldMap(int size) {
        this(size, new ArrayList<Agent>());
    }

    public WorldMap(int size, ArrayList<Agent> agents) {
        this.size = size;
        this.worldGrid = new int[size][size];
        this.agents = agents;
        for(int i = 0; i < size; i++) {
            worldGrid[0][i] = WALL;
            worldGrid[i][0] = WALL;
            worldGrid[size-1][i] = WALL;
            worldGrid[i][size-1] = WALL;
        }
    }

    public WorldMap(WorldMap worldMap) {
        this.size = worldMap.getSize();
        this.worldGrid = new int[size][size];
        this.agents = worldMap.getAgents();
        for(int r = 0; r < size; r++) {
            for(int c = 0; c < size; c++) {
                this.worldGrid[r][c] = worldMap.getWorldGrid()[r][c];
            }
        }
    }

    /**
     * Prints out the world for diagnostic purposes
     */
    public void displayWorldGrid() {
        for(int r = 0; r < size; r++) {
            for(int c = 0; c < size; c++) {
                System.out.printf("%3d", worldGrid[r][c]);
            }
            System.out.println();
        }
    }

    public int[][] getWorldGrid()
    {
        return worldGrid;
    }

    public void setWorldGrid(int[][] worldGrid)
    {
        this.worldGrid = worldGrid;
    }

    public int getSize()
    {
        return size;
    }

    /**
     * updates tile at r, c to certain type e.g. WALL
     * @param r row
     * @param c column
     * @param state state to compare to
     */
    public void updateTile(int r, int c, int state) {
        worldGrid[r][c] = state;
    }

    public int getTileState(int r, int c) {
         return worldGrid[r][c];
    }

    /**
     * checks if a square has a certain state
     * @param r row
     * @param c column
     * @param state state to compare to
     * @return true if the square has the same state at state
     */
    public boolean checkTile(int r, int c, int state) {
        return (worldGrid[r][c] == state);
    }

    public List<Agent> getAgents() {
        return this.agents;
    }

    public void setAgents(List<Agent> agents) {
        this.agents = agents;
    }

    /**
     * Removes an agent by exiting its while logic loop (where we determine what an agent does every tick)
     * And removes it from a worlds list of active agent and active threads
     * @param toBeRemoved
     * @return index of agent removed
     */
    public int removeAgent(Agent toBeRemoved) {
        int index = agents.indexOf(toBeRemoved);
        agents.get(index).setThreadStopped(true);
        agents.remove(index);
        agentThreads.remove(index);
        return index;
    }


    /**
     * Adds an agent and its (not yet running thread) to a world, needs to be started for it to actually start executing its logic
     * @param toBeAdded the agent that we want to add (probably want to at least specify its location before adding to world)
     */
    public void addAgent(Agent toBeAdded) {
        this.agents.add(toBeAdded);
        Thread threadToBeAdded = new Thread(toBeAdded);
        threadToBeAdded.setPriority(1);
        this.agentThreads.add(threadToBeAdded);
        //startAgents();
    }

    public void addOnlyAgent(Agent toBeAdded) {
        this.agents.add(toBeAdded);
        //startAgents();
    }

    /**
     * Starts all agents whos threads have been added to the world
     */
    public void startAgents() {
        for(Thread thread : this.agentThreads) {
            thread.start();
        }
    }

    /**
     * Removes all agents from the world, first stops their threads
     */
    public void removeAllAgents() {
        for(Agent agent : agents) {
            agent.setThreadStopped(true);
        }
        agents.clear();
        agentThreads.clear();
        System.out.println("Removed all agents");
    }

    public void forceUpdateAgents() {
        for(Agent agent : agents) {
            agent.forceUpdate();
        }
    }

    /**
     * Loops through all agents and checks if any agent is in target location, assuming theres only 1 intruder that needs to reach it
     * @return true if 1 agent is in target spot otherwise false
     */
    public boolean intruderInTarget() {
        for(Agent agent : agents) {
            if(agent instanceof Intruder) {
//                if(coordinatesToCell(agent.getPosition()) == TARGET) {
//                    System.out.println("agent intruderInTargetPos: " + agent.getPosition().toString());
//                    return true;
//                }
            }
        }
        return false;
    }

    /**
     * check what type of terrain is at a given point
     * @param location is the point at which the terrain type is desired
     * @return an integer describing the terrain type in the worldGrid corresponding to the input location
     */

    public int coordinatesToCell(Point2D location) {
        int windowSize = StartWorldBuilder.WINDOW_SIZE;
        int rowIndex = (int) ((location.getY()/windowSize) * worldGrid.length);
        int columnIndex = (int) ((location.getX()/windowSize) * worldGrid.length);
        return worldGrid[rowIndex][columnIndex];
    }

    public void fillWorldArray(int topLeftRow, int topLeftCol, int botRightRow, int botRightCol, int tileStatus) {
        for(int i = topLeftRow; i < botRightRow; i++) {
            for(int j = topLeftCol; j < botRightCol; j++) {
                worldGrid[i][j] = tileStatus;
            }
        }
    }

    public boolean isNotEnterableTile(int toCheck) {
        if((toCheck == STRUCTURE) || (toCheck == WALL)) return true;
        else return false;
    }

    public boolean isVisionObscuring(int toCheck) {
        if ((toCheck == STRUCTURE) || (toCheck == WALL) || (toCheck == DOOR) ||
                (toCheck == WINDOW)) {
            return true;
        }
        else return false;
    }

    public void createCones() {
        agentsCones.clear();
        for(Agent agent : agents) {
            agent.createCone();
            agentsCones.add(agent.getCone());
        }
    }

    public void createWorldGridShapes() {
        worldGridShapes.clear();
        for(int r = 0; r < worldGrid.length; r++) {
            for( int c = 0; c < worldGrid[0].length; c++) {
                if(isVisionObscuring(worldGrid[r][c])) {
                    Rectangle tile = new Rectangle();
                    tile.setX(convertArrayToWorld(c));
                    tile.setY(convertArrayToWorld(r));
                    tile.setWidth((ASSUMED_WORLDSIZE/worldGrid.length)*SCALING_FACTOR);
                    tile.setHeight((ASSUMED_WORLDSIZE/worldGrid.length)*SCALING_FACTOR);
                    tile.setFill(Color.BLACK);
                    worldGridShapes.add(tile);
                }
            }
        }
    }

    public double convertArrayToWorld(int arrayIndex)
    {
        return arrayIndex*(ASSUMED_WORLDSIZE/worldGrid.length)*SCALING_FACTOR; //changed 200 to 100
    }

    public List<Shape> getWorldGridShapes() {
        return worldGridShapes;
    }

    public void setWorldGridShapes(List<Shape> worldGridShapes) {
        this.worldGridShapes = worldGridShapes;
    }

    public List<Shape> getAgentsCones() {
        return agentsCones;
    }

    public void setAgentsCones(List<Shape> agentsCones) {
        this.agentsCones = agentsCones;
    }
}
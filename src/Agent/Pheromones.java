package Agent;

import World.TileView;
import World.WorldMap;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;

import java.util.List;

import static Agent.Agent.locationToWorldgrid;
import static World.GameScene.ASSUMED_WORLDSIZE;
import static World.GameScene.SCALING_FACTOR;
import static World.WorldMap.*;

public class Pheromones {

    private Pcell [][] mapPhero;
    private WorldMap worldMap;


    public Pheromones(WorldMap worldMap){
        mapPhero = new Pcell[worldMap.getSize()][worldMap.getSize()];
        this.worldMap = worldMap;
        for (int i = 0; i < mapPhero.length; i++) {
            for (int j = 0; j < mapPhero[0].length; j++) {
                mapPhero[i][j] = new Pcell();
            }
        }
    }

    public void update(double delta){
        colorTiles();
        updateMapPhero(delta);

    }


    public void colorTiles(){
        for(Agent agent: worldMap.getAgents() ) {
            if(agent instanceof Guard) mapPhero[locationToWorldgrid(agent.getPosition().getY())][locationToWorldgrid(agent.getPosition().getX())] = new Pcell(Color.GREEN);
            if(agent instanceof Intruder) mapPhero[locationToWorldgrid(agent.getPosition().getY())][locationToWorldgrid(agent.getPosition().getX())] = new Pcell(Color.RED);
        }
        updateWorldMap();
    }

    public void updateWorldMap(){
        for (int r = 0; r < worldMap.getSize(); r++) {
            for (int c = 0; c < worldMap.getSize(); c++) {
                if(mapPhero[r][c] != null) {
                    if (mapPhero[r][c].getCol() == Color.RED) {
                        worldMap.updateTile(r, c, MARKER_1);

                    }else if(mapPhero[r][c].getCol() == Color.GREEN) {
                        worldMap.updateTile(r, c, MARKER_2);
                    }
                } else {
                    if(worldMap.getTileState(r,c) == MARKER_1 || worldMap.getTileState(r, c) == MARKER_2) worldMap.updateTile(r, c, EMPTY);
                }
            }
        }
    }

    public  int locationToWorldgrid(double toBeConverted) {
        return (int)((toBeConverted * (1/((ASSUMED_WORLDSIZE/mapPhero.length)*SCALING_FACTOR))));
    }

    public void updateMapPhero(double delta){
        for(int i =0; i< mapPhero.length;i++){
            for (int j = 0; j < mapPhero[0].length ; j++) {
                if(mapPhero[i][j] != null) {
                    mapPhero[i][j].updateLifetime(delta);
//                    System.out.println("lifetime: "+mapPhero[i][j].getLifetime());
                    if(mapPhero[i][j].getLifetime() < 0) mapPhero[i][j] = null;
                }
            }
        }
    }

    public Pcell[][] getMapPhero() {
        return mapPhero;
    }

    public class Pcell{
        private Color col;
        private int count = 2; //frame counts for a pheromone to last
        private double lifetime;

        public Pcell(Color col, double lifetime) {
            this.col = col;
            this.lifetime = lifetime * 1e9; //delta in nanoseconds
        }

        public Pcell(Color col) {
            this(col, 10);
        }

        public Pcell(){
            this(Color.BLACK);
        }

        //getters and setters

        public void setCol(Color col) {
            this.col = col;
        }

        public void setCount(int count) {
            this.count = count;
        }

        public Color getCol() {
            return col;
        }

        public int getCount() {
            return count;
        }

        public void updateLifetime(double delta) {
            lifetime -= delta;
        }

        public double getLifetime() {
            return lifetime;
        }
    }
}

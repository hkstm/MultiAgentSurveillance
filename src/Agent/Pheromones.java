package Agent;

import World.TileView;
import World.WorldMap;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;

import java.util.List;

import static Agent.Agent.locationToWorldgrid;
import static World.GameScene.ASSUMED_WORLDSIZE;
import static World.GameScene.SCALING_FACTOR;
import static World.WorldMap.MARKER_1;
import static World.WorldMap.MARKER_2;

public class Pheromones {

    private Pcell [][] mapPhero;
    private List<Guard> guards;
    private List<Intruder> intruders;
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
        for (Guard g : guards){
            mapPhero[locationToWorldgrid(g.getPosition().getY())][locationToWorldgrid(g.getPosition().getX())] = new Pcell(Color.GREEN);
            //mapPhero[(int)gpos.getX()][(int)gpos.getY()] = new Pcell(Color.GREEN);
        }

        for (Intruder i : intruders){
            //System.out.println("og i x:" + i.getPosition().getX() + "og i y"+i.getPosition().getY());
            mapPhero[locationToWorldgrid(i.getPosition().getY())][locationToWorldgrid(i.getPosition().getX())] = new Pcell(Color.RED);
           // System.out.println("i x:" + (int)ipos.getX() + "i y:" + (int)ipos.getY());
            //mapPhero[(int)ipos.getX()][(int)ipos.getY()] = new Pcell(Color.RED);
        }
        updateWorldMap();
    }

    public void updateWorldMap(){
        for (int r = 0; r < worldMap.getSize(); r++) {
            for (int c = 0; c < worldMap.getSize(); c++) {
                if (mapPhero[r][c].getCol() == Color.GREEN) {
                    worldMap.worldGrid[r][c] = MARKER_1;

                }else if(mapPhero[r][c].getCol() == Color.RED) {
                    worldMap.worldGrid[r][c] = MARKER_2;
                }
            }
        }
    }

    public void setAgents(List<Guard> guards,List<Intruder> intruders){
        this.guards = guards;
        this.intruders = intruders;
    }

    public  int locationToWorldgrid(double toBeConverted) {
        return (int)((toBeConverted * (1/((ASSUMED_WORLDSIZE/mapPhero.length)*SCALING_FACTOR))));
    }

    public void updateMapPhero(double delta){
        for(int i =0; i< mapPhero.length;i++){
            for (int j = 0; j < mapPhero[0].length ; j++) {
                mapPhero[i][j].updateLifetime(delta);
                if(mapPhero[i][j].getLifetime() < 0) mapPhero[i][j] = null;
            }
        }
    }

    public Pcell[][] getMapPhero() {
        return mapPhero;
    }

    public class Pcell{
        private Color col;
        private int count = 10; //frame counts for a pheromone to last
        private double lifetime;

        public Pcell(Color col, double lifetime) {
            this.col = col;
            this.lifetime = lifetime;
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

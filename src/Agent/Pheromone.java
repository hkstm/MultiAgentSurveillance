package Agent;

import javafx.geometry.Point2D;
import javafx.scene.paint.Color;

import java.util.List;

import static Agent.Agent.locationToWorldgrid;

/* This class serves as indirect (stigmergic) communication between agents.
 *
 * Agents have a data structure representing the trail left behind from walking about e.g. a queue or linked list
 * The current tile an agent is on should NOT be apart of its queue, but a "candidate" for the queue's head.
 *
 *
 * Must be able to check if the current tile has pheromones on it.
 * Must be able to identify the agent who "dropped" the found pheromones.
 *
 * The most recent agent that traverses [x,y] leaves their pheromone there - not one-to-many.
 *
 * @author Olive, Costi
 */

public class Pheromones {

    private Pcell [][] mapPhero;
    private List<Guard> guards;
    private List<Intruder> intruders;


    public Pheromones(int w, int h){
        mapPhero = new Pcell[w][h];

        for (int i = 0; i < mapPhero.length; i++) {
            for (int j = 0; j < mapPhero[0].length; j++) {
                mapPhero[i][j] = new Pcell();
            }
        }
    }

    public void update(){
        colorTiles();
        countManager();

    }

    private void countManager(){
        for (int i = 0; i < mapPhero.length; i++) {
            for (int j = 0; j < mapPhero[0].length; j++) {
                if(mapPhero[i][j].count>0){
                    mapPhero[i][j].count --;
                }

                if(mapPhero[i][j].count < 0){
                    mapPhero[i][j].count = 0;
                }
            }
        }
    }

    public void colorTiles(){
        for (Guard g : guards){

            System.out.println("og g x:" + g.getPosition().getX() + "og g y"+g.getPosition().getY());
            Point2D gpos = coordinatesToCell(g.position);


            System.out.println("g x:" + (int)gpos.getX() + "g y:" + (int)gpos.getY());
            mapPhero[(int)gpos.getX()][(int)gpos.getY()] = new Pcell(Color.GREEN);
        }

        for (Intruder i : intruders){
            System.out.println("og i x:" + i.getPosition().getX() + "og i y"+i.getPosition().getY());
            Point2D ipos = coordinatesToCell(i.position);
            System.out.println("i x:" + (int)ipos.getX() + "i y:" + (int)ipos.getY());
            mapPhero[(int)ipos.getX()][(int)ipos.getY()] = new Pcell(Color.RED);
        }
    }

    public void setAgents(List<Guard> guards,List<Intruder> intruders){
        this.guards = guards;
        this.intruders = intruders;
    }


    public Point2D coordinatesToCell(Point2D location) {
        int rowIndex = locationToWorldgrid(location.getY());
        int columnIndex = locationToWorldgrid(location.getX());
        return new Point2D(rowIndex, columnIndex);
    }

    public  int locationToWorldgrid(double toBeConverted) {
        return (int)((toBeConverted * 100)/700);
    }

    public Pcell[][] getMapPhero() {
        return mapPhero;
    }

    public class Pcell{
        private Color col;
        private int count = 100; //frame counts for a pheromone to last

        public Pcell(Color col) {
            this.col = col;
        }

        public Pcell(){
            this.col = Color.BLACK;
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
    }
}


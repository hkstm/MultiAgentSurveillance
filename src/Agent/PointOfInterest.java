package Agent;

import java.awt.*;

public class PointOfInterest {

    private int tileType;
    private double interestFactor;
    private int x;
    private int y;

    PointOfInterest(int x, int y, int tileType, double interestFactor) {
        this.x = x;
        this.y = y;
        this.tileType = tileType;
        this.interestFactor = interestFactor;
    }

    public boolean equals(PointOfInterest point2) {
        if(this.x == point2.getX() && this.y == point2.getY()) {
            this.tileType = point2.getTileType();
            this.interestFactor = point2.getInterestFactor();
            return true;
        }
        return false;
    }

    public int getTileType() {
        return tileType;
    }

    public double getInterestFactor() {
        return interestFactor;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}

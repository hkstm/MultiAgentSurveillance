package Agent;

import javafx.geometry.Point2D;

/**
 * Reward points used in the AreaOptimzer bot
 * @author Kailhan
 */

public class Point2DReward extends Point2D {

    private double reward;

    public Point2DReward(double x, double y, double reward) {
        super(x, y);
        this.reward = reward;
    }

    public Point2DReward(double x, double y) {
        this(x, y, 0);
    }

    public double consumeReward(){
        double rewardToBeConsumed = this.reward;
        resetReward();
        return rewardToBeConsumed;
    }

    public void updateReward(double toBeAdded) {
        this.reward += toBeAdded;
    }

    public void resetReward(){
        this.reward = 0;
    }

    public double getReward() {
        return reward;
    }
}
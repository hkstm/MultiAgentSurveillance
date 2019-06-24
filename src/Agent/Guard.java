package Agent;
import World.GameScene;
import World.WorldMap;
import javafx.scene.paint.Color;

import javafx.geometry.Point2D;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static Agent.MoveTo.destX;
import static Agent.MoveTo.destY;
import static World.GameScene.SCALING_FACTOR;
import static World.WorldMap.SENTRY;
import static World.WorldMap.TARGET;


public class Guard extends Agent {

    /**
     * A subclass of Agent for the Guards with an internal map containing the starting positions of other guards and the terrain across the map
     * @author Benjamin, Thibaut, Kailhan, Costi
     */


    Routine routine;
    protected boolean tired;
    protected final long createdMillis = System.currentTimeMillis();
    protected double timeCost; //time until end in seconds
    protected double distanceCost; //meters moved
    protected double directCommsCost; //message size in "bytes"
    protected double indirectCommsCost; //number of markers placed;
    protected Intruder intruder;
    private int count = 0;
    protected boolean chasing;

    private boolean firstRunBehaviourTreeGuardLogic;

    public Guard(Point2D position, double direction) {
        super(position, direction);
        this.timeCost = 0;
        this.distanceCost = 0;
        this.directCommsCost = 0;
        this.indirectCommsCost = 0;
        this.viewingAngle = 45;
//        this.viewingAngle = 60;
        this.visualRange[0] = 0;
        this.visualRange[1] = 8;
        this.chasing = false;
//        this.visualRange[1] = 20;
        this.color = Color.AZURE;
        this.firstRunBehaviourTreeGuardLogic = true;
        Routine guard1 = Routines.sequence(
                Routines.moveTo(locationToWorldgrid(600.0),locationToWorldgrid(200.0))

                // Routines.wander(worldMap,this)
        );
        this.setRoutine(guard1);
        System.out.println("Guard initialized");

        //this.knownTerrain = worldMap.getWorldGrid();
    }

    public void updatePerformanceCriteria() {
        timeCost += delta;
        distanceCost += previousPosition.distance(position);
    }

    /**
     * Logic that gets executed every tick
     */
    public void executeAgentLogic(){
        if(firstRunBehaviourTreeGuardLogic) {
            routine.start();
            firstRunBehaviourTreeGuardLogic = false;
        }
        currentTime = System.nanoTime();
        delta = currentTime - previousTime;
        delta /= 1e9; //makes it in seconds
        System.out.println("y: " + locationToWorldgrid( getPosition().getX() )+ "x: " + locationToWorldgrid( getPosition().getY()));
        update();
        updatePerformanceCriteria();
    }

    public boolean equals(Object obj) {
        boolean equals = false;
        if (obj == null) return false;
        if (obj == this) return true;
        if (!(obj instanceof Guard)) return false;
        Guard o = (Guard) obj;
        if((o.direction == this.direction) && (o.position.equals(this.position))) equals = true;
        return equals;
    }
    public void updateVisualRange(){
        if (worldMap.coordinatesToCell(position) == SENTRY) { // row.e. guard in on a tower
           this.visualRange[0] = 2;
           this.visualRange[1] = 15;
           this.viewingAngle = 30;
        } else {
            this.visualRange[0] = 0;
            this.visualRange[1] = 6;
            this.viewingAngle = 45;

        }
    }

    public void openTower() {
        if (worldMap.coordinatesToCell(position) == SENTRY)
        {
            class OpenTower extends TimerTask
            {
                public void run()
                {
                    //worldMap.updateTile((int)position.getRow(), (int)position.getColumn(), SENTRY);
                    knownTerrain[(int)position.getX()][(int)position.getY()] = SENTRY;
                }
            }
            Timer timer = new Timer();
            TimerTask openTower = new OpenTower();


                timer.schedule(openTower, 3000);
            timer.cancel();
        }
    }
    public void update() {
        if (routine.getState() == null) {
            // hasn't started yet so we start it
            routine.start();
        }
        routine.act(this, worldMap);
    }
    public String getRoutine() {
        return routine.getClass().getSimpleName();
    }

    public void setRoutine(Routine routine) {
        this.routine = routine;
    }

    public void gameTree(double timeStep)
    {

        double walkingDistance = (BASE_SPEED *SCALING_FACTOR);
        updateWalls();
        if(oldTempGoal != null) {
            checkChangedStatus();
        }
//        double elapsedTime = (System.currentTimeMillis()-startTime)/1000;
//        if(elapsedTime > freezeTime)
//        {
            frozen = false;
            startTime = 0;
            freezeTime = 0;
            oldTempGoal = tempGoal;
            int[][] blocks = aStarTerrain(knownTerrain);
            Astar pathMaker = new Astar(knownTerrain[0].length, knownTerrain.length, (int)(position.getX()/SCALING_FACTOR),
                    (int)(position.getY()/SCALING_FACTOR), (int)destY, (int)destX, blocks, this,false);
            List<Node> path = pathMaker.findPath();
        System.out.println(path.size());
            if(!chasing) {
                if (path.size() <= 2) {
                    if (count % 2 == 0) {
                        System.out.println("-600");
                        count += 1;
                        System.out.println("count: " + count);
                        destX -= locationToWorldgrid(600);
                        destY += locationToWorldgrid(100);
                        updateDirection(direction + 90);
                        updatePath();

                        return;
                    } else {
                        System.out.println("+600");
                        count += 1;
                        System.out.println("count: " + count);
                        destX += locationToWorldgrid(600);
                        destY += locationToWorldgrid(100);
                        updateDirection(direction - 90);
                        updatePath();

                        return;
                    }
                }
            }

            if(!changed)
            {
                tempGoal = new Point2D((path.get(path.size()-1).row*SCALING_FACTOR)+worldMap.convertArrayToWorld(1)/2,
                        (path.get(path.size()-1).column*SCALING_FACTOR)+(SCALING_FACTOR/2));
                if (path.size() > 1) {
                    previousTempGoal = new Point2D((path.get(path.size() - 2).row * SCALING_FACTOR) + (SCALING_FACTOR / 2),
                            (path.get(path.size() - 2).column * SCALING_FACTOR) + (SCALING_FACTOR / 2));
                }
                else{
                    previousTempGoal = tempGoal;
                }
            }
            wallPhaseDetection();
            cornerCorrection();
            double divisor = Math.abs(tempGoal.getY()-position.getY());
            double preDivisor = Math.abs(previousTempGoal.getY()-tempGoal.getY());
            if(divisor == 0)
            {
                divisor++;
            }
            else if (preDivisor == 0){
                preDivisor++;
            }
            double turnAngle = Math.toDegrees(Math.atan(Math.abs(tempGoal.getX()-position.getX())/divisor));
            performTurn(turnAngle);
                if(legalMoveCheck(walkingDistance))
                {
                    move(walkingDistance);

                }
        //    }
        }
    }

package Agent;

import World.WorldMap;

public class DoAgain extends Routine {
    Routine routine;
    int times; //how many times the routine is executed
    int resetTimes;


    public DoAgain(Routine routine){
        super();
        this.routine = routine;
        this.times = -1;
        this.resetTimes = times;
    }
    public DoAgain(Routine routine,int times){
        super();
        if(times<1){
            throw  new RuntimeException("can't do an action a negative amount of times...");
        }
        this.routine = routine;
        this.times = times;
        this.resetTimes = times;
    }

    @Override
    public void start(){
        super.start();
        this.routine.start();
    }

    public void reset() {
        this.times = resetTimes;
    }
    @Override
    public void act(Guard guard, WorldMap worldMap) {
        if(routine.isFailure()){
            fail();
        }
        else if(routine.isSuccess()){
            if(times == 0){
                succeed();
                return;
            }
            if(times > 0 || times == -1){
                times --;
                routine.reset();
                routine.start();
            }
        }
        if(routine.isWalking()){
            routine.act(guard, worldMap);
        }
    }

}

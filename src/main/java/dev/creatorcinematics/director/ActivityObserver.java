package dev.creatorcinematics.director;

/** Qualifies stays without retaining a keyboard log or counting cinematic time. */
public final class ActivityObserver {
    private Point anchor;
    private long since,lastSample;
    public boolean sample(ActivityMemory memory,Point eye,long time,boolean observing){
        if(!observing){reset();return false;}
        if(anchor==null || time-lastSample>40 || time<lastSample || eye.distance(anchor)>3){anchor=eye;since=time;}
        lastSample=time;
        if(time-since<160)return false;
        memory.record(eye,ActivityMemory.Kind.VISIT,time);
        return true;
    }
    public void reset(){anchor=null;}
}

package dev.creatorcinematics.director;

/** Frame-time fade; a cut is permitted only while fully covered. */
public final class ShotTransition {
    private long started;
    private int phase;
    public boolean ready(long now){
        if(phase==2 && now-started<1_000_000_000L)return false;
        if(phase!=1){phase=1;started=now;}
        return now-started>=600_000_000L;
    }
    public void complete(long now) {if(phase==1){phase=2;started=now;}}
    public double alpha(long now){
        if(phase==0)return 0;
        double t=Math.clamp((now-started)/(phase==1?600_000_000d:1_000_000_000d),0,1);
        double eased=t*t*(3-2*t);
        return phase==1?eased:1-eased;
    }
    public void reset() {phase=0;}
}

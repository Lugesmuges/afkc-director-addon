package dev.creatorcinematics.director;

/** Monotonic time, independent of frame rate, world ticks, and wall-clock corrections. */
public final class StatsTimeline {
    public record Frame(int index,double alpha,double offsetY) { public boolean visible(){return alpha>0;} }
    private static final Frame HIDDEN=new Frame(0,0,0);
    private boolean active;
    private long started;
    public void start(long now) {active=true;started=now;}
    public void stop() {active=false;}
    public Frame frame(long now,int count) {
        if(!active || count<=0)return HIDDEN;
        double elapsed=(now-started)/1_000_000_000.0-6;
        if(elapsed<0)return HIDDEN;
        long cycle=(long)Math.floor(elapsed/7.8);
        double phase=elapsed-cycle*7.8;
        if(phase>=7)return HIDDEN;
        double enter=ease(phase/.6),leave=ease((7-phase)/.6);
        return new Frame((int)(cycle%count),Math.min(enter,leave),(1-enter)*6+(1-leave)*-3);
    }
    private static double ease(double value) {double t=Math.clamp(value,0,1);return t*t*(3-2*t);}
}

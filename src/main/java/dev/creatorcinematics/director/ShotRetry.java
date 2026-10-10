package dev.creatorcinematics.director;

/** Bounds expensive scene selection when no acceptable framing exists. */
public final class ShotRetry {
    private int failures;
    public int rejectedDelay(){int delay=5<<Math.min(failures,3);failures=Math.min(3,failures+1);return delay;}
    public void accepted(){failures=0;}
}

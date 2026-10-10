package dev.creatorcinematics.director;

import java.util.*;

/** Supplies framing hints to AFKC. Contains no animation, shot timer, easing or audio. */
public final class ShotAdvisor {
    public interface Space {
        boolean clear(Point from,Point to);
        boolean visible(Point from,Point focus);
        default boolean unobstructed(Point from,Point to){return clear(from,to);}
        default boolean lensClear(Point from,Point to){return unobstructed(from,to);}
        default boolean framingClear(Point camera,Point direction,double fov,double aspect){return true;}
        default Point surface(Point camera,Point focus,Point min,Point max){return focus;}
    }
    public record Advice(Point camera,Point focus) { }
    private final Random random;
    private int sequence,detailSequence;
    private double lastAngle=Double.NaN;
    public ShotAdvisor(Random random){this.random=random;}
    public Advice advise(Point eye,Point nativeCamera,ActivityMemory.Subject subject,Point context,Space space,double radius) {
        Point offset=nativeCamera.subtract(eye);
        if(offset.length()>radius || offset.length()<1.15)return null;
        int stage=sequence++%4;
        Point target=subject.focus(),min=subject.min(),max=subject.max();
        if(stage==1 || stage==3) {
            if(subject.landmarks().isEmpty())return null;
            target=subject.landmarks().get(detailSequence++%subject.landmarks().size());min=max=target;
        } else if(stage==2 && context!=null) {target=context;min=max=target;}
        double nativeAngle=Math.atan2(offset.z(),offset.x());
        double distance=Math.hypot(offset.x(),offset.z());
        int sign=random.nextBoolean()?1:-1;
        Advice repeated=null;
        for(int attempt=0;attempt<12;attempt++) {
            double angle=nativeAngle+attempt*sign*Math.PI/6;
            Point camera=attempt==0?nativeCamera:eye.add(new Point(Math.cos(angle)*distance,offset.y(),Math.sin(angle)*distance));
            if(!space.clear(eye,camera))continue;
            Point focus=space.surface(camera,target,min,max);
            if(focus==null || camera.distance(focus)<1.15 || !space.visible(camera,focus))continue;
            if(!FrameQuality.acceptable(camera,focus.subtract(camera),30,16.0/9,space))continue;
            // AFKC clamps animated focus positions to 16 horizontal blocks. Leave room for its
            // native focus oscillation so it does not pull a distant subject away after the cut.
            if(Math.hypot(focus.x()-eye.x(),focus.z()-eye.z())>15.5)continue;
            Advice advice=new Advice(camera,focus);
            if(Double.isFinite(lastAngle) && Math.cos(angle-lastAngle)>0.94){repeated=advice;continue;}
            lastAngle=angle;return advice;
        }
        return repeated; // A single safe opening is preferable to an unrelated target.
    }
}

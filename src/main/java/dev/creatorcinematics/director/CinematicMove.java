package dev.creatorcinematics.director;

/** One fixed subject and one deliberate travel direction. No per-tick random motion. */
public final class CinematicMove {
    private final Point start,end,focus;
    private final int durationTicks;

    private CinematicMove(Point start,Point end,Point focus,int durationTicks){
        this.start=start;this.end=end;this.focus=focus;this.durationTicks=durationTicks;
    }
    public int durationTicks(){return durationTicks;}
    public ShotAdvisor.Advice at(int elapsedTicks){
        double t=Math.clamp((double)elapsedTicks/(durationTicks-1),0,1);
        // Zero velocity and acceleration at both ends; never reverses within a shot.
        double travel=t*t*t*(10+t*(-15+6*t));
        return new ShotAdvisor.Advice(start.lerp(end,travel),focus);
    }
    public static CinematicMove plan(Point start,Point focus,Point eye,double radius,int sequence,
                                     ShotAdvisor.Space space,double fov,double aspect){
        int duration=360+Math.floorMod(sequence,5)*40;
        var hold=new CinematicMove(start,start,focus,duration);
        Point delta=focus.subtract(start);double distance=delta.length();
        if(!Double.isFinite(distance) || distance<1.15)return hold;
        Point forward=delta.scale(1/distance),side=new Point(-forward.z(),0,forward.x());
        side=side.length()<0.001?new Point(1,0,0):side.scale(1/side.length());
        Point up=new Point(0,1,0);
        Point[] directions={forward,forward.scale(-1),side,side.scale(-1),up,up.scale(-1),
                side.add(up.scale(.5)),side.scale(-1).add(up.scale(.5)),
                side.add(up.scale(-.5)),side.scale(-1).add(up.scale(-.5))};
        for(int attempt=0;attempt<directions.length;attempt++){
            int style=Math.floorMod(sequence+attempt,directions.length);
            double length=Math.min(style==4 || style==5?1.2:2.8,distance*(style<2?0.24:0.18));
            for(double scale:new double[]{1,0.6,0.3}){
                Point end=start.add(directions[style].scale(length*scale/directions[style].length()));
                if(!space.clear(start,end))continue;
                int steps=Math.max(2,(int)Math.ceil(start.distance(end)/0.25));
                boolean safe=true;
                for(int i=0;i<=steps;i++){
                    Point camera=start.lerp(end,(double)i/steps);
                    if(camera.distance(eye)>radius || camera.distance(focus)<1.5 || !space.visible(camera,focus)
                            || !FrameQuality.acceptable(camera,focus.subtract(camera),fov,aspect,space)){
                        safe=false;break;
                    }
                }
                if(safe)return new CinematicMove(start,end,focus,duration);
            }
        }
        // A safe still image is preferable to a correction that bounces off a nearby wall.
        return hold;
    }
}

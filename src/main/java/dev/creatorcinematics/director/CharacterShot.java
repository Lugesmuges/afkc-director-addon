package dev.creatorcinematics.director;

/** Alternating sides of the actor, with optional views from a visited window interior. */
public final class CharacterShot {
    private CharacterShot() { }
    public static ShotAdvisor.Advice plan(Point eye,Point facing,int sequence,double radius,ShotAdvisor.Space space,boolean window,double aspect){
        Point focus=eye.add(new Point(0,-.65,0));
        double base=Math.atan2(facing.z(),facing.x())+Math.floorMod(sequence,4)*Math.PI/2;
        for(int angle=0;angle<16;angle++){
            double azimuth=base+angle*Math.PI/8;
            for(double height:new double[]{0,2.5,5})for(double distance:new double[]{6,9,12}){
                Point camera=eye.add(new Point(Math.cos(azimuth)*distance,height,Math.sin(azimuth)*distance));
                if(camera.distance(eye)>radius || camera.distance(focus)>space.viewDistance())continue;
                if(window && !space.windowView(camera,focus))continue;
                if(!space.visible(camera,focus) || !FrameQuality.acceptable(camera,focus.subtract(camera),30,aspect,space))continue;
                if(!space.reachable(eye,camera))continue;
                return new ShotAdvisor.Advice(camera,focus);
            }
        }
        return null;
    }
}

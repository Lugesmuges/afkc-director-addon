package dev.creatorcinematics.director;

/** A whole remembered project, including the lens and framing constraint for its movement. */
public record WideShot(Point camera,Point focus,Point min,Point max,int fov) {
    private static final double HORIZONTAL=.85,VERTICAL=.68;
    public static WideShot plan(ActivityMemory.Subject subject,Point eye,Point nativeCamera,
            ShotAdvisor.Space space,double radius,double aspect,int sequence){
        if(!subject.built() || subject.max().subtract(subject.min()).length()<12)return null;
        Point padding=new Point(.75,.75,.75),min=subject.min().subtract(padding),max=subject.max().add(padding);
        Point focus=min.lerp(max,.5),toward=eye.subtract(focus);
        double azimuth=Math.atan2(toward.z(),toward.x());
        // A stable lens per shot. Prefer the original narrow lens, widening only if reach requires it.
        for(int fov:new int[]{30,45,55,65,70})for(int step=0;step<12;step++)for(double elevation:new double[]{.08,.26,.52,0}){
            double angle=azimuth+Math.floorMod(sequence+step,12)*Math.PI/6;
            Point outward=new Point(Math.cos(angle)*Math.cos(elevation),Math.sin(elevation),Math.sin(angle)*Math.cos(elevation));
            Point forward=outward.scale(-1),right=right(forward),up=cross(right,forward);
            double tangent=Math.tan(Math.toRadians(fov/2.0)),distance=4;
            for(Point corner:corners(min,max)){
                Point p=corner.subtract(focus);double z=dot(p,forward);
                distance=Math.max(distance,Math.max(Math.abs(dot(p,right))/(tangent*aspect*HORIZONTAL)-z,
                        Math.abs(dot(p,up))/(tangent*VERTICAL)-z));
            }
            Point camera=focus.add(outward.scale(distance+3.5));
            if(camera.distance(eye)>radius || !withinView(camera,min,max,space.viewDistance()) || !space.clear(eye,camera))continue;
            var shot=new WideShot(camera,focus,min,max,fov);
            if(shot.fits(camera,aspect) && shot.visible(camera,space)
                    && FrameQuality.acceptable(camera,focus.subtract(camera),fov,aspect,space))return shot;
        }
        return null;
    }
    /** Keep every remembered building corner clear of both frame edges and the cinematic bars. */
    public boolean fits(Point at,double aspect){
        Point delta=focus.subtract(at);if(delta.length()<.01)return false;
        Point forward=delta.scale(1/delta.length()),right=right(forward),up=cross(right,forward);
        double tangent=Math.tan(Math.toRadians(fov/2.0));
        for(Point corner:corners(min,max)){
            Point p=corner.subtract(at);double depth=dot(p,forward);
            if(depth<=1 || Math.abs(dot(p,right))>depth*tangent*aspect*HORIZONTAL
                    || Math.abs(dot(p,up))>depth*tangent*VERTICAL)return false;
        }
        return true;
    }
    private boolean visible(Point at,ShotAdvisor.Space base){
        // The centre may be inside solid blocks. Visible surfaces establish visibility, not a new aim.
        Point extent=max.subtract(min).scale(.35);int visible=0;
        for(Point p:new Point[]{focus,focus.add(new Point(extent.x(),0,0)),focus.add(new Point(-extent.x(),0,0)),
                focus.add(new Point(0,extent.y(),0)),focus.add(new Point(0,-extent.y(),0)),
                focus.add(new Point(0,0,extent.z())),focus.add(new Point(0,0,-extent.z()))}){
            if(base.surface(at,p,min,max)!=null)visible++;
        }
        return visible>=4;
    }
    public ShotAdvisor.Space space(ShotAdvisor.Space base,double aspect){
        return new ShotAdvisor.Space(){
            public boolean clear(Point a,Point b){return base.clear(a,b);}
            public boolean visible(Point a,Point b){return fits(a,aspect) && withinView(a,min,max,base.viewDistance()) && WideShot.this.visible(a,base);}
            public boolean unobstructed(Point a,Point b){return base.unobstructed(a,b);}
            public boolean lensClear(Point a,Point b){return base.lensClear(a,b);}
            public boolean framingClear(Point a,Point b,double f,double ratio){return base.framingClear(a,b,f,ratio);}
        };
    }
    private static boolean withinView(Point camera,Point min,Point max,double distance){
        for(Point corner:corners(min,max))if(camera.distance(corner)>distance)return false;
        return true;
    }
    private static Point[] corners(Point min,Point max){
        Point[] result=new Point[8];int i=0;
        for(double x:new double[]{min.x(),max.x()})for(double y:new double[]{min.y(),max.y()})for(double z:new double[]{min.z(),max.z()})result[i++]=new Point(x,y,z);
        return result;
    }
    private static Point right(Point forward){Point p=new Point(-forward.z(),0,forward.x());return p.length()<.001?new Point(1,0,0):p.scale(1/p.length());}
    private static Point cross(Point a,Point b){return new Point(a.y()*b.z()-a.z()*b.y(),a.z()*b.x()-a.x()*b.z(),a.x()*b.y()-a.y()*b.x());}
    private static double dot(Point a,Point b){return a.x()*b.x()+a.y()*b.y()+a.z()*b.z();}
}

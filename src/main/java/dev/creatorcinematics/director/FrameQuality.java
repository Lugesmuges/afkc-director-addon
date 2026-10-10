package dev.creatorcinematics.director;

/** Small lens-proximity check; not a claim to measure artistic quality. */
public final class FrameQuality {
    private FrameQuality(){ }
    public static boolean acceptable(Point camera,Point direction,double verticalFov,double aspect,ShotAdvisor.Space space){
        double length=direction.length();
        if(!Double.isFinite(length) || length<0.001 || !space.clear(camera,camera))return false;
        Point forward=direction.scale(1/length);
        if(!space.framingClear(camera,forward,verticalFov,aspect))return false;
        if(!space.lensClear(camera,camera.add(forward.scale(1.25))))return false;
        Point right=new Point(-forward.z(),0,forward.x());
        right=right.length()<0.001?new Point(1,0,0):right.scale(1/right.length());
        Point up=new Point(right.y()*forward.z()-right.z()*forward.y(),right.z()*forward.x()-right.x()*forward.z(),right.x()*forward.y()-right.y()*forward.x());
        double vertical=Math.tan(Math.toRadians(Math.clamp(verticalFov,30,110)/2)),horizontal=vertical*Math.clamp(aspect,0.5,4);
        int depthRays=space.unobstructed(camera,camera.add(forward.scale(3)))?1:0;
        for(int x:new int[]{-1,1})for(int y:new int[]{-1,1}){
            Point corner=forward.add(right.scale(x*horizontal)).add(up.scale(y*vertical));
            if(!space.lensClear(camera,camera.add(corner.scale(0.7/corner.length()))))return false;
            // Use inner-frame probes for depth: distant side walls are normal in a narrow hall.
            Point inner=forward.add(right.scale(x*horizontal*0.25)).add(up.scale(y*vertical*0.25));
            if(space.unobstructed(camera,camera.add(inner.scale(3/inner.length()))))depthRays++;
        }
        // A sliver of open space at one edge cannot rescue a wall covering most of the image.
        return depthRays>=3;
    }
    /** Reject only a large, visibly cropped foreground figure; whole figures stay allowed. */
    public static boolean croppedActor(Point camera,Point forward,double fov,double aspect,Point min,Point max){
        Point right=new Point(-forward.z(),0,forward.x());
        right=right.length()<0.001?new Point(1,0,0):right.scale(1/right.length());
        Point up=new Point(right.y()*forward.z()-right.z()*forward.y(),right.z()*forward.x()-right.x()*forward.z(),right.x()*forward.y()-right.y()*forward.x());
        double vertical=Math.tan(Math.toRadians(Math.clamp(fov,30,110)/2));
        double horizontal=vertical*Math.clamp(aspect,0.5,4);
        double minX=Double.POSITIVE_INFINITY,minY=minX,maxX=Double.NEGATIVE_INFINITY,maxY=maxX;
        boolean inFront=false;
        for(double x:new double[]{min.x(),max.x()})for(double y:new double[]{min.y(),max.y()})for(double z:new double[]{min.z(),max.z()}){
            Point p=new Point(x,y,z).subtract(camera);
            double depth=dot(p,forward);inFront|=depth>0.05;
            double px=dot(p,right)/(Math.max(0.05,depth)*horizontal),py=dot(p,up)/(Math.max(0.05,depth)*vertical);
            minX=Math.min(minX,px);maxX=Math.max(maxX,px);minY=Math.min(minY,py);maxY=Math.max(maxY,py);
        }
        if(!inFront)return false;
        double width=Math.max(0,Math.min(1,maxX)-Math.max(-1,minX));
        double height=Math.max(0,Math.min(1,maxY)-Math.max(-1,minY));
        return width*height/4>0.08 && (minX<-1 || maxX>1 || minY<-1 || maxY>1);
    }
    private static double dot(Point a,Point b){return a.x()*b.x()+a.y()*b.y()+a.z()*b.z();}
}

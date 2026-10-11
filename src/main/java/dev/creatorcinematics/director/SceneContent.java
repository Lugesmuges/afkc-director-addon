package dev.creatorcinematics.director;
import java.util.function.Predicate;
/** Requires actual scenery in the inner frame, so empty sky cannot become a fallback shot. */
public final class SceneContent {
    private SceneContent(){}
    public static boolean visible(Point direction,double fov,double aspect,Predicate<Point> hit){
        if(direction.length()<.001)return false;
        Point forward=direction.scale(1/direction.length());
        if(hit.test(forward))return true;
        Point right=new Point(-forward.z(),0,forward.x());
        right=right.length()<.001?new Point(1,0,0):right.scale(1/right.length());
        Point up=new Point(-right.z()*forward.y(),right.z()*forward.x()-right.x()*forward.z(),right.x()*forward.y());
        double half=Math.tan(Math.toRadians(fov/2));
        for(int x:new int[]{-1,1})for(int y:new int[]{-1,1}){
            Point directionToProbe=forward.add(right.scale(x*half*aspect*.4)).add(up.scale(y*half*.4));
            if(hit.test(directionToProbe.scale(1/directionToProbe.length())))return true;
        }
        return false;
    }
}

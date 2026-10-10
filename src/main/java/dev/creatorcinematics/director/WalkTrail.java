package dev.creatorcinematics.director;

import java.util.*;

/** Session-local spatial samples. A discontinuity never becomes a flight segment. */
public final class WalkTrail {
    private record Sample(Point point,int segment){ }
    private final List<Sample> points=new ArrayList<>();
    private Point previous;
    private long lastTime;
    private int segment;
    public void sample(Point eye,long time,boolean walking){
        if(!walking){previous=null;return;}
        if(previous==null || time<lastTime || time-lastTime>20 || eye.distance(previous)>2){segment++;previous=null;}
        lastTime=time;
        if(previous!=null && eye.distance(previous)<0.4)return;
        points.add(new Sample(eye,segment));previous=eye;
        if(points.size()>512)points.removeFirst();
    }
    public void clear(){points.clear();previous=null;segment=0;}
    public int size(){return points.size();}
    public RouteGuide route(Point eye,double radius,ShotAdvisor.Space space){
        int nearest=-1;double distance=2;
        for(int i=points.size()-1;i>=0;i--){double d=points.get(i).point.distance(eye);if(d<distance){nearest=i;distance=d;}}
        if(nearest<1)return null;
        Sample first=points.get(nearest);
        if(!space.clear(eye,first.point))return null;
        var route=new ArrayList<Point>();route.add(first.point);double length=0;
        for(int i=nearest-1;i>=0;i--){
            Sample sample=points.get(i);Point last=route.getLast();
            if(sample.segment!=first.segment || sample.point.distance(eye)>radius-1)break;
            double step=last.distance(sample.point);
            Point next=step+length>5?last.lerp(sample.point,(5-length)/step):sample.point;
            if(!space.clear(last,next))break;
            route.add(next);length+=last.distance(next);
            if(length>=4.99)break;
        }
        // Starting inside the player's rendered head is geometrically free but visually unusable.
        double skip=1;
        while(route.size()>1 && skip>0){
            double step=route.getFirst().distance(route.get(1));
            if(step<=skip){route.removeFirst();length-=step;skip-=step;}
            else {route.set(0,route.getFirst().lerp(route.get(1),skip/step));length-=skip;skip=0;}
        }
        if(route.isEmpty() || route.getFirst().distance(eye)<0.9 || !space.clear(eye,route.getFirst()))return null;
        return length>=2?new RouteGuide(route):null;
    }
}

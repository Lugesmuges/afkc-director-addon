package dev.creatorcinematics.director;

import java.util.List;

/** Moving targets only; native AFKC owns interpolation, rotation and duration. */
public final class RouteGuide {
    private final List<Point> points;
    private final double[] distances;
    private double progress;
    public RouteGuide(List<Point> points){
        if(points.size()<2)throw new IllegalArgumentException("A route needs two points");
        this.points=List.copyOf(points);distances=new double[points.size()];
        for(int i=1;i<points.size();i++){
            double step=points.get(i-1).distance(points.get(i));
            if(step<0.0001)throw new IllegalArgumentException("Duplicate route point");
            distances[i]=distances[i-1]+step;
        }
    }
    public Point start(){return points.getFirst();}
    private Point at(double distance){
        for(int i=1;i<points.size();i++)if(distance<=distances[i])
            return points.get(i-1).lerp(points.get(i),(distance-distances[i-1])/(distances[i]-distances[i-1]));
        return points.getLast();
    }
    public ShotAdvisor.Advice next(Point camera,Point eye,double radius,ShotAdvisor.Space space){
        if(camera.distance(eye)>radius || !space.clear(camera,camera))return null;
        double best=Double.POSITIVE_INFINITY,nearest=progress;
        for(int i=1;i<points.size();i++){
            Point a=points.get(i-1),delta=points.get(i).subtract(a),offset=camera.subtract(a);
            double t=Math.clamp((offset.x()*delta.x()+offset.y()*delta.y()+offset.z()*delta.z())/(delta.length()*delta.length()),0,1);
            double along=distances[i-1]+t*(distances[i]-distances[i-1]);
            if(along<progress-0.1 || along>progress+1.5)continue;
            double d=camera.distance(a.add(delta.scale(t)));
            if(d<best){best=d;nearest=along;}
        }
        if(best>0.8)return null;
        progress=Math.max(progress,nearest);
        Point target=null;
        for(double lead=0.9;lead>=0.056;lead*=0.5){
            Point candidate=at(progress+lead);
            if(candidate.distance(eye)<=radius && space.clear(camera,candidate)){target=candidate;break;}
        }
        if(target==null)return null;
        Point focus=null;
        // If the corridor opens up, anticipate its exit instead of staring diagonally at
        // the opposite wall. This extends the gaze only, never the observed camera path.
        Point tangent=points.getLast().subtract(points.get(points.size()-2));
        for(double extension=4;extension>=0.125;extension*=0.5){
            Point candidate=points.getLast().add(tangent.scale(extension/tangent.length()));
            if(camera.distance(candidate)>0.12 && Math.hypot(candidate.x()-eye.x(),candidate.z()-eye.z())<=15.5
                    && space.unobstructed(camera,candidate) && space.unobstructed(target,candidate)){focus=candidate;break;}
        }
        for(double lead=2.2;focus==null && lead>=0.137;lead*=0.5){
            Point candidate=at(progress+lead);
            if(camera.distance(candidate)>0.12 && Math.hypot(candidate.x()-eye.x(),candidate.z()-eye.z())<=15.5
                    && space.unobstructed(camera,candidate) && space.unobstructed(target,candidate)){focus=candidate;break;}
        }
        return focus==null?null:new ShotAdvisor.Advice(target,focus);
    }
}

package dev.creatorcinematics.director;

import java.io.*;
import java.util.*;

/** Local observations, not ownership detection. Times use epoch milliseconds / 50. */
public final class ActivityMemory {
    public enum Kind { PLACE, BREAK, VISIT, AFK }
    private static final long MINUTE=1200, HABIT_HALF_LIFE=14*24*60*MINUTE;
    private final double recentHalfLife;
    public ActivityMemory(){this(10*MINUTE);}
    ActivityMemory(double recentHalfLife){this.recentHalfLife=recentHalfLife;}
    public record Subject(Point focus,double score,Point min,Point max,List<Point> landmarks,boolean built) {
        public double span(){return Math.max(max.x()-min.x(),Math.max(max.y()-min.y(),max.z()-min.z()));}
    }
    private record Cell(int x,int y,int z) {
        static Cell at(Point p){return new Cell((int)Math.floor(p.x()/8),(int)Math.floor(p.y()/8),(int)Math.floor(p.z()/8));}
    }
    private static final class Area {
        Point focus,min,max,stayFocus;
        double building,visits,recentEdits,habits;
        long updated,touched,lastAfk=-10*MINUTE,lastVisit=-20;
        Area(Point p,long time){focus=min=max=stayFocus=p;updated=touched=time;}
        void decay(long time,double halfLife){
            long elapsed=Math.max(0,time-updated);
            double recent=Math.pow(0.5,elapsed/halfLife);
            visits*=recent;recentEdits*=recent;
            habits*=Math.pow(0.5,(double)elapsed/HABIT_HALF_LIFE);
            updated=Math.max(updated,time);
        }
        void include(Point p){min=minimum(min,p);max=maximum(max,p);}
    }
    private final Map<Cell,Area> areas=new LinkedHashMap<>();
    private static Point minimum(Point a,Point b){return new Point(Math.min(a.x(),b.x()),Math.min(a.y(),b.y()),Math.min(a.z(),b.z()));}
    private static Point maximum(Point a,Point b){return new Point(Math.max(a.x(),b.x()),Math.max(a.y(),b.y()),Math.max(a.z(),b.z()));}
    private static double distanceTo(Point p,Point min,Point max) {
        return p.distance(new Point(Math.clamp(p.x(),min.x(),max.x()),Math.clamp(p.y(),min.y(),max.y()),Math.clamp(p.z(),min.z(),max.z())));
    }
    public void record(Point point,Kind kind,long time) {
        if(!valid(point))return;
        Cell key=Cell.at(point);
        if(!areas.containsKey(key) && areas.size()>=2048) {
            Cell oldest=areas.entrySet().stream().min(Comparator.<Map.Entry<Cell,Area>>comparingInt(e->e.getValue().building>0?1:0)
                    .thenComparingLong(e->e.getValue().touched)).orElseThrow().getKey();
            areas.remove(oldest);
        }
        Area area=areas.computeIfAbsent(key,ignored->new Area(point,time));
        area.decay(time,recentHalfLife);
        if(kind==Kind.VISIT) {
            if(time-area.lastVisit<20)return;
            area.lastVisit=time;
            area.visits=Math.min(180,area.visits+1);
            area.stayFocus=point;
            if(area.building==0){area.focus=point;area.include(point);}
        } else if(kind==Kind.AFK) {
            if(time-area.lastAfk<10*MINUTE)return;
            area.lastAfk=time;
            area.habits=Math.min(8,area.habits+1);
            area.stayFocus=point;
            if(area.building==0){area.focus=point;area.include(point);}
        } else {
            if(area.building==0)area.min=area.max=point;
            area.building=Math.min(60,area.building+(kind==Kind.PLACE?6:2));
            area.recentEdits=Math.min(30,area.recentEdits+(kind==Kind.PLACE?1:0.4));
            area.focus=point;area.include(point);
        }
        area.touched=time;
    }
    public List<Subject> ranked(Point origin,double radius,long time) {
        areas.values().removeIf(a->a.building==0 && a.habits<0.01 && time-a.touched>2*24*60*MINUTE);
        var result=new ArrayList<Subject>();
        // A seat inside a large building is still a local motif, not just its distant centroid.
        for(Area a:areas.values()){
            a.decay(time,recentHalfLife);
            double distance=origin.distance(a.stayFocus),score=12*saturate(a.visits,45)+10*saturate(a.habits,2);
            if(a.building>0 && score>=0.3 && distance<=radius)
                result.add(new Subject(a.stayFocus,score/(1+distance/12),a.stayFocus,a.stayFocus,List.of(a.stayFocus),false));
        }
        var remaining=new LinkedHashSet<>(areas.keySet());
        while(!remaining.isEmpty()) {
            Cell seed=remaining.iterator().next();remaining.remove(seed);
            Area first=areas.get(seed);
            var members=new ArrayList<Area>();members.add(first);
            Point min=first.min,max=first.max;
            if(first.building>0) {
                var queue=new ArrayDeque<Cell>();queue.add(seed);
                while(!queue.isEmpty()) {
                    Cell cell=queue.removeFirst();
                    for(int x=-1;x<=1;x++)for(int y=-1;y<=1;y++)for(int z=-1;z<=1;z++) {
                        Cell neighbour=new Cell(cell.x+x,cell.y+y,cell.z+z);
                        if(!remaining.contains(neighbour))continue;
                        Area a=areas.get(neighbour);
                        if(a.building==0)continue;
                        Point nextMin=minimum(min,a.min),nextMax=maximum(max,a.max);
                        if(Math.max(nextMax.x()-nextMin.x(),Math.max(nextMax.y()-nextMin.y(),nextMax.z()-nextMin.z()))>96)continue;
                        remaining.remove(neighbour);queue.add(neighbour);members.add(a);min=nextMin;max=nextMax;
                    }
                }
            }
            double distance=distanceTo(origin,min,max);
            if(distance>radius)continue;
            double building=0,visits=0,edits=0,habits=0;
            for(Area a:members){a.decay(time,recentHalfLife);building+=a.building;visits+=a.visits;edits+=a.recentEdits;habits+=a.habits;}
            // Saturate project evidence: more blocks establish a project, not a monopoly.
            double score=10*saturate(building,24)+12*saturate(visits,45)+8*saturate(edits,8)+10*saturate(habits,2);
            if(score<0.3)continue;
            var landmarks=new ArrayList<Point>();
            // Sample across the extent, not just the latest edit. Bound raycast work per shot.
            members.sort(Comparator.comparingDouble((Area a)->a.focus.x()).thenComparingDouble(a->a.focus.z()).thenComparingDouble(a->a.focus.y()));
            int count=Math.min(12,members.size());
            for(int i=0;i<count;i++)landmarks.add(members.get(count==1?0:i*(members.size()-1)/(count-1)).focus);
            result.add(new Subject(min.lerp(max,0.5),score/(1+distance/12),min,max,List.copyOf(landmarks),first.building>0));
        }
        result.sort(Comparator.comparingDouble(Subject::score).reversed());
        return result;
    }
    private static double saturate(double evidence,double scale){return -Math.expm1(-evidence/scale);}
    private static boolean valid(Point p){return Double.isFinite(p.x()+p.y()+p.z()) && Math.abs(p.x())<=30000000 && Math.abs(p.z())<=30000000 && Math.abs(p.y())<=100000;}
    public int size(){return areas.size();}
    public void clear(){areas.clear();}
    public void write(DataOutput out)throws IOException {
        out.writeInt(0x41464B34);out.writeInt(areas.size());
        for(Area a:areas.values()) {
            writePoint(out,a.focus);writePoint(out,a.min);writePoint(out,a.max);
            out.writeDouble(a.building);out.writeDouble(a.visits);out.writeLong(a.updated);out.writeLong(a.touched);
            out.writeDouble(a.recentEdits);out.writeDouble(a.habits);out.writeLong(a.lastAfk);out.writeLong(a.lastVisit);
            writePoint(out,a.stayFocus);
        }
    }
    public void read(DataInput in)throws IOException {
        int format=in.readInt();
        if(format!=0x41464B32 && format!=0x41464B33 && format!=0x41464B34)throw new IOException("Unsupported director memory format");
        int count=in.readInt();
        if(count<0 || count>2048)throw new IOException("Invalid area count");
        var restored=new LinkedHashMap<Cell,Area>();
        for(int i=0;i<count;i++) {
            Point focus=readPoint(in),min=readPoint(in),max=readPoint(in);
            double building=in.readDouble(),visits=in.readDouble();long updated=in.readLong(),touched=in.readLong();
            double recentEdits=format!=0x41464B32?in.readDouble():0,habits=format!=0x41464B32?in.readDouble():0;
            long lastAfk=format!=0x41464B32?in.readLong():-10*MINUTE,lastVisit=format!=0x41464B32?in.readLong():-20;
            Point stayFocus=format==0x41464B34?readPoint(in):focus;
            if(!valid(focus)||!valid(min)||!valid(max)||!valid(stayFocus)||!Cell.at(stayFocus).equals(Cell.at(focus))||min.x()>max.x()||min.y()>max.y()||min.z()>max.z()
                    ||!Cell.at(focus).equals(Cell.at(min))||!Cell.at(focus).equals(Cell.at(max))
                    ||!Double.isFinite(building+visits+recentEdits+habits)||building<0||building>60||visits<0||visits>(format==0x41464B32?2:180)
                    ||recentEdits<0||recentEdits>30||habits<0||habits>8||lastAfk< -10*MINUTE||lastVisit< -20||updated<0||touched<0)
                throw new IOException("Invalid remembered area");
            Area a=new Area(focus,touched);a.min=min;a.max=max;a.building=building;a.visits=format==0x41464B32?visits*10:visits;a.updated=updated;
            a.recentEdits=recentEdits;a.habits=habits;a.lastAfk=lastAfk;a.lastVisit=lastVisit;
            a.stayFocus=stayFocus;
            restored.put(Cell.at(focus),a);
        }
        areas.clear();areas.putAll(restored);
    }
    private static void writePoint(DataOutput out,Point p)throws IOException {out.writeDouble(p.x());out.writeDouble(p.y());out.writeDouble(p.z());}
    private static Point readPoint(DataInput in)throws IOException {return new Point(in.readDouble(),in.readDouble(),in.readDouble());}
}

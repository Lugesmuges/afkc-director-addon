package dev.creatorcinematics.director;

import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WideShotTest {
    private final Point eye=new Point(0,67,-5),nativeCamera=new Point(-8,69,-9);
    private final ActivityMemory.Subject building=new ActivityMemory.Subject(new Point(0,77,24),10,
            new Point(-24,65,8),new Point(24,89,40),List.of(new Point(0,65,8)),true);
    private final ShotAdvisor.Space open=new ShotAdvisor.Space(){
        public boolean clear(Point a,Point b){return true;}
        public boolean visible(Point a,Point b){return true;}
    };
    @Test void wholeLargeBuildingNeedsARealStandBackShot(){
        var shot=WideShot.plan(building,eye,nativeCamera,open,96,16.0/9,0);
        assertNotNull(shot,"A loaded open world should allow a full view of this 48-block building");
        assertTrue(shot.camera().distance(eye)>16,"Old close-up radius cannot provide this establishing shot");
        assertTrue(shot.camera().distance(eye)<=96);
        checkProjectedCorners(shot,shot.camera(),16.0/9);
    }
    @Test void tallAndNarrowWindowsStillKeepRoofAndGroundInsideTheBars(){
        var tower=new ActivityMemory.Subject(new Point(0,90,24),10,new Point(-8,65,16),new Point(8,115,32),List.of(),true);
        var shot=WideShot.plan(tower,eye,nativeCamera,open,128,4.0/3,1);
        assertNotNull(shot);checkProjectedCorners(shot,shot.camera(),4.0/3);
    }
    @Test void rejectsBlockedUnloadedOrTooShortReachInsteadOfCropping(){
        var blocked=new ShotAdvisor.Space(){public boolean clear(Point a,Point b){return false;}public boolean visible(Point a,Point b){return true;}};
        var hidden=new ShotAdvisor.Space(){public boolean clear(Point a,Point b){return true;}public boolean visible(Point a,Point b){return false;}
            public Point surface(Point a,Point b,Point min,Point max){return null;}};
        assertNull(WideShot.plan(building,eye,nativeCamera,blocked,96,16.0/9,0));
        assertNull(WideShot.plan(building,eye,nativeCamera,hidden,96,16.0/9,0));
        assertNull(WideShot.plan(building,eye,nativeCamera,open,16,16.0/9,0));
    }
    @Test void surfaceVisibilityMustNotReplaceTheBuildingCentreAsAim(){
        var solid=new ShotAdvisor.Space(){public boolean clear(Point a,Point b){return true;}public boolean visible(Point a,Point b){return false;}
            public Point surface(Point a,Point b,Point min,Point max){return new Point(0,77,7);}};
        var shot=WideShot.plan(building,eye,nativeCamera,solid,96,16.0/9,0);
        assertNotNull(shot);assertEquals(building.focus(),shot.focus());checkProjectedCorners(shot,shot.camera(),16.0/9);
    }
    @Test void selectedMovementKeepsTheWholeBuildingInFrame(){
        var shot=WideShot.plan(building,eye,nativeCamera,open,96,16.0/9,0);
        assertNotNull(shot);
        var move=CinematicMove.plan(shot.camera(),shot.focus(),eye,96,2,shot.space(open,16.0/9),shot.fov(),16.0/9);
        assertTrue(move.at(0).camera().distance(move.at(move.durationTicks()).camera())>0.3);
        for(int tick=0;tick<=move.durationTicks();tick++)checkProjectedCorners(shot,move.at(tick).camera(),16.0/9);
    }
    @Test void usesAWiderLensBeforeTheBuildingDisappearsInServerDistanceFog(){
        var shortView=new ShotAdvisor.Space(){
            public boolean clear(Point a,Point b){return true;}
            public boolean visible(Point a,Point b){return true;}
            public double viewDistance(){return 72;}
        };
        var shot=WideShot.plan(building,eye,nativeCamera,shortView,96,16.0/9,0);
        assertNotNull(shot);assertTrue(shot.fov()>30,"Narrow lens puts the building beyond server fog");
        assertNotNull(WideShot.plan(building,new Point(.5,66.62,-5.5),nativeCamera,shortView,96,854.0/480,0),
                "An ordinary off-centre player must not prevent a fitting level establishing shot");
        for(double x:new double[]{shot.min().x(),shot.max().x()})for(double y:new double[]{shot.min().y(),shot.max().y()})for(double z:new double[]{shot.min().z(),shot.max().z()})
            assertTrue(shot.camera().distance(new Point(x,y,z))<=72,"Building extends beyond effective view distance");
    }
    private void checkProjectedCorners(WideShot shot,Point camera,double aspect){
        // Independent projection of every original building corner into normalized screen space.
        Point dir=shot.focus().subtract(camera);dir=dir.scale(1/dir.length());
        Point right=new Point(-dir.z(),0,dir.x());right=right.scale(1/right.length());
        Point up=new Point(-right.z()*dir.y(),right.z()*dir.x()-right.x()*dir.z(),right.x()*dir.y());
        double vertical=Math.tan(Math.toRadians(shot.fov()/2.0));
        for(double x:new double[]{shot.min().x(),shot.max().x()})for(double y:new double[]{shot.min().y(),shot.max().y()})for(double z:new double[]{shot.min().z(),shot.max().z()}){
            Point delta=new Point(x,y,z).subtract(camera);double depth=dot(delta,dir);
            assertTrue(depth>0);
            assertTrue(Math.abs(dot(delta,right)/(depth*vertical*aspect))<0.90,"Building cropped horizontally");
            assertTrue(Math.abs(dot(delta,up)/(depth*vertical))<0.72,"Roof or ground behind cinematic bars");
        }
    }
    private double dot(Point a,Point b){return a.x()*b.x()+a.y()*b.y()+a.z()*b.z();}
}

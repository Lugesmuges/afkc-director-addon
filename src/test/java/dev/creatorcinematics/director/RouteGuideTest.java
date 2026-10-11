package dev.creatorcinematics.director;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class RouteGuideTest {
    @Test void narrowInteriorTravelDoesNotRushAheadOfTheNativeRotation(){
        var guide=new RouteGuide(List.of(new Point(0,65,0),new Point(4,65,0)));
        Point camera=guide.start();
        for(int tick=0;tick<80;tick++){
            var hint=guide.next(camera,guide.start(),16,OPEN);
            Point next=camera.lerp(hint.camera(),.07);
            assertTrue(next.distance(camera)<=.028,"A corridor must not rush past its details");
            camera=next;
        }
        assertTrue(camera.x()>.8);
    }
    @Test void openGalleryKeepsTheGazeDownTheHallInsteadOfOntoTheOppositeWall() {
        var guide=new RouteGuide(List.of(new Point(0,65,0),new Point(0,65,1),new Point(2,65,1)));
        var hint=guide.next(guide.start(),guide.start(),16,OPEN);
        assertNotNull(hint);
        assertTrue(hint.focus().x()>4,"At an open corner the gaze should continue down the hall");
        ShotAdvisor.Space endWall=new ShotAdvisor.Space(){
            public boolean clear(Point a,Point b){return a.x()<2.8 && b.x()<2.8;}
            public boolean visible(Point a,Point b){return clear(a,b);}
        };
        var stopped=guide.next(guide.start(),guide.start(),16,endWall);
        assertNotNull(stopped);
        assertTrue(stopped.focus().x()<2.8,"Never extend the gaze through a closing wall");
    }
    @Test void routeFocusCannotUseEditedBlockVisibilityTolerance() {
        var guide=new RouteGuide(List.of(new Point(0,65,0),new Point(2,65,0)));
        var visibleSurface=new ShotAdvisor.Space(){
            public boolean clear(Point a,Point b){return true;}
            public boolean visible(Point a,Point b){return true;}
            public boolean unobstructed(Point a,Point b){return false;}
        };
        assertNull(guide.next(guide.start(),guide.start(),16,visibleSurface));
    }
    static final ShotAdvisor.Space OPEN=new ShotAdvisor.Space(){
        public boolean clear(Point a,Point b){return true;}
        public boolean visible(Point a,Point b){return true;}
    };
    static final ShotAdvisor.Space CORRIDOR=new ShotAdvisor.Space(){
        public boolean clear(Point a,Point b){
            for(int i=0;i<=100;i++){
                Point p=a.lerp(b,i/100.0);
                if(!(Math.abs(p.z())<=0.48 && p.x()>=-0.48 && p.x()<=2.48
                        || Math.abs(p.x()-2)<=0.48 && p.z()>=-0.48 && p.z()<=2.48))return false;
            }
            return true;
        }
        public boolean visible(Point a,Point b){return clear(a,b);}
    };
    @Test void nativeInterpolationCanFollowBothLegsWithoutCuttingTheCorner() {
        var guide=new RouteGuide(List.of(new Point(0,65,0),new Point(2,65,0),new Point(2,65,2)));
        Point camera=guide.start(),eye=camera;
        for(int tick=0;tick<300;tick++) {
            var hint=guide.next(camera,eye,16,CORRIDOR);
            assertNotNull(hint,"Guide should remain usable at tick "+tick);
            Point next=camera.lerp(hint.camera(),0.07); // native environment-preset blend
            assertTrue(CORRIDOR.clear(camera,next),"Never round through the inside wall");
            assertTrue(CORRIDOR.visible(camera,hint.focus()));
            camera=next;
        }
        assertTrue(camera.x()>1.7 && camera.z()>1,"Shot actually travelled around the corner: "+camera);
    }
    @Test void newlyBlockedOrOutOfRangeRoutesAreRejected() {
        var guide=new RouteGuide(List.of(new Point(0,65,0),new Point(3,65,0)));
        assertNotNull(guide.next(guide.start(),guide.start(),16,OPEN));
        ShotAdvisor.Space blocked=new ShotAdvisor.Space(){public boolean clear(Point a,Point b){return false;}public boolean visible(Point a,Point b){return false;}};
        assertNull(guide.next(guide.start(),guide.start(),16,blocked));
        assertNull(guide.next(guide.start(),new Point(30,65,0),16,OPEN));
    }
    @Test void trailIsBoundedAndCannotConnectAcrossTeleportsOrInactiveGaps() {
        var trail=new WalkTrail();
        for(int i=0;i<800;i++)trail.sample(new Point(i*0.5,65,0),i*5,true);
        assertTrue(trail.size()>0 && trail.size()<=512);
        assertNotNull(trail.route(new Point(399.5,65,0),16,OPEN));
        trail.sample(new Point(500,65,0),4000,true);
        assertNull(trail.route(new Point(500,65,0),16,OPEN));
        trail.sample(new Point(500.5,65,0),4005,false);
        trail.sample(new Point(501,65,0),4010,true);
        assertNull(trail.route(new Point(501,65,0),16,OPEN));
        trail.clear();assertEquals(0,trail.size());
    }
    @Test void trailRouteRejectsChangedGeometry() {
        var trail=new WalkTrail();
        for(int i=0;i<=10;i++)trail.sample(new Point(i*0.5,65,0),i*5,true);
        ShotAdvisor.Space wall=new ShotAdvisor.Space(){public boolean clear(Point a,Point b){return a.x()>=4&&b.x()>=4;}public boolean visible(Point a,Point b){return clear(a,b);}};
        assertNull(trail.route(new Point(5,65,0),16,wall),"One short safe metre is insufficient for a shot");
    }
}

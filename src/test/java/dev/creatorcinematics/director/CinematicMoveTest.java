package dev.creatorcinematics.director;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CinematicMoveTest {
    private final Point camera=new Point(0,66,-8),focus=new Point(0,66,0),eye=new Point(0,66,0);
    private final ShotAdvisor.Space open=new ShotAdvisor.Space(){
        public boolean clear(Point a,Point b){return true;}
        public boolean visible(Point a,Point b){return true;}
    };
    private CinematicMove plan(int style,ShotAdvisor.Space space){
        return CinematicMove.plan(camera,focus,eye,16,style,space,30,16.0/9);
    }
    @Test void shotsKeepTravellingAfterFourteenSecondsWithoutAbruptAcceleration(){
        for(int style=0;style<10;style++){
            var move=plan(style,open);
            assertTrue(move.at(300).camera().distance(move.at(280).camera())>0.001,
                    "Long shots must still move after fourteen seconds");
            assertTrue(move.durationTicks()>=360 && move.durationTicks()<=520);
        }
    }
    @Test void descendingAndDiagonalMovesHaveDistinctVerticalTravel(){
        assertTrue(plan(5,open).at(10000).camera().y()<camera.y());
        for(int style=6;style<10;style++){
            Point end=plan(style,open).at(10000).camera();
            assertTrue(Math.abs(end.x()-camera.x())>.1,"Diagonal shot needs sideways travel");
            assertTrue(Math.abs(end.y()-camera.y())>.1,"Diagonal shot needs vertical travel");
        }
    }
    @Test void dollyInApproachesOneFixedSubjectWithoutReversingOrSwaying(){
        var move=plan(0,open);double lastDistance=8;
        for(int tick=0;tick<=move.durationTicks();tick++){
            var pose=move.at(tick);
            assertEquals(focus,pose.focus());
            assertEquals(0,pose.camera().x(),1e-9);assertEquals(66,pose.camera().y(),1e-9);
            assertTrue(pose.camera().distance(focus)<=lastDistance+1e-9);
            lastDistance=pose.camera().distance(focus);
        }
        assertTrue(lastDistance<7,"The camera must actually travel toward the subject");
    }
    @Test void dollyOutRevealsMoreOfTheSceneWithoutChangingTheFocus(){
        var move=plan(1,open);double previous=8;
        for(int tick=0;tick<=move.durationTicks();tick++){
            var pose=move.at(tick);assertEquals(focus,pose.focus());
            assertTrue(pose.camera().distance(focus)>=previous-1e-9);
            previous=pose.camera().distance(focus);
        }
        assertTrue(previous>9);
    }
    @Test void trackingAndCraneMovesAreDistinctAndUseOneContinuousDirection(){
        var left=plan(2,open);var right=plan(3,open);var crane=plan(4,open);
        assertTrue(left.at(10000).camera().x()<0);assertTrue(right.at(10000).camera().x()>0);
        assertEquals(-8,left.at(10000).camera().z(),1e-9);
        assertTrue(crane.at(10000).camera().y()>66);
        assertEquals(0,crane.at(10000).camera().x(),1e-9);
    }
    @Test void travelStartsAndEndsGentlyWithBoundedSpeedAndNoOvershoot(){
        var move=plan(2,open);int duration=move.durationTicks();
        double first=move.at(1).camera().distance(move.at(0).camera());
        double middle=move.at(duration/2).camera().distance(move.at(duration/2-1).camera());
        double last=move.at(duration).camera().distance(move.at(duration-1).camera());
        assertTrue(first<middle*0.01);assertTrue(last<middle*0.01);
        for(int t=1;t<=duration;t++)assertTrue(move.at(t).camera().distance(move.at(t-1).camera())<0.03);
        assertEquals(camera,move.at(-100).camera());
        assertEquals(move.at(duration),move.at(duration+100));
    }
    @Test void entireTravelMustStayWithinRangeAndClearOfAnInterveningObstacle(){
        var blocked=new ShotAdvisor.Space(){
            public boolean clear(Point a,Point b){return a.distance(b)<1e-8;}
            public boolean visible(Point a,Point b){return true;}
            public boolean lensClear(Point a,Point b){return true;}
            public boolean unobstructed(Point a,Point b){return true;}
        };
        var hold=plan(0,blocked);
        assertEquals(camera,hold.at(10000).camera(),"A blocked move must hold instead of clipping or oscillating");
        for(int style=0;style<5;style++){
            var limited=CinematicMove.plan(camera,focus,eye,8,style,open,30,16.0/9);
            for(int t=0;t<=limited.durationTicks();t++)assertTrue(limited.at(t).camera().distance(eye)<=8.000001);
        }
    }
    @Test void aClearEndpointCannotHideAnOccludedMiddleOfTheTrack(){
        var hidden=new ShotAdvisor.Space(){
            public boolean clear(Point a,Point b){return true;}
            public boolean visible(Point a,Point b){return a.distance(camera)<0.1;}
        };
        var hold=plan(0,hidden);
        assertEquals(camera,hold.at(10000).camera());
    }
}

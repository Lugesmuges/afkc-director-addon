package dev.creatorcinematics.director;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class FrameQualityTest {
    private static ShotAdvisor.Space wall(double z){return new ShotAdvisor.Space(){
        public boolean clear(Point a,Point b){return a.z()<z&&b.z()<z;}
        public boolean visible(Point a,Point b){return true;} // edited-surface tolerance is deliberately unsuitable here
    };}
    @Test void rejectsAFrontWallDespiteAVisibleSubjectSurface() {
        assertFalse(FrameQuality.acceptable(new Point(0,65,0),new Point(0,0,1),30,16.0/9,wall(0.8)));
    }
    @Test void evaluatesActualLookDirectionAndAllowsClearDetailFraming() {
        assertTrue(FrameQuality.acceptable(new Point(0,65,0),new Point(0,0,-1),30,16.0/9,wall(0.8)));
        assertTrue(FrameQuality.acceptable(new Point(0,65,0),new Point(0,0,1),30,16.0/9,wall(4)));
    }
    @Test void flatWallFillingTheWholeNearFrameIsNotAUsefulShot() {
        assertFalse(FrameQuality.acceptable(new Point(0,65,0),new Point(0,0,1),30,16.0/9,wall(2)));
    }
    @Test void aSmallOpeningAtOneEdgeDoesNotRescueAWallFillingTheImage() {
        var sliver=new ShotAdvisor.Space(){
            public boolean clear(Point a,Point b){return b.z()<2 || b.x()<-0.8 && b.y()>65.3;}
            public boolean visible(Point a,Point b){return true;}
        };
        assertFalse(FrameQuality.acceptable(new Point(0,65,0),new Point(0,0,1),30,16.0/9,sliver));
    }
    @Test void openNarrowCorridorHasDepthDespiteItsSideWalls() {
        var corridor=new ShotAdvisor.Space(){
            public boolean clear(Point a,Point b){return Math.abs(b.x())<0.5 && Math.abs(b.y()-65)<1;}
            public boolean visible(Point a,Point b){return true;}
        };
        assertTrue(FrameQuality.acceptable(new Point(0,65,0),new Point(0,0,1),30,16.0/9,corridor));
    }
    @Test void aPlayerOrMobOverTheLensCannotCountAsClearBlockSpace() {
        var body=new ShotAdvisor.Space(){
            public boolean clear(Point a,Point b){return true;}
            public boolean visible(Point a,Point b){return true;}
            public boolean lensClear(Point a,Point b){return false;}
        };
        assertFalse(FrameQuality.acceptable(new Point(0,65,0),new Point(0,0,1),30,16.0/9,body));
    }
    @Test void rejectsAPostAtTheEdgeOfTheLens() {
        var edge=new ShotAdvisor.Space(){
            public boolean clear(Point a,Point b){return b.x()<0.15;}
            public boolean visible(Point a,Point b){return true;}
        };
        assertFalse(FrameQuality.acceptable(new Point(0,65,0),new Point(0,0,1),30,16.0/9,edge));
    }
}

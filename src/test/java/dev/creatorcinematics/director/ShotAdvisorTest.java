package dev.creatorcinematics.director;
import java.util.Random;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class ShotAdvisorTest {
    private final Point eye=new Point(0,66,0),nativeCamera=new Point(-8,68,-4);
    private final ShotAdvisor.Space open=new ShotAdvisor.Space(){
        public boolean clear(Point from,Point to){return true;}
        public boolean visible(Point from,Point to){return true;}
    };
    private ActivityMemory.Subject house() {
        var memory=new ActivityMemory();
        for(int x=0;x<=16;x+=4)for(int z=4;z<=12;z+=4)memory.record(new Point(x,66,z),ActivityMemory.Kind.PLACE,0);
        return memory.ranked(eye,24,288000).getFirst();
    }
    @Test void givesFramingAdviceWhileRetainingNativePresetDistanceAndHeight() {
        var advice=new ShotAdvisor(new Random(1)).advise(eye,nativeCamera,house(),null,open,16);
        assertNotNull(advice);
        assertEquals(nativeCamera,advice.camera());
        assertEquals(new Point(8,66,8),advice.focus());
    }
    @Test void variesSubjectAndAngleInsteadOfAlwaysShowingLastPlacedBlock() {
        var advisor=new ShotAdvisor(new Random(4));var subject=house();
        var wide=advisor.advise(eye,nativeCamera,subject,null,open,16);
        var detail=advisor.advise(eye,nativeCamera,subject,null,open,16);
        assertNotNull(wide);assertNotNull(detail);
        assertNotEquals(wide.focus(),detail.focus());
        assertTrue(wide.camera().distance(detail.camera())>2);
        assertEquals(nativeCamera.y(),detail.camera().y(),0.01);
        assertEquals(eye.distance(nativeCamera),eye.distance(detail.camera()),0.01);
    }
    @Test void contextualViewIsPartOfVariedSequenceRatherThanLockedView() {
        var advisor=new ShotAdvisor(new Random(5));var subject=house();var context=new Point(2,66,10);
        var first=advisor.advise(eye,nativeCamera,subject,context,open,16);
        advisor.advise(eye,nativeCamera,subject,context,open,16);
        var third=advisor.advise(eye,nativeCamera,subject,context,open,16);
        assertNotNull(first);assertNotNull(third);
        assertNotEquals(context,first.focus());
        assertEquals(context,third.focus());
    }
    @Test void refusesOccludedOrInaccessibleFraming() {
        var blocked=new ShotAdvisor.Space(){
            public boolean clear(Point a,Point b){return false;}
            public boolean visible(Point a,Point b){return true;}
        };
        assertNull(new ShotAdvisor(new Random(1)).advise(eye,nativeCamera,house(),null,blocked,16));
        var hidden=new ShotAdvisor.Space(){
            public boolean clear(Point a,Point b){return true;}
            public boolean visible(Point a,Point b){return false;}
        };
        assertNull(new ShotAdvisor(new Random(1)).advise(eye,nativeCamera,house(),null,hidden,16));
    }
    @Test void distantFocusCannotDriftWhenOriginalAppliesItsFocusClamp() {
        var point=new Point(23,66,0);
        var subject=new ActivityMemory.Subject(point,10,point,point,java.util.List.of(point),true);
        assertNull(new ShotAdvisor(new Random(1)).advise(eye,nativeCamera,subject,null,open,16),
                "The native 16-block focus clamp would move this accepted target away from the building");
    }
}

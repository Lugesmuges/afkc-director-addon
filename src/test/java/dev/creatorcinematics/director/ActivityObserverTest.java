package dev.creatorcinematics.director;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ActivityObserverTest {
    @Test void walkingThroughDoesNotBecomeAStay() {
        var memory=new ActivityMemory();var observer=new ActivityObserver();
        for(int second=0;second<40;second++)observer.sample(memory,new Point(second*4,65,0),second*20,true);
        assertEquals(0,memory.size());
    }
    @Test void stationaryPreAfkStayQualifiesWithoutRepeatedKeyboardInput() {
        var memory=new ActivityMemory();var observer=new ActivityObserver();
        for(int second=0;second<=24;second++)observer.sample(memory,new Point(0,65,0),second*20,true);
        assertFalse(memory.ranked(new Point(0,65,0),24,480).isEmpty());
        double before=memory.ranked(new Point(0,65,0),24,480).getFirst().score();
        for(int second=25;second<3600;second++)observer.sample(memory,new Point(0,65,0),second*20,false);
        assertTrue(memory.ranked(new Point(0,65,0),24,480).getFirst().score()<=before);
    }
    @Test void afkEntryIsBoundedButReturnVisitsAccumulate() {
        var memory=new ActivityMemory();Point p=new Point(0,65,0);
        memory.record(p,ActivityMemory.Kind.AFK,0);
        double once=memory.ranked(p,24,0).getFirst().score();
        for(int i=1;i<100;i++)memory.record(p,ActivityMemory.Kind.AFK,i);
        assertEquals(once,memory.ranked(p,24,100).getFirst().score(),0.001);
        memory.record(p,ActivityMemory.Kind.AFK,12000);
        assertTrue(memory.ranked(p,24,12000).getFirst().score()>once);
    }
    @Test void returningFromPauseRequiresANewStay() {
        var memory=new ActivityMemory();var observer=new ActivityObserver();Point p=new Point(0,65,0);
        observer.sample(memory,p,0,true);
        observer.sample(memory,p,400,true);
        assertEquals(0,memory.size());
        observer.sample(memory,p,420,false);
        observer.sample(memory,p,600,true);
        assertEquals(0,memory.size());
    }
}

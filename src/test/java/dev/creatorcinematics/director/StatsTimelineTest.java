package dev.creatorcinematics.director;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class StatsTimelineTest {
    @Test void delaysFromActualStartAndFadesWithoutTickDependence() {
        var timeline=new StatsTimeline();
        timeline.start(1_000_000_000L);
        assertFalse(timeline.frame(6_999_000_000L,3).visible());
        var enter=timeline.frame(7_300_000_000L,3);
        assertEquals(0,enter.index());
        assertTrue(enter.alpha()>0 && enter.alpha()<1);
        assertTrue(enter.offsetY()>0 && enter.offsetY()<6);
        assertEquals(1,timeline.frame(8_000_000_000L,3).alpha(),1e-6);
        assertFalse(timeline.frame(14_500_000_000L,3).visible());
        assertEquals(1,timeline.frame(15_400_000_000L,3).index());
        assertEquals(1,timeline.frame(15_400_000_000L,3).alpha(),1e-6);
    }
    @Test void noDataStopAndRestartNeverLeakPreviousCard() {
        var timeline=new StatsTimeline();
        timeline.start(0);
        assertFalse(timeline.frame(8_000_000_000L,0).visible());
        assertTrue(timeline.frame(8_000_000_000L,2).visible());
        timeline.stop();
        assertFalse(timeline.frame(9_000_000_000L,2).visible());
        timeline.start(10_000_000_000L);
        assertFalse(timeline.frame(11_000_000_000L,2).visible());
        assertEquals(0,timeline.frame(17_000_000_000L,2).index());
    }
}

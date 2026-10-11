package dev.creatorcinematics.director;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ShotTransitionTest {
    @Test void cutWaitsForFullCoverageThenRevealsTheNewViewContinuously(){
        var fade=new ShotTransition();
        assertFalse(fade.ready(0));assertEquals(0,fade.alpha(0));
        assertFalse(fade.ready(300_000_000));
        assertEquals(.5,fade.alpha(300_000_000),.01);
        assertTrue(fade.ready(600_000_000));assertEquals(1,fade.alpha(600_000_000));
        fade.complete(650_000_000);
        assertEquals(1,fade.alpha(650_000_000));
        assertEquals(.5,fade.alpha(1_150_000_000),.01);
        assertEquals(0,fade.alpha(1_650_000_000));
    }
    @Test void stopRemovesAnInProgressFadeImmediately(){
        var fade=new ShotTransition();fade.ready(0);
        assertTrue(fade.alpha(400_000_000)>0);fade.reset();
        assertEquals(0,fade.alpha(400_000_000));
        assertFalse(fade.ready(2_000_000_000));
        assertEquals(0,fade.alpha(2_000_000_000));
    }
    @Test void rejectedCutRetriesCannotInterruptTheRevealAndFlashTheScreen(){
        var fade=new ShotTransition();fade.ready(0);fade.complete(600_000_000);
        double before=fade.alpha(900_000_000);
        assertFalse(fade.ready(900_000_000));
        assertEquals(before,fade.alpha(900_000_000),1e-9);
    }
}

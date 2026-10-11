package dev.creatorcinematics.director;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ActorEdgeTest {
    @Test void halfHeightFigureClippedAtTheSideIsNotAUsableDetailShot(){
        assertTrue(FrameQuality.croppedActor(new Point(0,0,0),new Point(0,0,1),30,16.0/9,
                new Point(2.75,-.9,5.7),new Point(3.35,.9,6.3)));
        assertFalse(FrameQuality.croppedActor(new Point(0,0,0),new Point(0,0,1),30,16.0/9,
                new Point(-.3,-.9,5.7),new Point(.3,.9,6.3)),"A fully framed player stays allowed");
    }
}

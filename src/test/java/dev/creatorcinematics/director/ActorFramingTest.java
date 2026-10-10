package dev.creatorcinematics.director;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ActorFramingTest {
    private boolean cropped(Point min,Point max){
        return FrameQuality.croppedActor(new Point(0,1.6,0),new Point(0,0,1),37,16.0/9,min,max);
    }
    @Test void rejectsLargeCroppedForegroundBody(){
        assertTrue(cropped(new Point(-0.3,0,2),new Point(0.3,1.8,2.6)));
        assertTrue(cropped(new Point(-1,0,2),new Point(-0.4,1.8,2.6)));
    }
    @Test void retainsWholeFigureAndDistantContext(){
        assertFalse(cropped(new Point(-0.3,0,6),new Point(0.3,1.8,6.6)));
        assertFalse(cropped(new Point(5,0,12),new Point(5.6,1.8,12.6)));
    }
    @Test void ignoresActorsBehindTheCameraOrOutsideTheImage(){
        assertFalse(cropped(new Point(-0.3,0,-3),new Point(0.3,1.8,-2.4)));
        assertFalse(cropped(new Point(4,0,2),new Point(4.6,1.8,2.6)));
    }
    @Test void nearPlaneIntersectionCannotProduceAnInfiniteOrEmptyFrameTest(){
        assertTrue(cropped(new Point(-0.3,0,-0.1),new Point(0.3,1.8,0.5)));
    }
}

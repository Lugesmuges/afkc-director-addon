package dev.creatorcinematics.director;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CharacterShotTest {
    private final Point eye=new Point(0,66,0),facing=new Point(0,0,1);
    private final ShotAdvisor.Space open=new ShotAdvisor.Space(){
        public boolean clear(Point a,Point b){return true;}
        public boolean visible(Point a,Point b){return true;}
    };
    @Test void successiveCharacterShotsShowFrontSideAndBackWithStableChestFraming(){
        var front=CharacterShot.plan(eye,facing,0,16,open,false,16.0/9);
        var side=CharacterShot.plan(eye,facing,1,16,open,false,16.0/9);
        var back=CharacterShot.plan(eye,facing,2,16,open,false,16.0/9);
        assertNotNull(front);assertNotNull(side);assertNotNull(back);
        assertTrue(front.camera().z()>4);assertTrue(Math.abs(side.camera().x())>4);
        assertTrue(back.camera().z()<-4);
        for(var shot:new ShotAdvisor.Advice[]{front,side,back}){
            assertEquals(new Point(0,65.35,0),shot.focus());
            assertTrue(shot.camera().distance(eye)<=16);
        }
    }
    @Test void blockedRoomsCannotBecomeCharacterShots(){
        var wall=new ShotAdvisor.Space(){
            public boolean clear(Point a,Point b){return false;}
            public boolean visible(Point a,Point b){return true;}
        };
        assertNull(CharacterShot.plan(eye,facing,0,16,wall,false,16.0/9));
        assertNull(CharacterShot.plan(eye,facing,0,16,open,true,16.0/9),"Open air is not a window shot");
    }
}

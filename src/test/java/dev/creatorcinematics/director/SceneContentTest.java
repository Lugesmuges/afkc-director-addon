package dev.creatorcinematics.director;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class SceneContentTest {
    @Test void emptySkyMustNotPassAsScenery(){assertFalse(SceneContent.visible(new Point(0,1,0),30,16.0/9,ray->false));}
    @Test void centralBuildingIsEnoughWithoutFillingTheCorners(){assertTrue(SceneContent.visible(new Point(0,0,1),30,16.0/9,ray->Math.abs(ray.x())<.01 && Math.abs(ray.y())<.01));}
    @Test void aLandscapeBelowTheHorizonIsAValidSubject(){assertTrue(SceneContent.visible(new Point(0,0,1),45,16.0/9,ray->ray.y()<-.05));}
}

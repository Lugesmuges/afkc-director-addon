package dev.creatorcinematics.director;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ActivityMemoryTest {
    @Test void cozyPlaceInsideLargeBuildRemainsALocalSubject() {
        var memory=new ActivityMemory();
        for(int x=0;x<=64;x+=4)memory.record(new Point(x,65,0),ActivityMemory.Kind.PLACE,0);
        Point seat=new Point(1,66,1);
        memory.record(seat,ActivityMemory.Kind.AFK,72000);
        var subjects=memory.ranked(seat,24,72000);
        assertTrue(subjects.stream().anyMatch(s->!s.built() && s.focus().distance(seat)<0.1),
                "A known seat must not disappear into the distant centroid of its surrounding project");
    }
    @Test void halfLifeSensitivityKeepsFreshActivityAndDurableBuilds() {
        for(int minutes:new int[]{5,10,20}) {
            var memory=new ActivityMemory(minutes*1200.0);
            Point p=new Point(0,65,0);
            for(int i=0;i<90;i++)memory.record(p,ActivityMemory.Kind.VISIT,i*20);
            memory.record(new Point(20,65,0),ActivityMemory.Kind.PLACE,0);
            assertFalse(memory.ranked(p,24,1800).getFirst().built(),"Current stay, half-life "+minutes);
            double recent=memory.ranked(p,24,1800).getFirst().score();
            double later=memory.ranked(p,24,1800+1200*minutes).stream().filter(s->!s.built()).findFirst().orElseThrow().score();
            assertTrue(later<recent && later>0,"Smooth fade without an abrupt deadline");
            assertTrue(memory.ranked(p,24,1800+1200*120).getFirst().built(),"After two hours the durable build remains");
        }
    }
    @Test void currentStayCanOutrankAnOldLargeNeighbouringProject() {
        var memory=new ActivityMemory();
        for(int x=16;x<64;x+=4)for(int z=-16;z<=16;z+=4)
            for(int n=0;n<10;n++)memory.record(new Point(x,64,z),ActivityMemory.Kind.PLACE,0);
        long now=20L*60*60*24;
        for(int i=0;i<90;i++)memory.record(new Point(0,65,0),ActivityMemory.Kind.VISIT,now+i*20);
        assertFalse(memory.ranked(new Point(0,65,0),24,now+1800).getFirst().built(),
                "A huge old build must not always beat the place currently occupied");
    }
    @Test void freshWorkWinsBetweenOtherwiseEquivalentProjects() {
        var memory=new ActivityMemory();
        memory.record(new Point(-16,64,0),ActivityMemory.Kind.PLACE,0);
        memory.record(new Point(16,64,0),ActivityMemory.Kind.PLACE,20*60*60);
        assertEquals(16,memory.ranked(new Point(0,64,0),24,20*60*60).getFirst().focus().x());
        assertEquals(2,memory.ranked(new Point(0,64,0),24,20*60*60*24).size(),
                "Recency may fade; the builds themselves must remain");
    }
    @Test void constructionOutweighsRepeatedVisits() {
        var memory = new ActivityMemory();
        for (int i = 0; i < 60; i++) memory.record(new Point(2, 64, 2), ActivityMemory.Kind.VISIT, i);
        for (int i = 0; i < 5; i++) memory.record(new Point(10, 64, 2), ActivityMemory.Kind.PLACE, 60 + i);
        assertTrue(memory.ranked(new Point(0,64,0), 24, 65).getFirst().focus().x() > 8);
    }
    @Test void finishedBuildRemainsAMotifAfterHoursButOnlyWhenNearby() {
        var memory = new ActivityMemory();
        memory.record(new Point(2,64,2), ActivityMemory.Kind.PLACE, 0);
        memory.record(new Point(200,64,2), ActivityMemory.Kind.PLACE, 1);
        assertEquals(1, memory.ranked(new Point(0,64,0), 24, 100).size());
        assertEquals(1,memory.ranked(new Point(0,64,0), 24, 288001).size(),"A four-hour-old build must remain a subject");
        assertTrue(memory.ranked(new Point(100,64,0),24,288001).isEmpty());
    }
    @Test void resetAndBoundedStorage() {
        var memory = new ActivityMemory();
        for (int i = 0; i < 3000; i++) memory.record(new Point(i*8,64,0), ActivityMemory.Kind.PLACE, i);
        assertTrue(memory.size() <= 2048);
        memory.clear();
        assertEquals(0, memory.size());
    }
    @Test void negativeCoordinatesStayInTheirOwnArea() {
        var memory = new ActivityMemory();
        memory.record(new Point(-1,64,0), ActivityMemory.Kind.PLACE, 0);
        memory.record(new Point(1,64,0), ActivityMemory.Kind.PLACE, 0);
        assertEquals(2, memory.size());
    }

    @Test void completedHouseIsFramedAcrossItsExtentInsteadOfLastBlock() {
        var memory=new ActivityMemory();
        for(int x=0;x<=24;x+=4) for(int z=0;z<=16;z+=4)
            memory.record(new Point(x,64,z),ActivityMemory.Kind.PLACE,x+z);
        var subjects=memory.ranked(new Point(12,65,-4),32,288000);
        assertEquals(1,subjects.size(),"Adjacent construction should be one project");
        assertEquals(12,subjects.getFirst().focus().x(),0.1);
        assertEquals(8,subjects.getFirst().focus().z(),0.1);
    }
}

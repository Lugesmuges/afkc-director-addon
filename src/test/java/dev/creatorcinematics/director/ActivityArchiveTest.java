package dev.creatorcinematics.director;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.*;

class ActivityArchiveTest {
    @TempDir Path directory;
    @Test void alphaTwoArchiveMigratesWithoutLosingABuild()throws Exception {
        var bytes=new java.io.ByteArrayOutputStream();var out=new java.io.DataOutputStream(bytes);
        out.writeInt(0x41464B32);out.writeInt(1);
        for(int i=0;i<3;i++){out.writeDouble(1);out.writeDouble(65);out.writeDouble(2);}
        out.writeDouble(30);out.writeDouble(1);out.writeLong(100);out.writeLong(100);
        var memory=new ActivityMemory();memory.read(new java.io.DataInputStream(new java.io.ByteArrayInputStream(bytes.toByteArray())));
        assertTrue(memory.ranked(new Point(0,65,0),24,72000).getFirst().built());
        new ActivityArchive(directory).save("world","overworld",memory);
        assertEquals(new Point(1,65,2),new ActivityArchive(directory).load("world","overworld").ranked(new Point(0,65,0),24,72000).getFirst().focus());
    }
    @Test void habitAndFullCapacitySurviveRestart()throws Exception {
        var memory=new ActivityMemory();
        for(int i=0;i<2048;i++)memory.record(new Point(i*8,65,0),ActivityMemory.Kind.AFK,0);
        var archive=new ActivityArchive(directory);archive.save("world","overworld",memory);
        var restored=archive.load("world","overworld");assertEquals(2048,restored.size());
        assertFalse(restored.ranked(new Point(0,65,0),24,1200*60*24).isEmpty());
    }
    @Test void projectSurvivesRestartAndCannotLeakToOtherServerOrDimension()throws Exception {
        var memory=new ActivityMemory();
        for(int x=0;x<=24;x+=4)memory.record(new Point(x,64,0),ActivityMemory.Kind.PLACE,x);
        new ActivityArchive(directory).save("server-a:25565","overworld",memory);
        var archive=new ActivityArchive(directory);
        var restored=archive.load("server-a:25565","overworld").ranked(new Point(12,65,0),24,288000);
        assertEquals(1,restored.size());
        assertEquals(24,restored.getFirst().span(),0.01);
        assertTrue(archive.load("server-b:25565","overworld").ranked(new Point(12,65,0),24,288000).isEmpty());
        assertEquals(0,archive.load("server-a:25565","nether").size());
    }
    @Test void clearingReplacesSavedMemoryRatherThanResurrectingIt()throws Exception {
        var archive=new ActivityArchive(directory);
        var memory=new ActivityMemory();
        memory.record(new Point(0,64,0),ActivityMemory.Kind.PLACE,0);
        archive.save("world","overworld",memory);
        assertEquals(1,archive.load("world","overworld").size());
        memory.clear();archive.save("world","overworld",memory);
        assertEquals(0,archive.load("world","overworld").size());
    }
    @Test void corruptArchiveIsRejectedWithoutPartiallyReplacingGoodMemory()throws Exception {
        var memory=new ActivityMemory();
        memory.record(new Point(1,64,1),ActivityMemory.Kind.PLACE,0);
        var bytes=new java.io.ByteArrayOutputStream();
        memory.write(new java.io.DataOutputStream(bytes));
        byte[] truncated=java.util.Arrays.copyOf(bytes.toByteArray(),bytes.size()-3);
        assertThrows(java.io.IOException.class,()->memory.read(new java.io.DataInputStream(new java.io.ByteArrayInputStream(truncated))));
        assertEquals(1,memory.ranked(new Point(0,64,0),24,100).size());
    }
}

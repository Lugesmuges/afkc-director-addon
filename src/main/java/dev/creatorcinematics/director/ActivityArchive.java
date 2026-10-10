package dev.creatorcinematics.director;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.*;
import java.util.HexFormat;
public final class ActivityArchive {
    private final Path directory;
    public ActivityArchive(Path directory) {this.directory=directory;}
    private Path file(String world,String dimension) {
        try {
            byte[] hash=MessageDigest.getInstance("SHA-256").digest((world+"\0"+dimension).getBytes(StandardCharsets.UTF_8));
            return directory.resolve(HexFormat.of().formatHex(hash)+".dat");
        } catch(NoSuchAlgorithmException e){throw new IllegalStateException(e);}
    }
    public void save(String world,String dimension,ActivityMemory memory)throws IOException {
        Files.createDirectories(directory);
        Path file=file(world,dimension),temporary=Files.createTempFile(directory,"director-",".tmp");
        try {
            try(var out=new DataOutputStream(new BufferedOutputStream(Files.newOutputStream(temporary)))){memory.write(out);}
            try {Files.move(temporary,file,StandardCopyOption.ATOMIC_MOVE,StandardCopyOption.REPLACE_EXISTING);}
            catch(AtomicMoveNotSupportedException e){Files.move(temporary,file,StandardCopyOption.REPLACE_EXISTING);}
        } finally {Files.deleteIfExists(temporary);}
    }
    public ActivityMemory load(String world,String dimension)throws IOException {
        var memory=new ActivityMemory();Path file=file(world,dimension);
        if(!Files.isRegularFile(file))return memory;
        if(Files.size(file)>400000)throw new IOException("Director memory file exceeds limit");
        try(var in=new DataInputStream(new BufferedInputStream(Files.newInputStream(file)))){memory.read(in);}
        return memory;
    }
}

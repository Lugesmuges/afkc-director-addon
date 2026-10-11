package dev.creatorcinematics.director;

import net.fabricmc.loader.api.FabricLoader;
import java.io.*;
import java.nio.file.*;
import java.util.Properties;

public final class DirectorConfig {
    public boolean enabled=true;
    public int radius=16;
    public boolean wideShots=true;
    public int wideRadius=96;
    public boolean statsEnabled=true;
    public int webStatsMode=0; // 0 automatic CraftAttack server detection, 1 on, 2 off
    private final Path file=FabricLoader.getInstance().getConfigDir().resolve("afkc-director.properties");
    public void load() {
        if(!Files.isRegularFile(file)) return;
        var values=new Properties();
        try(var reader=Files.newBufferedReader(file)) {
            values.load(reader);
            enabled=Boolean.parseBoolean(values.getProperty("enabled","true"));
            radius=Math.clamp(Integer.parseInt(values.getProperty("radius","16")),8,16);
            wideShots=Boolean.parseBoolean(values.getProperty("wideShots","true"));
            wideRadius=Math.clamp(Integer.parseInt(values.getProperty("wideRadius","96")),32,128);
            statsEnabled=Boolean.parseBoolean(values.getProperty("statsEnabled","true"));
            webStatsMode=Math.clamp(Integer.parseInt(values.getProperty("webStatsMode","0")),0,2);
        } catch(IOException|IllegalArgumentException e) { DirectorClient.LOG.warn("Cannot read director config; using remaining defaults",e); }
    }
    public void save() {
        var values=new Properties();
        values.setProperty("enabled",Boolean.toString(enabled));
        values.setProperty("radius",Integer.toString(radius));
        values.setProperty("wideShots",Boolean.toString(wideShots));
        values.setProperty("wideRadius",Integer.toString(wideRadius));
        values.setProperty("statsEnabled",Boolean.toString(statsEnabled));
        values.setProperty("webStatsMode",Integer.toString(webStatsMode));
        try {
            Files.createDirectories(file.getParent());
            try(var writer=Files.newBufferedWriter(file)){values.store(writer,"AFKC Director Add-on (client only)");}
        } catch(IOException e){DirectorClient.LOG.warn("Cannot save director config",e);}
    }
}

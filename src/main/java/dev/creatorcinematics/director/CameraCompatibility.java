package dev.creatorcinematics.director;
import net.fabricmc.loader.api.FabricLoader;
import java.lang.reflect.*;
import java.util.*;

/** Optional public APIs verified in the 26.3 JARs; no foreign classes in our linkage. */
public final class CameraCompatibility {
    private CameraCompatibility() { }
    private static final class Probe {
        final String mod,owner,instance;
        final String[] names;
        Method[] methods;
        Object receiver;
        boolean checked,failed;
        Probe(String mod,String owner,String instance,String... names) {
            this.mod=mod;this.owner=owner;this.instance=instance;this.names=names;
        }
        boolean active() {
            if(!FabricLoader.getInstance().isModLoaded(mod)) return false;
            if(failed) return true;
            try {
                if(!checked) {
                    Class<?> type=Class.forName(owner,false,CameraCompatibility.class.getClassLoader());
                    receiver=instance==null?null:type.getField(instance).get(null);
                    methods=new Method[names.length];
                    for(int i=0;i<names.length;i++) methods[i]=type.getMethod(names[i]);
                    checked=true;
                }
                for(Method method:methods) if(Boolean.TRUE.equals(method.invoke(receiver))) return true;
                return false;
            } catch(ReflectiveOperationException|LinkageError e) {
                failed=true;
                DirectorClient.LOG.warn("Camera compatibility API changed for {}; suspending cinematics while this mod is present",mod,e);
                return true;
            }
        }
    }
    private static final List<Probe> PROBES=List.of(
        new Probe("freecam","net.xolt.freecam.Freecam",null,"isEnabled"),
        new Probe("flashback","com.moulberry.flashback.Flashback",null,"isInReplay","isExporting"),
        new Probe("zoomify","dev.isxander.zoomify.Zoomify","INSTANCE","getZooming","getSecondaryZooming")
    );
    public static boolean active() {
        for(var probe:PROBES) if(probe.active()) return true;
        return false;
    }
}

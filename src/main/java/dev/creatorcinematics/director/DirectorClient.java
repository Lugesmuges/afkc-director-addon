package dev.creatorcinematics.director;

import com.spunkyinsaan.afkcinematics.CinematicCameraRig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.storage.LevelResource;
import java.io.IOException;

public final class DirectorClient implements ClientModInitializer {
    public static final Logger LOG=LoggerFactory.getLogger("afkc_director");
    public static final DirectorConfig CONFIG=new DirectorConfig();
    private static ActivityMemory MEMORY=new ActivityMemory();
    private static final ActivityArchive ARCHIVE=new ActivityArchive(FabricLoader.getInstance().getConfigDir().resolve("afkc-director-memory"));
    private static String worldKey,dimensionKey;
    private static boolean dirty;
    private static Point viewDirection=new Point(0,0,1);
    private static ShotAdvisor advisor=new ShotAdvisor(new Random());
    private static final List<EditCandidate> pending=new ArrayList<>();
    private static ClientLevel world;
    private static long tick,lastInput=-1000;
    private static Point lastMotif;
    private static int motifRepeats;
    private static final ActivityObserver observer=new ActivityObserver();
    private static final WalkTrail trail=new WalkTrail();
    private static int routeSequence;
    public record EditCandidate(ClientLevel world,BlockPos pos,BlockState reference,ActivityMemory.Kind kind,long due) { }
    private static LiteralArgumentBuilder<FabricClientCommandSource> literal(String name) {
        return LiteralArgumentBuilder.literal(name);
    }

    @Override public void onInitializeClient() {
        CONFIG.load();
        ClientTickEvents.START_CLIENT_TICK.register(DirectorClient::tick);
        ClientLifecycleEvents.CLIENT_STOPPING.register(client->saveMemory());
        ClientCommandRegistrationCallback.EVENT.register((dispatcher,access)->dispatcher.register(literal("afkdirector")
                .executes(c->{c.getSource().sendFeedback(Component.literal(status()));return 1;})
                .then(literal("on").executes(c->{CONFIG.enabled=true;CONFIG.save();return 1;}))
                .then(literal("off").executes(c->{CONFIG.enabled=false;CONFIG.save();return 1;}))
                .then(literal("clear").executes(c->{clear();c.getSource().sendFeedback(Component.literal("Director: memory cleared for this world and dimension."));return 1;}))
                .then(literal("remember").executes(c->{int count=rememberView(c.getSource().getClient());c.getSource().sendFeedback(Component.literal("Director: "+count+" visible surfaces remembered. Look at the build and repeat from another side if needed."));return count>0?1:0;}))
                .then(literal("settings").executes(c->{c.getSource().getClient().schedule(()->c.getSource().getClient().gui.setScreen(new DirectorScreen(null)));return 1;}))));
        LOG.info("AFKC Director alpha loaded, client-only; AFK Cinematics 1.0.6+26.3 required.");
        for(String id:List.of("sodium","iris","entityculling","modmenu","freecam","zoomify","do_a_barrel_roll","flashback","distanthorizons","nvidium","voxy","bobby")) {
            FabricLoader.getInstance().getModContainer(id).ifPresent(m->LOG.info("Compatibility inventory: {} {} (presence is not runtime certification)",id,m.getMetadata().getVersion()));
        }
    }
    public static String status(){return "Director "+(CONFIG.enabled?"ON":"OFF")+" | "+MEMORY.size()+" remembered areas | radius "+CONFIG.radius;}
    private static long memoryTime(){return System.currentTimeMillis()/50;}
    public static void input(){lastInput=tick;}
    private static void resetSequence(){pending.clear();lastMotif=null;motifRepeats=0;advisor=new ShotAdvisor(new Random());observer.reset();trail.clear();routeSequence=0;}
    public static void clear(){MEMORY.clear();resetSequence();dirty=true;saveMemory();}
    private static void saveMemory() {
        if(!dirty || worldKey==null || dimensionKey==null)return;
        try {ARCHIVE.save(worldKey,dimensionKey,MEMORY);dirty=false;}
        catch(IOException e){LOG.warn("Cannot save local director memory",e);}
    }
    private static void changeWorld(Minecraft client) {
        saveMemory();MEMORY=new ActivityMemory();resetSequence();dirty=false;
        world=client.level;worldKey=dimensionKey=null;lastInput=-1000;
        if(world==null)return;
        if(client.getSingleplayerServer()!=null)
            worldKey="local:"+client.getSingleplayerServer().getWorldPath(LevelResource.ROOT).toAbsolutePath().normalize();
        else if(client.getCurrentServer()!=null)
            worldKey="server:"+client.getCurrentServer().ip.toLowerCase(Locale.ROOT);
        // Unknown connections stay session-local instead of sharing an ambiguous persistence key.
        dimensionKey=world.dimension().identifier().toString();
        if(worldKey!=null)try {MEMORY=ARCHIVE.load(worldKey,dimensionKey);}
        catch(IOException e){LOG.warn("Cannot load local director memory; starting empty",e);}
    }
    private static void tick(Minecraft client) {
        tick++;
        if(client.level!=world)changeWorld(client);
        if(client.level==null || client.player==null) return;
        if(!CinematicCameraRig.isActive())viewDirection=WorldSpace.point(client.player.getLookAngle());
        if(client.options.keyUp.isDown() || client.options.keyDown.isDown() || client.options.keyLeft.isDown() || client.options.keyRight.isDown()) lastInput=tick;
        for(var it=pending.iterator();it.hasNext();) {
            var edit=it.next();
            if(tick<edit.due()) continue;
            it.remove();
            if(edit.world()!=world || !world.hasChunkAt(edit.pos())) continue;
            var after=world.getBlockState(edit.pos());
            boolean changed=edit.kind()==ActivityMemory.Kind.BREAK ? !edit.reference().isAir() && after.isAir()
                    : !after.isAir() && after.is(edit.reference().getBlock());
            if(changed) {MEMORY.record(WorldSpace.point(Vec3.atCenterOf(edit.pos())),edit.kind(),memoryTime());dirty=true;}
        }
        boolean observing=CONFIG.enabled && client.gui.screen()==null && !CinematicCameraRig.isActive() && !foreignCamera(client);
        Point eye=WorldSpace.point(client.player.getEyePosition());
        if(tick%20==0 && observer.sample(MEMORY,eye,memoryTime(),observing))dirty=true;
        if(tick%5==0)trail.sample(eye,tick,observing && client.player.onGround() && !client.player.isPassenger());
        if(tick%1200==0)saveMemory();
    }
    public static void cinematicStarted(Minecraft client){
        if(!CONFIG.enabled || client.player==null || client.level==null || !CinematicCameraRig.isActive() || foreignCamera(client))return;
        if(client.level!=world)changeWorld(client);
        MEMORY.record(WorldSpace.point(client.player.getEyePosition()),ActivityMemory.Kind.AFK,memoryTime());
        observer.reset();dirty=true;saveMemory();
    }
    public static RouteGuide route(Minecraft client){
        if(!CONFIG.enabled || client.player==null || client.level==null || foreignCamera(client) || ++routeSequence%4!=0)return null;
        Point eye=WorldSpace.point(client.player.getEyePosition());
        if(MEMORY.ranked(eye,24,memoryTime()).isEmpty())return null;
        return trail.route(eye,CONFIG.radius,new WorldSpace(client));
    }
    public static EditCandidate prepare(BlockPos pos,ActivityMemory.Kind kind) {
        var client=Minecraft.getInstance();
        if(!CONFIG.enabled || client.level==null || !client.level.hasChunkAt(pos)) return null;
        return new EditCandidate(client.level,pos.immutable(),client.level.getBlockState(pos),kind,tick+12);
    }
    public static void placed(BlockPos pos,BlockState state) {
        var client=Minecraft.getInstance();
        if(CONFIG.enabled && client.level!=null) {
            accept(new EditCandidate(client.level,pos.immutable(),state,ActivityMemory.Kind.PLACE,tick+12));
        }
    }
    public static void accept(EditCandidate candidate) {
        if(candidate==null || pending.size()>=32) return;
        if(pending.stream().noneMatch(e->e.world()==candidate.world() && e.pos().equals(candidate.pos()))) pending.add(candidate);
    }
    public static boolean foreignCamera(Minecraft client) {
        if(client.player==null) return false;
        if(client.player.isFallFlying()) return true;
        if(client.getCameraEntity()!=null && client.getCameraEntity()!=client.player) return true;
        return CameraCompatibility.active();
    }
    public static int rememberView(Minecraft client) {
        if(client.player==null || client.level==null || foreignCamera(client) || CinematicCameraRig.isActive())return 0;
        Vec3 eye=client.player.getEyePosition();int count=0;
        for(int yaw=-2;yaw<=2;yaw++)for(int pitch=-1;pitch<=1;pitch++) {
            Vec3 direction=Vec3.directionFromRotation(client.player.getXRot()+pitch*15,client.player.getYRot()+yaw*15);
            var hit=client.level.clip(new ClipContext(eye,eye.add(direction.scale(40)),ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,client.player));
            if(hit.getType()!=HitResult.Type.BLOCK || !client.level.hasChunkAt(hit.getBlockPos()))continue;
            Point target=WorldSpace.point(Vec3.atCenterOf(hit.getBlockPos()));
            if(!new WorldSpace(client).visible(WorldSpace.point(eye),target))continue;
            MEMORY.record(target,ActivityMemory.Kind.PLACE,memoryTime());count++;
        }
        if(count>0){dirty=true;saveMemory();}
        return count;
    }
    public static WideShot wideShot(Minecraft client,Point nativeCamera,int sequence) {
        if(!CONFIG.enabled || !CONFIG.wideShots || client.player==null || client.level==null || foreignCamera(client))return null;
        Point eye=WorldSpace.point(client.player.getEyePosition());
        double aspect=(double)client.getWindow().getWidth()/Math.max(1,client.getWindow().getHeight());
        for(var subject:MEMORY.ranked(eye,24,memoryTime()).stream().filter(ActivityMemory.Subject::built).limit(3).toList()){
            var shot=WideShot.plan(subject,eye,nativeCamera,new WorldSpace(client),CONFIG.wideRadius,aspect,sequence);
            if(shot!=null)return shot;
        }
        return null;
    }
    public static ShotAdvisor.Advice advise(Minecraft client,Point nativeCamera) {
        if(!CONFIG.enabled || client.player==null || client.level==null || foreignCamera(client)) return null;
        Point eye=WorldSpace.point(client.player.getEyePosition());
        var space=new WorldSpace(client);
        boolean cozy=client.player.isPassenger() || client.level.isRaining() && !client.level.canSeeSky(client.player.blockPosition());
        Point context=null;
        if(cozy) {
            Vec3 end=WorldSpace.vec(eye.add(viewDirection.scale(12)));
            var hit=client.level.clip(new ClipContext(WorldSpace.vec(eye),end,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,client.player));
            context=WorldSpace.point(hit.getType()==HitResult.Type.MISS?end:hit.getLocation());
        }
        var subjects=new ArrayList<>(MEMORY.ranked(eye,24,memoryTime()));
        // Four shots describe a project: overview, detail, surroundings, medium.
        if(motifRepeats>=4 && subjects.size()>1 && lastMotif!=null) {
            subjects.sort(Comparator.comparingInt(s->s.focus().distance(lastMotif)<6?1:0));
        }
        for(var subject:subjects.stream().limit(3).toList()) {
            var shot=advisor.advise(eye,nativeCamera,subject,context,space,CONFIG.radius);
            if(shot!=null) {
                motifRepeats=lastMotif!=null && lastMotif.distance(subject.focus())<6 ? motifRepeats+1:1;
                lastMotif=subject.focus();
                return shot;
            }
        }
        return null;
    }
}

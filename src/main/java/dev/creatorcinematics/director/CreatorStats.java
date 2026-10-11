package dev.creatorcinematics.director;

import com.spunkyinsaan.afkcinematics.CinematicCameraRig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ServerboundClientCommandPacket;
import net.minecraft.stats.Stats;
import java.time.Instant;
import java.util.*;

/** Client-thread bridge between genuine server counters, bounded website data, and the overlay. */
public final class CreatorStats {
    public record View(StatsData.Card card,StatsTimeline.Frame animation) { }
    private static final StatsStore STORE=new StatsStore();
    private static final StatsTimeline TIMELINE=new StatsTimeline();
    private static final StatsWebClient WEB=new StatsWebClient();
    private static ClientLevel world;
    private static ClientPacketListener connection;
    private static String playerName="";
    private static UUID playerId;
    private static long lastLocalRequest;
    private static boolean requested;
    private CreatorStats() { }

    private static void observeWorld(Minecraft client) {
        String name=client.getUser().getName();
        UUID id=client.getUser().getProfileId();
        if(world==client.level && connection==client.getConnection() && playerName.equals(name) && Objects.equals(playerId,id))return;
        world=client.level;connection=client.getConnection();playerName=name;playerId=id;
        STORE.reset();TIMELINE.stop();requested=false;
    }
    private static boolean eligible(Minecraft client) {
        return DirectorClient.CONFIG.enabled && DirectorClient.CONFIG.statsEnabled && client.level!=null && client.player!=null
                && client.gui.screen()==null && client.gui.overlay()==null && CinematicCameraRig.isActive() && !DirectorClient.foreignCamera(client);
    }
    private static boolean webAllowed(Minecraft client) {
        var server=client.getCurrentServer();
        return StatsData.externalAllowed(DirectorClient.CONFIG.webStatsMode,client.hasSingleplayerServer(),server==null?"":server.name,server==null?"":server.ip);
    }
    public static void started(Minecraft client) {
        observeWorld(client);
        if(!eligible(client))return;
        long now=System.nanoTime();
        TIMELINE.start(now);
        refresh(client,now);
    }
    public static void stop() {TIMELINE.stop();}
    public static void tick(Minecraft client) {
        observeWorld(client);
        if(!eligible(client)) {stop();return;}
        refresh(client,System.nanoTime());
    }
    private static void refresh(Minecraft client,long now) {
        if(connection!=null && (!requested || now-lastLocalRequest>=60_000_000_000L)) {
            requested=true;lastLocalRequest=now;
            connection.send(new ServerboundClientCommandPacket(ServerboundClientCommandPacket.Action.REQUEST_STATS));
        }
        if(!webAllowed(client))return;
        long generation=STORE.generation();
        var expectedWorld=world;
        var expectedConnection=connection;
        var future=WEB.request(playerName,playerId,now);
        if(future!=null)future.thenAccept(cards->client.execute(()->{
            if(client.level==expectedWorld && client.getConnection()==expectedConnection && webAllowed(client))STORE.website(generation,cards);
        }));
    }

    /** TAIL of handleAwardStats: counters are now populated, including legitimate zeroes. */
    public static void received(ClientPacketListener listener) {
        var client=Minecraft.getInstance();
        observeWorld(client);
        if(!requested || client.player==null || client.level==null || listener!=connection)return;
        var stats=client.player.getStats();
        long broken=0;
        for(var stat:Stats.BLOCK_MINED)broken+=Math.max(0,stats.getValue(stat));
        STORE.local(StatsData.local(playerName,
                stats.getValue(Stats.CUSTOM,Stats.PLAY_TIME),stats.getValue(Stats.CUSTOM,Stats.WALK_ONE_CM),
                stats.getValue(Stats.CUSTOM,Stats.SPRINT_ONE_CM),stats.getValue(Stats.CUSTOM,Stats.CROUCH_ONE_CM),
                broken,stats.getValue(Stats.CUSTOM,Stats.MOB_KILLS),stats.getValue(Stats.CUSTOM,Stats.JUMP),
                stats.getValue(Stats.CUSTOM,Stats.DEATHS),Instant.now()));
    }
    public static List<StatsData.Card> cards() {
        var client=Minecraft.getInstance();
        observeWorld(client);
        return STORE.cards(Instant.now(),webAllowed(client));
    }
    public static View view(long now) {
        var client=Minecraft.getInstance();
        observeWorld(client);
        if(!eligible(client)) {stop();return null;}
        var cards=cards();
        var frame=TIMELINE.frame(now,cards.size());
        return frame.visible()?new View(cards.get(frame.index()),frame):null;
    }
    public static void render(GuiGraphicsExtractor graphics) {
        var view=view(System.nanoTime());
        if(view!=null)StatsRenderer.render(Minecraft.getInstance(),graphics,view);
    }
}

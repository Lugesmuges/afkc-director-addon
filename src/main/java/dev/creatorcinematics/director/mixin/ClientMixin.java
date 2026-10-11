package dev.creatorcinematics.director.mixin;

import com.spunkyinsaan.afkcinematics.CinematicCameraRig;
import dev.creatorcinematics.director.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.sounds.Music;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets="com.spunkyinsaan.afkcinematics.AfkCinematicsClient",remap=false)
public abstract class ClientMixin {
    @Shadow private boolean cinematicMusicEnabled;
    @Shadow private int inactivityTicks;
    @Shadow private int suppressActivityTicks;
    @Shadow private boolean forceStartRequested;
    @Shadow private int creditsTicksRemaining;
    @Shadow private int fadeInTicksRemaining;
    @Shadow private int movementGraceTicks;
    @Shadow private void stopCinematic(Minecraft client,boolean ui,boolean movement,boolean input,double distance) { }
    @Unique private ClientLevel afkcd$world;
    @Inject(method="onInitializeClient",at=@At("TAIL"))
    private void afkcd$silentDefault(CallbackInfo ci){cinematicMusicEnabled=false;}
    @Inject(method="setCinematicMusicEnabled",at=@At("HEAD"),cancellable=true)
    private void afkcd$keepSilent(Minecraft client,boolean enabled,CallbackInfo ci) {
        cinematicMusicEnabled=false;
        ci.cancel();
    }
    @Inject(method="isCinematicMusicEnabled",at=@At("HEAD"),cancellable=true)
    private void afkcd$silentStatus(CallbackInfoReturnable<Boolean> ci){ci.setReturnValue(false);}
    @Inject(method="playCinematicMusic",at=@At("HEAD"),cancellable=true)
    private void afkcd$noMusic(Minecraft client,Music music,boolean explicit,CallbackInfo ci) {
        // Also covers /afkc choose ... start, which bypasses the original music toggle.
        ci.cancel();
    }
    @Inject(method="markInputActivity",at=@At("HEAD"))
    private static void afkcd$input(CallbackInfo ci){DirectorClient.input();CreatorStats.stop();}
    @Inject(method="startCinematic",at=@At("HEAD"),cancellable=true)
    private void afkcd$yieldCamera(Minecraft client,String reason,CallbackInfo ci) {
        if(DirectorClient.foreignCamera(client)) ci.cancel();
    }
    @Inject(method="startCinematic",at=@At("TAIL"))
    private void afkcd$rememberStay(Minecraft client,String reason,CallbackInfo ci){
        creditsTicksRemaining=0;
        if(!CinematicCameraRig.isActive()) {
            // A rejected initial view is not input: AFKC discards input during startup grace.
            // Reset the actual timeout, otherwise a sealed room triggers a new search every tick.
            inactivityTicks=0;
            suppressActivityTicks=0;
            forceStartRequested=false;
            fadeInTicksRemaining=0;
            movementGraceTicks=0;
            return;
        }
        DirectorClient.cinematicStarted(client);
        CreatorStats.started(client);
    }
    @Inject(method="stopCinematic",at=@At("HEAD"))
    private void afkcd$stopStats(Minecraft client,boolean ui,boolean movement,boolean input,double distance,CallbackInfo ci){CreatorStats.stop();}
    @Inject(method="renderOverlay",at=@At("TAIL"))
    private void afkcd$statsOverlay(GuiGraphicsExtractor graphics,DeltaTracker delta,CallbackInfo ci){CreatorStats.render(graphics);}
    @Inject(method="onClientTick",at=@At("HEAD"))
    private void afkcd$interrupt(Minecraft client,CallbackInfo ci) {
        boolean worldChanged=client.level!=afkcd$world;
        afkcd$world=client.level;
        if(worldChanged || DirectorClient.foreignCamera(client)) {
            // Do not use the input flag: AFKC deliberately ignores it for its first eight ticks.
            if(CinematicCameraRig.isActive()) stopCinematic(client,false,false,true,0);
            inactivityTicks=0;
            suppressActivityTicks=0;
            forceStartRequested=false;
        }
        CreatorStats.tick(client);
    }
}

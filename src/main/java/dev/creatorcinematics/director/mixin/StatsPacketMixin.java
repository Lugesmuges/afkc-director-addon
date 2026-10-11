package dev.creatorcinematics.director.mixin;

import dev.creatorcinematics.director.CreatorStats;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundAwardStatsPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public abstract class StatsPacketMixin {
    @Inject(method="handleAwardStats",at=@At("TAIL"))
    private void afkcd$receivedStats(ClientboundAwardStatsPacket packet,CallbackInfo ci) {
        CreatorStats.received((ClientPacketListener)(Object)this);
    }
}

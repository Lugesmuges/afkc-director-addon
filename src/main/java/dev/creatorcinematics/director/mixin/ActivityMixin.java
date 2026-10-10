package dev.creatorcinematics.director.mixin;

import dev.creatorcinematics.director.*;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MultiPlayerGameMode.class)
public abstract class ActivityMixin {
    @Unique private DirectorClient.EditCandidate afkcd$broken;
    @Inject(method="destroyBlock",at=@At("HEAD"))
    private void afkcd$beforeBreak(BlockPos pos,CallbackInfoReturnable<Boolean> ci) {
        afkcd$broken=DirectorClient.prepare(pos,ActivityMemory.Kind.BREAK);
    }
    @Inject(method="destroyBlock",at=@At("RETURN"))
    private void afkcd$afterBreak(BlockPos pos,CallbackInfoReturnable<Boolean> ci) {
        if(ci.getReturnValueZ()) DirectorClient.accept(afkcd$broken);
        afkcd$broken=null;
    }
}

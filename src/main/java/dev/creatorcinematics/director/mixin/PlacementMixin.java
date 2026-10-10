package dev.creatorcinematics.director.mixin;

import dev.creatorcinematics.director.DirectorClient;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockItem.class)
public abstract class PlacementMixin {
    @Inject(method="placeBlock",at=@At("RETURN"))
    private void afkcd$placed(BlockPlaceContext context,BlockState state,CallbackInfoReturnable<Boolean> ci) {
        // The same BlockItem instance can be used by the integrated server: no shared per-call fields.
        if(!context.getLevel().isClientSide() || !ci.getReturnValueZ()) return;
        var client=Minecraft.getInstance();
        if(context.getLevel()==client.level && context.getPlayer()==client.player) {
            DirectorClient.placed(context.getClickedPos(),state);
        }
    }
}

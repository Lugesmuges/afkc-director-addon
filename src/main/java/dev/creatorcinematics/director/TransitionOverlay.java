package dev.creatorcinematics.director;

import com.spunkyinsaan.afkcinematics.CinematicCameraRig;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public final class TransitionOverlay {
    private TransitionOverlay() { }
    public static void render(GuiGraphicsExtractor graphics){
        if(!DirectorClient.CONFIG.enabled || !CinematicCameraRig.isActive())return;
        int alpha=(int)Math.round(255*DirectorClient.TRANSITION.alpha(System.nanoTime()));
        if(alpha>0)graphics.fill(0,0,graphics.guiWidth(),graphics.guiHeight(),alpha<<24);
    }
}

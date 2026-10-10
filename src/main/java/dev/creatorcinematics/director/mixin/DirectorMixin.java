package dev.creatorcinematics.director.mixin;

import com.spunkyinsaan.afkcinematics.CinematicCameraRig;
import dev.creatorcinematics.director.*;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Retarget native presets. AFKC still owns timing, oscillation, smoothing, FOV and rendering. */
@Mixin(targets="com.spunkyinsaan.afkcinematics.CinematicDirector",remap=false)
public abstract class DirectorMixin {
    @Shadow private Vec3 shotBaseCamera,shotBaseFocus,shotRightAxis,shotUpAxis,cameraPosition,focusPosition;
    @Shadow private int shotTicksRemaining;
    @Shadow abstract void stop(Minecraft client);
    @Shadow private Vec3 enforceAboveGround(Player player,Vec3 point,double margin){return point;}
    @Shadow private Vec3 avoidBlockClipping(Player player,Vec3 camera,Vec3 focus,double margin){return camera;}
    @Unique private boolean afkcd$advised;
    @Unique private Point afkcd$previous;
    @Unique private RouteGuide afkcd$route;
    @Unique private boolean afkcd$rejected;
    @Unique private Point afkcd$lastShown;
    @Unique private final ShotRetry afkcd$retry=new ShotRetry();
    @Unique private boolean afkcd$failedStart;

    @Inject(method="chooseNextShot",at=@At("HEAD"))
    private void afkcd$begin(Minecraft client,CallbackInfo ci){afkcd$advised=false;afkcd$previous=null;afkcd$route=null;afkcd$rejected=false;}

    @Inject(method="configureShot",at=@At("TAIL"))
    private void afkcd$frame(Player player,@Coerce Object preset,CallbackInfo ci) {
        afkcd$advised=false;
        // Native orbits explicitly follow the player on every tick; keep them as occasional character shots.
        if(preset.toString().contains("ORBIT"))return;
        var client=Minecraft.getInstance();
        afkcd$route=DirectorClient.route(client);
        if(afkcd$route!=null){
            var hint=afkcd$route.next(afkcd$route.start(),WorldSpace.point(player.getEyePosition()),DirectorClient.CONFIG.radius,new WorldSpace(client));
            if(hint!=null){
                shotBaseCamera=WorldSpace.vec(afkcd$route.start());shotBaseFocus=WorldSpace.vec(hint.focus());
                // Corridor motion comes from the guide. AFKC's native lerps and rotations remain.
                shotRightAxis=shotUpAxis=Vec3.ZERO;afkcd$advised=true;return;
            }
            afkcd$route=null;
        }
        var advice=DirectorClient.advise(client,WorldSpace.point(shotBaseCamera));
        if(advice==null)return;
        Vec3 focus=enforceAboveGround(player,WorldSpace.vec(advice.focus()),0.3);
        Vec3 camera=enforceAboveGround(player,WorldSpace.vec(advice.camera()),0.95);
        camera=enforceAboveGround(player,avoidBlockClipping(player,camera,focus,0.3),0.95);
        var space=new WorldSpace(client);
        Point eye=WorldSpace.point(player.getEyePosition()),position=WorldSpace.point(camera),target=WorldSpace.point(focus);
        if(position.distance(eye)>DirectorClient.CONFIG.radius || !space.clear(eye,position) || !space.visible(position,target))return;
        Vec3 direction=focus.subtract(camera).normalize();
        if(direction.lengthSqr()<0.001)return;
        Vec3 horizontal=new Vec3(-direction.z,0,direction.x);
        if(horizontal.lengthSqr()<0.001)return;
        shotBaseCamera=camera;shotBaseFocus=focus;
        shotRightAxis=horizontal.normalize();shotUpAxis=shotRightAxis.cross(direction).normalize();
        afkcd$advised=true;
    }
    @Inject(method="chooseNextShot",at=@At("TAIL"))
    private void afkcd$chosen(Minecraft client,CallbackInfo ci){afkcd$previous=afkcd$lastShown;}

    @Unique private boolean afkcd$goodFrame(Vec3 position,float yaw,float pitch){
        var client=Minecraft.getInstance();
        if(client.player==null || client.level==null)return false;
        double aspect=(double)client.getWindow().getWidth()/Math.max(1,client.getWindow().getHeight());
        return FrameQuality.acceptable(WorldSpace.point(position),WorldSpace.point(Vec3.directionFromRotation(pitch,yaw)),
                client.options.fov().get(),aspect,new WorldSpace(client));
    }
    @Redirect(method={"chooseNextShot","refreshAfterPassiveMovement"},at=@At(value="INVOKE",target="Lcom/spunkyinsaan/afkcinematics/CinematicCameraRig;cutTo(Lnet/minecraft/world/phys/Vec3;FF)V"))
    private void afkcd$validateCut(Vec3 position,float yaw,float pitch){
        var client=Minecraft.getInstance();
        boolean reachable=!afkcd$advised || client.player!=null && WorldSpace.point(position).distance(WorldSpace.point(client.player.getEyePosition()))<=DirectorClient.CONFIG.radius
                && new WorldSpace(client).clear(WorldSpace.point(client.player.getEyePosition()),WorldSpace.point(position));
        if(DirectorClient.CONFIG.enabled && (!reachable || !afkcd$goodFrame(position,yaw,pitch))){
            afkcd$rejected=true;shotTicksRemaining=afkcd$retry.rejectedDelay();return;
        }
        afkcd$retry.accepted();
        afkcd$lastShown=WorldSpace.point(position);
        CinematicCameraRig.cutTo(position,yaw,pitch);
    }
    @Redirect(method="start",at=@At(value="INVOKE",target="Lcom/spunkyinsaan/afkcinematics/CinematicCameraRig;activate(Lnet/minecraft/world/phys/Vec3;FF)V"))
    private void afkcd$safeInitialView(Vec3 position,float yaw,float pitch){
        var client=Minecraft.getInstance();
        afkcd$failedStart=false;
        if(DirectorClient.CONFIG.enabled && client.player!=null && !afkcd$goodFrame(position,yaw,pitch)){
            var space=new WorldSpace(client);Point eye=WorldSpace.point(client.player.getEyePosition());
            boolean found=false;
            search: for(double distance:new double[]{2,4})for(int angle=0;angle<8;angle++){
                float outward=client.player.getYRot()+angle*45;
                Vec3 candidate=client.player.getEyePosition().add(Vec3.directionFromRotation(0,outward).scale(distance));
                if(!space.clear(eye,WorldSpace.point(candidate)))continue;
                for(int turn:new int[]{0,90,-90,180})if(afkcd$goodFrame(candidate,outward+turn,0)){
                    position=candidate;yaw=outward+turn;pitch=0;found=true;break search;
                }
            }
            // A tiny sealed space may offer no usable view. Restore gameplay instead of
            // activating a camera inside the rendered player and retry only after inactivity.
            if(!found){afkcd$failedStart=true;return;}
            cameraPosition=position;
        }
        afkcd$lastShown=WorldSpace.point(position);
        CinematicCameraRig.activate(position,yaw,pitch);
    }
    @Inject(method="start",at=@At("TAIL"))
    private void afkcd$declineBlockedStart(Minecraft client,CallbackInfo ci){
        if(afkcd$failedStart)stop(client);
    }

    @Inject(method="applyCurrentShot",at=@At("HEAD"),cancellable=true)
    private void afkcd$guide(Minecraft client,CallbackInfo ci){
        if(afkcd$rejected){ci.cancel();return;}
        if(afkcd$route==null)return;
        var hint=client.player==null || !DirectorClient.CONFIG.enabled || DirectorClient.foreignCamera(client)?null
                :afkcd$route.next(WorldSpace.point(cameraPosition),WorldSpace.point(client.player.getEyePosition()),DirectorClient.CONFIG.radius,new WorldSpace(client));
        if(hint==null){shotTicksRemaining=0;ci.cancel();return;}
        shotBaseCamera=WorldSpace.vec(hint.camera());shotBaseFocus=WorldSpace.vec(hint.focus());
    }
    @Inject(method="enforceAboveGround",at=@At("HEAD"),cancellable=true)
    private void afkcd$keepInteriorHeight(Player player,Vec3 point,double margin,CallbackInfoReturnable<Vec3> ci){
        // Terrain height maps describe roofs, not corridor floors. Route sweeps check real blocks.
        if(afkcd$route!=null)ci.setReturnValue(point);
    }
    @Inject(method="getSubtleShake",at=@At("HEAD"),cancellable=true)
    private void afkcd$steadyInCorridor(double intensity,CallbackInfoReturnable<Vec3> ci){
        if(afkcd$route!=null)ci.setReturnValue(Vec3.ZERO);
    }

    @Redirect(method={"applyCurrentShot","applyPlayerOrbitShot"},at=@At(value="INVOKE",target="Lcom/spunkyinsaan/afkcinematics/CinematicCameraRig;update(Lnet/minecraft/world/phys/Vec3;FF)V"))
    private void afkcd$validateNativeMovement(Vec3 position,float yaw,float pitch) {
        if(DirectorClient.CONFIG.enabled && !afkcd$goodFrame(position,yaw,pitch)){
            // At a tight corner, allow the original rotation to catch up before advancing.
            // No extra interpolation or extended shot timer is introduced.
            if(afkcd$route!=null && afkcd$previous!=null && afkcd$goodFrame(WorldSpace.vec(afkcd$previous),yaw,pitch)){
                cameraPosition=WorldSpace.vec(afkcd$previous);
                CinematicCameraRig.update(cameraPosition,yaw,pitch);return;
            }
            shotTicksRemaining=0;return;
        }
        if(afkcd$advised) {
            var client=Minecraft.getInstance();var space=new WorldSpace(client);Point next=WorldSpace.point(position);
            if(client.player==null || afkcd$previous==null || !DirectorClient.CONFIG.enabled
                    || next.distance(WorldSpace.point(client.player.getEyePosition()))>DirectorClient.CONFIG.radius
                    || !space.clear(afkcd$previous,next) || !space.visible(next,WorldSpace.point(focusPosition))) {
                shotTicksRemaining=0;return;
            }
            afkcd$previous=next;
        }
        afkcd$lastShown=WorldSpace.point(position);
        CinematicCameraRig.update(position,yaw,pitch);
    }
    @Inject(method="stop",at=@At("TAIL"))
    private void afkcd$reset(Minecraft client,CallbackInfo ci){afkcd$advised=false;afkcd$previous=null;afkcd$route=null;afkcd$lastShown=null;afkcd$rejected=false;afkcd$retry.accepted();}
}

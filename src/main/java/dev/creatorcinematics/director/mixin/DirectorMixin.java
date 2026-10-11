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

/** Retarget native presets and replace idle sway with deliberate, collision-checked travel. */
@Mixin(targets="com.spunkyinsaan.afkcinematics.CinematicDirector",remap=false)
public abstract class DirectorMixin {
    @Shadow private Vec3 shotBaseCamera,shotBaseFocus,shotRightAxis,shotUpAxis,cameraPosition,focusPosition;
    @Shadow private int shotTicksRemaining,shotElapsedTicks,currentShotDurationTicks;
    @Shadow private float cameraYaw,cameraPitch;
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
    @Unique private CinematicMove afkcd$move;
    @Unique private int afkcd$moveSequence;
    @Unique private WideShot afkcd$wide;
    @Unique private WideShot afkcd$heldWide;
    @Unique private int afkcd$wideSequence;
    @Unique private boolean afkcd$hasShot;

    @Inject(method="chooseNextShot",at=@At("HEAD"),cancellable=true)
    private void afkcd$begin(Minecraft client,CallbackInfo ci){
        if(DirectorClient.CONFIG.enabled && afkcd$hasShot && shotTicksRemaining<=0
                && !DirectorClient.TRANSITION.ready(System.nanoTime())){
            afkcd$holdFrame();ci.cancel();return;
        }
        afkcd$advised=false;afkcd$previous=null;afkcd$route=null;afkcd$move=null;afkcd$rejected=false;
        if(afkcd$wide!=null){afkcd$heldWide=afkcd$wide;afkcd$wide=null;}
        int sequence=afkcd$wideSequence++;
        if(client.player==null)return;
        ShotAdvisor.Advice custom=null;
        if(sequence%3==0){
            afkcd$wide=DirectorClient.wideShot(client,WorldSpace.point(cameraPosition),sequence/3);
            if(afkcd$wide!=null)custom=new ShotAdvisor.Advice(afkcd$wide.camera(),afkcd$wide.focus());
        }else if(sequence%3==1)custom=DirectorClient.characterShot(client,sequence/3);
        if(custom==null)return;
        cameraPosition=shotBaseCamera=WorldSpace.vec(custom.camera());
        focusPosition=shotBaseFocus=WorldSpace.vec(custom.focus());
        Vec3 look=focusPosition.subtract(cameraPosition);
        cameraYaw=(float)Math.toDegrees(Math.atan2(look.z,look.x))-90;
        cameraPitch=(float)-Math.toDegrees(Math.atan2(look.y,Math.hypot(look.x,look.z)));
        int fov=afkcd$wide==null?30:afkcd$wide.fov();
        client.options.fov().set(fov);
        afkcd$heldWide=null;
        double aspect=(double)client.getWindow().getWidth()/Math.max(1,client.getWindow().getHeight());
        afkcd$move=CinematicMove.plan(custom.camera(),custom.focus(),WorldSpace.point(client.player.getEyePosition()),
                afkcd$wide==null?DirectorClient.CONFIG.radius:DirectorClient.CONFIG.wideRadius,afkcd$moveSequence++,
                afkcd$wide==null?new WorldSpace(client):afkcd$wide.space(new WorldSpace(client),aspect),fov,aspect);
        shotElapsedTicks=0;shotTicksRemaining=currentShotDurationTicks=afkcd$move.durationTicks();
        afkcd$previous=afkcd$lastShown=custom.camera();afkcd$retry.accepted();afkcd$hasShot=true;
        CinematicCameraRig.cutTo(cameraPosition,cameraYaw,cameraPitch);
        DirectorClient.TRANSITION.complete(System.nanoTime());ci.cancel();
    }
    @ModifyArg(method="tick",at=@At(value="INVOKE",target="Lcom/spunkyinsaan/afkcinematics/CinematicDirector;easeFov(Lnet/minecraft/client/Minecraft;FF)V"),index=1)
    private float afkcd$wideLens(float original){return afkcd$wide!=null?afkcd$wide.fov():afkcd$heldWide!=null?afkcd$heldWide.fov():original;}
    @Inject(method="refreshAfterPassiveMovement",at=@At("HEAD"),cancellable=true)
    private void afkcd$resetWideAfterMovement(Minecraft client,CallbackInfo ci){
        if(!DirectorClient.CONFIG.enabled)return;
        // AFKC otherwise cuts twice immediately on server position corrections.
        // Finish the current shot and let the regular covered-cut path relocate the camera.
        afkcd$rejected=true;shotTicksRemaining=0;afkcd$holdFrame();ci.cancel();
    }

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
        return afkcd$goodFrame(position,yaw,pitch,Minecraft.getInstance().options.fov().get());
    }
    @Unique private boolean afkcd$goodFrame(Vec3 position,float yaw,float pitch,double fov){
        var client=Minecraft.getInstance();
        if(client.player==null || client.level==null)return false;
        double aspect=(double)client.getWindow().getWidth()/Math.max(1,client.getWindow().getHeight());
        return FrameQuality.acceptable(WorldSpace.point(position),WorldSpace.point(Vec3.directionFromRotation(pitch,yaw)),
                fov,aspect,new WorldSpace(client));
    }
    @Redirect(method={"chooseNextShot","refreshAfterPassiveMovement"},at=@At(value="INVOKE",target="Lcom/spunkyinsaan/afkcinematics/CinematicCameraRig;cutTo(Lnet/minecraft/world/phys/Vec3;FF)V"))
    private void afkcd$validateCut(Vec3 position,float yaw,float pitch){
        var client=Minecraft.getInstance();
        if(DirectorClient.CONFIG.enabled){
            var space=new WorldSpace(client);
            boolean reachable=client.player!=null && WorldSpace.point(position).distance(WorldSpace.point(client.player.getEyePosition()))<=DirectorClient.CONFIG.radius
                    && space.clear(WorldSpace.point(client.player.getEyePosition()),WorldSpace.point(position))
                    && space.visible(WorldSpace.point(position),WorldSpace.point(focusPosition));
            if(!reachable || !afkcd$goodFrame(position,yaw,pitch,afkcd$heldWide==null?client.options.fov().get():30)){
                afkcd$rejected=true;shotTicksRemaining=afkcd$retry.rejectedDelay();afkcd$holdFrame();
                DirectorClient.TRANSITION.complete(System.nanoTime());return;
            }
        }
        afkcd$retry.accepted();
        if(afkcd$heldWide!=null){client.options.fov().set(30);afkcd$heldWide=null;}
        afkcd$lastShown=WorldSpace.point(position);
        if(DirectorClient.CONFIG.enabled && afkcd$route!=null)shotTicksRemaining=currentShotDurationTicks=360;
        if(DirectorClient.CONFIG.enabled && afkcd$route==null && client.player!=null){
            double aspect=(double)client.getWindow().getWidth()/Math.max(1,client.getWindow().getHeight());
            afkcd$move=CinematicMove.plan(WorldSpace.point(position),WorldSpace.point(focusPosition),
                    WorldSpace.point(client.player.getEyePosition()),DirectorClient.CONFIG.radius,
                    afkcd$moveSequence++,new WorldSpace(client),client.options.fov().get(),aspect);
            shotTicksRemaining=currentShotDurationTicks=afkcd$move.durationTicks();
        }
        CinematicCameraRig.cutTo(position,yaw,pitch);
        afkcd$hasShot=true;DirectorClient.TRANSITION.complete(System.nanoTime());
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
        if(afkcd$rejected){afkcd$holdFrame();ci.cancel();return;}
        if(afkcd$move!=null && DirectorClient.CONFIG.enabled){
            ci.cancel();
            if(client.player==null || DirectorClient.foreignCamera(client))return;
            var pose=afkcd$move.at(shotElapsedTicks);
            Point next=pose.camera(),focus=pose.focus();
            double aspect=(double)client.getWindow().getWidth()/Math.max(1,client.getWindow().getHeight());
            ShotAdvisor.Space space=afkcd$wide==null?new WorldSpace(client):afkcd$wide.space(new WorldSpace(client),aspect);
            double radius=afkcd$wide==null?DirectorClient.CONFIG.radius:DirectorClient.CONFIG.wideRadius;
            Vec3 look=WorldSpace.vec(focus.subtract(next));
            float yaw=(float)Math.toDegrees(Math.atan2(look.z,look.x))-90;
            float pitch=(float)-Math.toDegrees(Math.atan2(look.y,Math.hypot(look.x,look.z)));
            if(next.distance(WorldSpace.point(client.player.getEyePosition()))>radius
                    || !space.clear(WorldSpace.point(cameraPosition),next) || !space.visible(next,focus)
                    || !afkcd$goodFrame(WorldSpace.vec(next),yaw,pitch)){
                // A new obstacle ends the shot; never reposition it sideways to "repair" the path.
                shotTicksRemaining=0;
                afkcd$holdFrame();
                return;
            }
            cameraPosition=WorldSpace.vec(next);focusPosition=WorldSpace.vec(focus);
            cameraYaw=yaw;cameraPitch=pitch;
            afkcd$previous=afkcd$lastShown=next;
            // AFKC still performs render-frame interpolation, startup/exit and its black bars.
            CinematicCameraRig.update(cameraPosition,cameraYaw,cameraPitch);
            return;
        }
        if(afkcd$route==null)return;
        var hint=client.player==null || !DirectorClient.CONFIG.enabled || DirectorClient.foreignCamera(client)?null
                :afkcd$route.next(WorldSpace.point(cameraPosition),WorldSpace.point(client.player.getEyePosition()),DirectorClient.CONFIG.radius,new WorldSpace(client));
        if(hint==null){shotTicksRemaining=0;afkcd$holdFrame();ci.cancel();return;}
        shotBaseCamera=WorldSpace.vec(hint.camera());shotBaseFocus=WorldSpace.vec(hint.focus());
    }
    @Inject(method="enforceAboveGround",at=@At("HEAD"),cancellable=true)
    private void afkcd$keepInteriorHeight(Player player,Vec3 point,double margin,CallbackInfoReturnable<Vec3> ci){
        // Terrain height maps describe roofs, not corridor floors. Route sweeps check real blocks.
        if(afkcd$route!=null)ci.setReturnValue(point);
    }
    @Inject(method="getSubtleShake",at=@At("HEAD"),cancellable=true)
    private void afkcd$steadyCamera(double intensity,CallbackInfoReturnable<Vec3> ci){
        if(DirectorClient.CONFIG.enabled)ci.setReturnValue(Vec3.ZERO);
    }

    @Unique private void afkcd$holdFrame(){
        // Updating both interpolation endpoints prevents the last step replaying every tick.
        cameraPosition=CinematicCameraRig.getInterpolatedPosition(1);
        cameraYaw=CinematicCameraRig.getInterpolatedYaw(1);cameraPitch=CinematicCameraRig.getInterpolatedPitch(1);
        CinematicCameraRig.update(cameraPosition,cameraYaw,cameraPitch);
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
            shotTicksRemaining=0;afkcd$holdFrame();return;
        }
        if(afkcd$advised) {
            var client=Minecraft.getInstance();var space=new WorldSpace(client);Point next=WorldSpace.point(position);
            if(client.player==null || afkcd$previous==null || !DirectorClient.CONFIG.enabled
                    || next.distance(WorldSpace.point(client.player.getEyePosition()))>DirectorClient.CONFIG.radius
                    || !space.clear(afkcd$previous,next) || !space.visible(next,WorldSpace.point(focusPosition))) {
                shotTicksRemaining=0;afkcd$holdFrame();return;
            }
            afkcd$previous=next;
        }
        afkcd$lastShown=WorldSpace.point(position);
        CinematicCameraRig.update(position,yaw,pitch);
    }
    @Inject(method="stop",at=@At("TAIL"))
    private void afkcd$reset(Minecraft client,CallbackInfo ci){afkcd$advised=false;afkcd$previous=null;afkcd$route=null;afkcd$move=null;afkcd$wide=null;afkcd$heldWide=null;afkcd$lastShown=null;afkcd$rejected=false;afkcd$retry.accepted();afkcd$hasShot=false;DirectorClient.TRANSITION.reset();}
}

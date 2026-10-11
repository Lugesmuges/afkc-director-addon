package dev.creatorcinematics.director;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.*;

/** Reads only the current client world. Never requests chunks or moves the player. */
public final class WorldSpace implements ShotAdvisor.Space {
    private final Minecraft client;
    private static final double MARGIN=0.25;
    public WorldSpace(Minecraft client) { this.client=client; }
    public static Vec3 vec(Point p){return new Vec3(p.x(),p.y(),p.z());}
    public static Point point(Vec3 p){return new Point(p.x,p.y,p.z);}
    @Override public double viewDistance(){
        // The server can reduce the effective distance even when farther client chunks exist.
        return client.options.getEffectiveRenderDistance()*16*.9;
    }
    private boolean loaded(Point p) {
        if(client.level==null) return false;
        var pos=BlockPos.containing(p.x(),p.y(),p.z());
        return client.level.isInsideBuildHeight(pos.getY()) && client.level.hasChunk(pos.getX()>>4,pos.getZ()>>4);
    }
    private BlockHitResult ray(Point from,Point to) {
        return client.level.clip(new ClipContext(vec(from),vec(to),ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,client.player));
    }
    @Override public boolean clear(Point from,Point to) {
        if(client.player==null || client.level==null) return false;
        int steps=Math.max(1,(int)Math.ceil(from.distance(to)/0.4));
        for(int i=0;i<=steps;i++) {
            Point p=from.lerp(to,(double)i/steps);
            for(int dx:new int[]{-1,1}) for(int dz:new int[]{-1,1}) {
                if(!loaded(p.add(new Point(dx*MARGIN,0,dz*MARGIN)))) return false;
            }
            if(!loaded(p.add(new Point(0,MARGIN,0))) || !loaded(p.add(new Point(0,-MARGIN,0)))) return false;
            if(!client.level.getFluidState(BlockPos.containing(vec(p))).isEmpty()) return false;
            if(!client.level.noCollision(new AABB(p.x()-MARGIN,p.y()-MARGIN,p.z()-MARGIN,p.x()+MARGIN,p.y()+MARGIN,p.z()+MARGIN))) return false;
        }
        // Sweep the centre and all corners, so a thin pane between samples cannot be skipped.
        if(ray(from,to).getType()!=HitResult.Type.MISS) return false;
        for(int dx:new int[]{-1,1}) for(int dy:new int[]{-1,1}) for(int dz:new int[]{-1,1}) {
            var offset=new Point(dx*MARGIN,dy*MARGIN,dz*MARGIN);
            if(ray(from.add(offset),to.add(offset)).getType()!=HitResult.Type.MISS) return false;
        }
        return true;
    }
    @Override public boolean visible(Point from,Point focus) {
        int steps=Math.max(1,(int)Math.ceil(from.distance(focus)));
        for(int i=0;i<=steps;i++) if(!loaded(from.lerp(focus,(double)i/steps))) return false;
        var hit=ray(from,focus);
        // Edited block centres can be solid. Seeing that block's surface is sufficient.
        return hit.getType()==HitResult.Type.MISS || hit.getLocation().distanceTo(vec(focus))<=1.05;
    }
    @Override public boolean unobstructed(Point from,Point to){
        int steps=Math.max(1,(int)Math.ceil(from.distance(to)));
        for(int i=0;i<=steps;i++)if(!loaded(from.lerp(to,(double)i/steps)))return false;
        return ray(from,to).getType()==HitResult.Type.MISS;
    }
    @Override public boolean lensClear(Point from,Point to){
        if(!unobstructed(from,to))return false;
        for(var entity:client.level.getEntities((net.minecraft.world.entity.Entity)null,new AABB(vec(from),vec(to)).inflate(0.1),
                entity->entity instanceof net.minecraft.world.entity.LivingEntity && !entity.isSpectator() && !entity.isInvisible())){
            var bounds=entity.getBoundingBox().inflate(0.05);
            if(bounds.contains(vec(from)) || bounds.clip(vec(from),vec(to)).isPresent())return false;
        }
        return true;
    }
    @Override public boolean framingClear(Point camera,Point direction,double fov,double aspect){
        if(client.level==null)return false;
        if(!SceneContent.visible(direction,fov,aspect,rayDirection->{
            Point end=camera.add(rayDirection.scale(viewDistance()));
            var hit=client.level.clip(new ClipContext(vec(camera),vec(end),ClipContext.Block.COLLIDER,ClipContext.Fluid.ANY,client.player));
            return hit.getType()==HitResult.Type.BLOCK && loaded(point(hit.getLocation()));
        }))return false;
        for(var entity:client.level.getEntities((net.minecraft.world.entity.Entity)null,new AABB(vec(camera),vec(camera)).inflate(4),
                entity->entity instanceof net.minecraft.world.entity.LivingEntity && !entity.isSpectator() && !entity.isInvisible())){
            var box=entity.getBoundingBox();
            if(ray(camera,point(box.getCenter())).getType()!=HitResult.Type.MISS)continue;
            if(FrameQuality.croppedActor(camera,direction,fov,aspect,new Point(box.minX,box.minY,box.minZ),
                    new Point(box.maxX,box.maxY,box.maxZ)))return false;
        }
        return true;
    }
    @Override public Point surface(Point camera,Point focus,Point min,Point max) {
        int steps=Math.max(1,(int)Math.ceil(camera.distance(focus)));
        for(int i=0;i<=steps;i++)if(!loaded(camera.lerp(focus,(double)i/steps)))return null;
        var hit=ray(camera,focus);
        if(hit.getType()==HitResult.Type.MISS)return focus;
        Point surface=point(hit.getLocation());
        // A surface belonging to the remembered extent is a valid view of a solid building.
        // A foreground wall outside that extent must not become the new subject.
        if(surface.x()<min.x()-1 || surface.x()>max.x()+1 || surface.y()<min.y()-1 || surface.y()>max.y()+1
                ||surface.z()<min.z()-1 || surface.z()>max.z()+1)return null;
        Point towardsCamera=camera.subtract(surface);
        if(towardsCamera.length()<1.2)return null;
        return surface.add(towardsCamera.scale(1.1/towardsCamera.length()));
    }
}

package dev.creatorcinematics.director;

import com.spunkyinsaan.afkcinematics.*;
import net.fabricmc.fabric.api.client.gametest.v1.context.*;
import net.minecraft.client.Minecraft;

/** Large synthetic project fixture, never included in the released JAR. */
final class WideScene {
    private static void fill(TestServerContext s,int x,int y,int z,int a,int b,int c,String block){
        s.runCommand("fill "+x+" "+y+" "+z+" "+a+" "+b+" "+c+" minecraft:"+block);
    }
    static void setup(ClientGameTestContext context,TestServerContext server){
        server.runCommand("tp @a 0.5 65 -5.5 0 0");
        server.runCommand("weather clear");server.runCommand("time set 11000");
        server.runCommand("gamerule minecraft:advance_time false");server.runCommand("difficulty peaceful");
        for(int x=-64;x<64;x+=16)for(int z=-64;z<64;z+=16){
            fill(server,x,64,z,x+15,99,z+15,"air");
            fill(server,x,61,z,x+15,63,z+15,"water");
        }
        fill(server,-24,64,8,24,65,40,"polished_deepslate");
        fill(server,-3,64,-8,3,64,8,"spruce_planks");
        fill(server,-21,66,12,21,78,35,"waxed_weathered_cut_copper");
        fill(server,-20,66,13,20,77,34,"air");
        for(int x=-18;x<=18;x+=6){
            fill(server,x,68,12,x+3,75,12,"glass");
            fill(server,x,68,35,x+3,75,35,"glass");
        }
        fill(server,-2,66,12,2,70,12,"air");
        fill(server,-22,79,11,22,79,36,"waxed_oxidized_cut_copper");
        for(int x:new int[]{-24,24})for(int z:new int[]{8,40}){
            fill(server,x,60,z,x,68,z,"polished_deepslate");
            fill(server,x,69,z,x,69,z,"lantern");
        }
        for(int x:new int[]{10,18})for(int z:new int[]{20,28})fill(server,x,80,z,x,87,z,"iron_bars");
        fill(server,9,88,19,19,89,29,"waxed_exposed_cut_copper");
        fill(server,-16,80,23,-12,86,27,"polished_deepslate");
        fill(server,-16,87,23,-12,87,27,"campfire[lit=false]");
        server.runCommand("gamemode survival @a");
        context.waitTicks(60);
        context.runOnClient(client->{
            DirectorClient.clear();
            // Reproducible recorded building history across the whole structure, not a claim
            // that the camera can infer an unobserved building from one recent block.
            try{
                var field=DirectorClient.class.getDeclaredField("MEMORY");field.setAccessible(true);
                var memory=(ActivityMemory)field.get(null);long time=System.currentTimeMillis()/50;
                for(int x=-24;x<=24;x+=8)for(int z=8;z<=40;z+=8)
                    memory.record(new Point(x,65,z),ActivityMemory.Kind.PLACE,time);
                for(int y=65;y<=89;y+=8)memory.record(new Point(16,y,24),ActivityMemory.Kind.PLACE,time);
            }catch(ReflectiveOperationException e){throw new AssertionError(e);}
        });
    }
    static WideShot choose(Minecraft client){
        try{
            var singleton=AfkCinematicsClient.class.getDeclaredField("instance");singleton.setAccessible(true);
            var field=AfkCinematicsClient.class.getDeclaredField("cinematicDirector");field.setAccessible(true);
            Object director=field.get(singleton.get(null));
            var choose=director.getClass().getDeclaredMethod("chooseNextShot",Minecraft.class);choose.setAccessible(true);
            var wide=director.getClass().getDeclaredField("afkcd$wide");wide.setAccessible(true);
            for(int i=0;i<6;i++){
                choose.invoke(director,client);
                if(wide.get(director)!=null)return (WideShot)wide.get(director);
            }
            var memoryField=DirectorClient.class.getDeclaredField("MEMORY");memoryField.setAccessible(true);
            var memory=(ActivityMemory)memoryField.get(null);
            Point eye=WorldSpace.point(client.player.getEyePosition());
            for(var subject:memory.ranked(eye,24,System.currentTimeMillis()/50)){
                if(!subject.built())continue;
                var base=new WorldSpace(client);
                var traced=new ShotAdvisor.Space(){
                    public double viewDistance(){return base.viewDistance();}
                    public boolean clear(Point a,Point b){boolean okay=base.clear(a,b);DirectorClient.LOG.info("WIDE CLEAR {} -> {} = {}",a,b,okay);return okay;}
                    public boolean visible(Point a,Point b){return base.visible(a,b);}
                    public Point surface(Point a,Point b,Point min,Point max){Point result=base.surface(a,b,min,max);DirectorClient.LOG.info("WIDE SURFACE {} -> {} = {}",a,b,result);return result;}
                    public boolean unobstructed(Point a,Point b){return base.unobstructed(a,b);}
                    public boolean lensClear(Point a,Point b){return base.lensClear(a,b);}
                    public boolean framingClear(Point a,Point b,double f,double ratio){boolean result=base.framingClear(a,b,f,ratio);DirectorClient.LOG.info("WIDE FRAME {} fov={} result={}",a,f,result);return result;}
                };
                DirectorClient.LOG.info("WIDE REJECT subject={} eye={} view={} result={}",subject,eye,base.viewDistance(),
                        WideShot.plan(subject,eye,eye,traced,96,(double)client.getWindow().getWidth()/client.getWindow().getHeight(),0));
            }
            throw new AssertionError("Loaded 48-block project has no safe establishing shot");
        }catch(ReflectiveOperationException e){throw new AssertionError(e);}
    }
    static void checkRejectedLens(Minecraft client,WideShot shot){
        try{
            var singleton=AfkCinematicsClient.class.getDeclaredField("instance");singleton.setAccessible(true);
            var field=AfkCinematicsClient.class.getDeclaredField("cinematicDirector");field.setAccessible(true);
            Object director=field.get(singleton.get(null));
            // Exercise a wider accepted lens even if this machine found a 30-degree candidate.
            shot=new WideShot(shot.camera(),shot.focus(),shot.min(),shot.max(),55);
            var held=director.getClass().getDeclaredField("afkcd$wide");held.setAccessible(true);held.set(director,shot);
            client.options.fov().set(55);
            var sequence=director.getClass().getDeclaredField("afkcd$wideSequence");sequence.setAccessible(true);sequence.setInt(director,1);
            var begin=java.util.Arrays.stream(director.getClass().getDeclaredMethods()).filter(m->m.getName().endsWith("afkcd$begin")).findFirst().orElseThrow();begin.setAccessible(true);
            begin.invoke(director,client,new org.spongepowered.asm.mixin.injection.callback.CallbackInfo("chooseNextShot",true));
            var cut=java.util.Arrays.stream(director.getClass().getDeclaredMethods()).filter(m->m.getName().endsWith("afkcd$validateCut")).findFirst().orElseThrow();cut.setAccessible(true);
            cut.invoke(director,new net.minecraft.world.phys.Vec3(0,64,-5),0f,0f);
            if(client.options.fov().get()!=shot.fov())throw new AssertionError("Rejected cut changed the held wide lens from "+shot.fov()+" to "+client.options.fov().get());
        }catch(ReflectiveOperationException e){throw new AssertionError(e);}
    }
    static void diagnose(ClientGameTestContext context){
        try(var world=context.worldBuilder().create()){
            world.getConnection().waitForChunksDownload();setup(context,world.getServer());
            context.runOnClient(client->{client.options.pauseOnLostFocus=false;client.getConnection().sendCommand("afkc start");});context.waitTicks(30);
            WideShot shot=context.computeOnClient(WideScene::choose);
            for(int i=0;i<3;i++){
                context.waitTicks(30);
                context.runOnClient(client->{
                    var camera=client.gameRenderer.mainCamera();
                    var fog=client.gameRenderer.gameRenderState().levelRenderState.cameraRenderState.fogData;
                    DirectorClient.LOG.info("WIDE FOG effectiveChunks={} start={} end={}",client.options.getEffectiveRenderDistance(),fog.renderDistanceStart,fog.renderDistanceEnd);
                    DirectorClient.LOG.info("WIDE DIAG plan={} rig={} yaw={} pitch={} rendered={} yaw={} pitch={} forward={} FOV={}",shot,
                            CinematicCameraRig.getInterpolatedPosition(1),CinematicCameraRig.getInterpolatedYaw(1),CinematicCameraRig.getInterpolatedPitch(1),
                            camera.position(),camera.yRot(),camera.xRot(),camera.forwardVector(),camera.getFov());
                    var actual=new net.minecraft.world.phys.Vec3(camera.forwardVector()).normalize();
                    var expected=WorldSpace.vec(shot.focus()).subtract(camera.position()).normalize();
                    if(actual.dot(expected)<.999)throw new AssertionError("Rendered camera is not looking at the planned building: "+actual.dot(expected));
                });
                context.takeScreenshot("wide-diagnostic-"+i);
            }
            context.runOnClient(client->checkRejectedLens(client,shot));
        }
    }
}

package dev.creatorcinematics.director;

import com.spunkyinsaan.afkcinematics.AfkCinematicsClient;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.*;
import java.nio.file.*;

/** Reproducible recording fixture only. Never packaged in the add-on or installed on a server. */
public final class ShowcaseGameTest implements FabricClientGameTest {
    private static void command(TestServerContext server,String command){server.runCommand(command);}
    private static void fill(TestServerContext server,int x,int y,int z,int xx,int yy,int zz,String block){
        command(server,"fill "+x+" "+y+" "+z+" "+xx+" "+yy+" "+zz+" minecraft:"+block);
    }
    private static void block(TestServerContext server,int x,int y,int z,String block){fill(server,x,y,z,x,y,z,block);}
    private static void mark(Path path,String contents){try{Files.createDirectories(path.getParent());Files.writeString(path,contents);}catch(Exception e){throw new RuntimeException(e);}}
    @Override public void runTest(ClientGameTestContext context){
        String mode=System.getProperty("director.showcase");
        boolean enabled=FabricLoader.getInstance().isModLoaded("afkc_director");
        if(enabled==mode.equals("before"))throw new AssertionError("Comparison did not actually remove/load the add-on");
        Path output=Path.of(System.getProperty("director.showcase.root"),".local","showcase");
        try(var world=context.worldBuilder().create()){
            var server=world.getServer();world.getConnection().waitForChunksDownload();
            command(server,"tp @a 2.5 67 0.5");
            command(server,"gamerule minecraft:advance_time false");
            command(server,"time set 12000");command(server,"weather clear");
            command(server,"difficulty peaceful");
            // Compact industrial waterside workshop: deliberately a demo, not CraftAttack footage.
            for(int x=-40;x<40;x+=20){
                fill(server,x,60,-40,x+19,63,39,"water");
                fill(server,x,64,-40,x+19,82,39,"air");
            }
            fill(server,-2,64,-9,16,64,9,"polished_deepslate");
            fill(server,-2,65,-9,16,65,9,"spruce_planks");
            fill(server,-2,65,-9,16,65,-9,"stripped_spruce_log[axis=x]");
            fill(server,-2,65,9,16,65,9,"stripped_spruce_log[axis=x]");
            for(int x:new int[]{-2,6,16})for(int z:new int[]{-9,9}){
                fill(server,x,60,z,x,66,z,"polished_deepslate");
                block(server,x,67,z,"lantern");
            }
            // Cabin with a broad window and an open entrance.
            fill(server,7,66,-6,14,70,2,"stripped_spruce_log");
            fill(server,8,66,-5,13,69,1,"air");
            fill(server,7,67,-5,7,69,-2,"glass");
            fill(server,7,66,0,7,68,1,"air");
            fill(server,6,71,-7,15,71,3,"waxed_weathered_cut_copper");
            fill(server,7,72,-6,14,72,2,"waxed_exposed_cut_copper_slab[type=bottom]");
            block(server,10,69,-3,"lantern[hanging=true]");
            fill(server,13,66,-5,13,66,-3,"bookshelf");
            block(server,12,66,0,"crafting_table");block(server,13,66,0,"barrel");
            // Industrial tower, pipes and roof machinery.
            for(int x:new int[]{10,13})for(int z:new int[]{5,8})fill(server,x,66,z,x,75,z,"iron_bars");
            fill(server,9,73,4,14,73,9,"waxed_oxidized_cut_copper");
            fill(server,10,74,5,13,75,8,"waxed_weathered_cut_copper");
            fill(server,10,76,5,13,76,8,"waxed_exposed_cut_copper_slab");
            fill(server,11,77,6,12,78,7,"polished_deepslate_wall");
            fill(server,8,66,5,8,70,5,"waxed_cut_copper");
            fill(server,8,70,3,12,70,5,"waxed_cut_copper");
            // Cozy work corner facing the water, materials and plants.
            for(int z:new int[]{-6,5}){
                fill(server,0,66,z,2,66,z,"spruce_stairs[facing=east]");
                block(server,3,66,z,"barrel");block(server,3,67,z,"lantern");
            }
            block(server,5,66,-7,"composter");block(server,5,67,-7,"azalea");
            block(server,5,66,7,"composter");block(server,5,67,7,"flowering_azalea");
            for(int x:new int[]{-14,24,30})for(int z:new int[]{-22,23}){
                fill(server,x-4,63,z-4,x+4,64,z+4,"grass_block");
                fill(server,x,65,z,x,72,z,"spruce_log");
                fill(server,x-2,68,z-2,x+2,71,z+2,"spruce_leaves[persistent=true]");
                fill(server,x-1,72,z-1,x+1,73,z+1,"spruce_leaves[persistent=true]");
                block(server,x,74,z,"spruce_leaves[persistent=true]");
            }
            command(server,"gamemode survival @a");
            command(server,"item replace entity @a weapon.mainhand with minecraft:cut_copper 64");
            context.waitTicks(30);
            if(enabled)context.runOnClient(client->DirectorClient.clear());
            // Same real placements in both runs. The add-on observes only normal item-use events.
            for(int[] p:new int[][]{{5,-5},{5,-3},{5,3},{5,5},{3,-3},{3,3}}){
                command(server,"tp @a "+(p[0]-1.5)+" 66 "+(p[1]+0.5));context.waitTicks(6);
                context.runOnClient(client->{
                    var hit=new BlockHitResult(new Vec3(p[0]+0.5,66,p[1]+0.5),Direction.UP,new BlockPos(p[0],65,p[1]),false);
                    if(!client.gameMode.useItemOn(client.player,InteractionHand.MAIN_HAND,hit).consumesAction())throw new AssertionError("Fixture placement failed");
                });context.waitTicks(15);
            }
            command(server,"tp @a 2.5 66 0.5 -90 -8");
            if(mode.equals("wide"))WideScene.setup(context,server);
            // Start at the capture size; live test-window resizing can leave the native blit cropped.
            context.runOnClient(client->{
                client.options.pauseOnLostFocus=false;
                client.options.framerateLimit().set(60);
                client.options.inactivityFpsLimit().set(net.minecraft.client.InactivityFpsLimit.MINIMIZED);
                client.options.renderDistance().set(8);
                client.options.getSoundSourceOptionInstance(net.minecraft.sounds.SoundSource.MASTER).set(0.0);
                client.getWindow().setTitle("AFKC Showcase "+mode);
            });
            context.waitTicks(100);
            context.takeScreenshot("showcase-"+mode+"-ready");
            mark(output.resolve(mode+".ready"),"Loaded add-on="+enabled+"; same world fixture, Survival, no shader. Waiting for capture.");
            for(int i=0;i<600 && !Files.exists(output.resolve(mode+".go"));i++){
                context.runOnClient(client->AfkCinematicsClient.markInputActivity());context.waitTicks(20);
            }
            if(!Files.exists(output.resolve(mode+".go")))throw new AssertionError("Recording never started");
            context.runOnClient(client->client.getConnection().sendCommand("afkc start"));
            mark(output.resolve(mode+".started"),Long.toString(System.currentTimeMillis()));
            context.waitTicks(40);
            for(int i=0;i<53;i++){
                context.waitTicks(20);
                if(mode.equals("wide"))context.runOnClient(client->{
                    var camera=client.gameRenderer.mainCamera();
                    if(!new WorldSpace(client).framingClear(WorldSpace.point(camera.position()),WorldSpace.point(new Vec3(camera.forwardVector())),
                            camera.getFov(),(double)client.getWindow().getWidth()/client.getWindow().getHeight()))
                        throw new AssertionError("Recording contains an empty sky or obstructed scene");
                });
            }
            mark(output.resolve(mode+".done"),Long.toString(System.currentTimeMillis()));
        }
    }
}

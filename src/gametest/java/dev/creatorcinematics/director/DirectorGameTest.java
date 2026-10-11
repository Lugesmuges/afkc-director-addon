package dev.creatorcinematics.director;

import com.spunkyinsaan.afkcinematics.*;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.minecraft.core.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.*;

public final class DirectorGameTest implements FabricClientGameTest {
    private static void check(boolean result,String message){if(!result)throw new AssertionError(message);}
    private static void requestStart() {
        try {
            var field=AfkCinematicsClient.class.getDeclaredField("instance");field.setAccessible(true);
            var start=AfkCinematicsClient.class.getDeclaredMethod("requestManualStart");start.setAccessible(true);
            start.invoke(field.get(null));
        } catch(ReflectiveOperationException e) {throw new AssertionError("Original start API changed",e);}
    }
    private static void toggleFreecam() {
        try {Class.forName("net.xolt.freecam.Freecam").getMethod("toggle").invoke(null);}
        catch(ReflectiveOperationException e){throw new AssertionError("Freecam API changed",e);}
    }
    private static void selectAdvisedShot(net.minecraft.client.Minecraft client) {
        try {
            var singleton=AfkCinematicsClient.class.getDeclaredField("instance");singleton.setAccessible(true);
            var directorField=AfkCinematicsClient.class.getDeclaredField("cinematicDirector");directorField.setAccessible(true);
            Object director=directorField.get(singleton.get(null));
            var choose=director.getClass().getDeclaredMethod("chooseNextShot",net.minecraft.client.Minecraft.class);choose.setAccessible(true);
            var advised=director.getClass().getDeclaredField("afkcd$advised");advised.setAccessible(true);
            var rejected=director.getClass().getDeclaredField("afkcd$rejected");rejected.setAccessible(true);
            var moveField=director.getClass().getDeclaredField("afkcd$move");moveField.setAccessible(true);
            for(int i=0;i<24;i++){
                choose.invoke(director,client);
                var move=(CinematicMove)moveField.get(director);
                if(advised.getBoolean(director) && !rejected.getBoolean(director) && move!=null
                        && move.at(0).camera().distance(move.at(move.durationTicks()).camera())>0.3)return;
            }
            throw new AssertionError("Native presets never accepted a build framing hint");
        } catch(ReflectiveOperationException e){throw new AssertionError(e);}
    }
    private static void selectGuidedShot(net.minecraft.client.Minecraft client) {
        try {
            var singleton=AfkCinematicsClient.class.getDeclaredField("instance");singleton.setAccessible(true);
            var field=AfkCinematicsClient.class.getDeclaredField("cinematicDirector");field.setAccessible(true);
            Object director=field.get(singleton.get(null));
            var choose=director.getClass().getDeclaredMethod("chooseNextShot",net.minecraft.client.Minecraft.class);choose.setAccessible(true);
            var route=director.getClass().getDeclaredField("afkcd$route");route.setAccessible(true);
            var rejected=director.getClass().getDeclaredField("afkcd$rejected");rejected.setAccessible(true);
            for(int i=0;i<48;i++){
                choose.invoke(director,client);
                if(route.get(director)!=null && !rejected.getBoolean(director)){
                    var duration=director.getClass().getDeclaredField("currentShotDurationTicks");duration.setAccessible(true);
                    check(duration.getInt(director)>=360,"Guided interior shot is too short");
                    return;
                }
            }
            var trailField=DirectorClient.class.getDeclaredField("trail");trailField.setAccessible(true);
            WalkTrail trail=(WalkTrail)trailField.get(null);
            var samples=WalkTrail.class.getDeclaredField("points");samples.setAccessible(true);
            throw new AssertionError("No safe guided corridor shot selected; onGround="+client.player.onGround()+", eye="+client.player.getEyePosition()+", trail="+samples.get(trail));
        }catch(ReflectiveOperationException e){throw new AssertionError(e);}
    }
    private static void walkLeg(ClientGameTestContext context,float yaw,boolean xAxis){
        context.runOnClient(client->{client.player.setYRot(yaw);client.player.setXRot(0);client.options.keyUp.setDown(true);});
        try {
            for(int i=0;i<40;i++){
                if(context.computeOnClient(client->xAxis?client.player.getX():client.player.getZ())<0.8)break;
                context.waitTick();
            }
        }finally{context.runOnClient(client->client.options.keyUp.setDown(false));}
        context.waitTicks(6);
        check(context.computeOnClient(client->xAxis?client.player.getX():client.player.getZ())<0.8,"Player did not walk along the corridor leg");
    }
    @Override public void runTest(ClientGameTestContext context) {
        if(Boolean.getBoolean("director.widecheck")){WideScene.diagnose(context);return;}
        if(System.getProperty("director.showcase")!=null){new ShowcaseGameTest().runTest(context);return;}
        var regressions=new java.util.ArrayList<String>();
        context.setScreen(()->new DirectorScreen(null));
        context.takeScreenshot("director-settings");
        context.runOnClient(client->client.gui.setScreen(null));
        try(var singleplayer=context.worldBuilder().create()) {
            var server=singleplayer.getServer();
            singleplayer.getConnection().waitForChunksDownload();
            server.runCommand("tp @a 0 66 0");
            server.runCommand("fill -22 64 -22 22 64 22 minecraft:stone");
            server.runCommand("fill -22 65 -22 22 78 22 minecraft:air");
            server.runCommand("gamemode survival @a");
            server.runCommand("item replace entity @a weapon.mainhand with minecraft:oak_planks 64");
            server.runCommand("setblock 2 65 0 minecraft:lever[face=floor]");
            context.waitTicks(25);
            context.runOnClient(client->{
                DirectorClient.clear();
                client.gameMode.useItemOn(client.player,InteractionHand.MAIN_HAND,new BlockHitResult(new Vec3(2.5,65.2,0.5),Direction.UP,new BlockPos(2,65,0),false));
            });
            context.waitTicks(20);
            context.runOnClient(client->{
                if(!DirectorClient.status().contains("0 remembered areas")) regressions.add("Lever interaction was scored as block placement");
                DirectorClient.clear();
            });
            context.runOnClient(client->{
                DirectorClient.clear();
                client.player.setPermissions(net.minecraft.server.permissions.PermissionSet.NO_PERMISSIONS);
                client.getConnection().sendCommand("afkdirector off");
                check(!DirectorClient.CONFIG.enabled,"Local off command failed without permissions");
                client.getConnection().sendCommand("afkdirector on");
                check(DirectorClient.CONFIG.enabled,"Local on command failed without permissions");
                check(!client.player.getAbilities().instabuild && !client.player.getAbilities().flying,"The client test must run in normal Survival");
                var hit=new BlockHitResult(new Vec3(3.5,65,0.5),Direction.UP,new BlockPos(3,64,0),false);
                check(client.gameMode.useItemOn(client.player,InteractionHand.MAIN_HAND,hit).consumesAction(),"Placement did not succeed");
                check(client.player.getMainHandItem().getCount()==63,"Survival placement did not consume the block");
                Point close=WorldSpace.point(client.player.getEyePosition().add(0,0,-2));
                check(!FrameQuality.acceptable(close,new Point(0,0,1),37,16.0/9,new WorldSpace(client)),
                        "A large foreground player cropped by the frame was accepted");
                Point far=WorldSpace.point(client.player.getEyePosition().add(0,0,-6));
                Point direction=new Point(.47,-.1,1);direction=direction.scale(1/direction.length());
                check(!new WorldSpace(client).framingClear(far,direction,30,16.0/9),
                        "A player clipped at the frame edge beyond four blocks was accepted");
            });
            context.waitTicks(20);
            context.runOnClient(client->{
                check(DirectorClient.advise(client,new Point(-7,67,-6))!=null,"No build-oriented shot after real placement");
                client.options.getSoundSourceOptionInstance(net.minecraft.sounds.SoundSource.MUSIC).set(0.0);
                client.getConnection().sendCommand("afkc start");
            });
            context.waitTicks(40);
            context.runOnClient(client->check(CinematicCameraRig.isActive(),"Cinematic did not start"));
            context.runOnClient(client->{
                try {
                    var singleton=AfkCinematicsClient.class.getDeclaredField("instance");singleton.setAccessible(true);
                    var directorField=AfkCinematicsClient.class.getDeclaredField("cinematicDirector");directorField.setAccessible(true);
                    Object director=directorField.get(singleton.get(null));
                    var shake=director.getClass().getDeclaredMethod("getSubtleShake",double.class);shake.setAccessible(true);
                    check(((Vec3)shake.invoke(director,1.0)).lengthSqr()==0,
                            "Enabled director still adds artificial camera shake");
                    var duration=director.getClass().getDeclaredField("currentShotDurationTicks");duration.setAccessible(true);
                    int ticks=duration.getInt(director);
                    if(ticks<360 || ticks>520)regressions.add("Steady shot is too short or too long: "+ticks);
                    var credits=AfkCinematicsClient.class.getDeclaredField("creditsTicksRemaining");credits.setAccessible(true);
                    check(credits.getInt(singleton.get(null))==0,"Original on-screen credit is still enabled");
                } catch(ReflectiveOperationException e){throw new AssertionError(e);}
            });
            context.runOnClient(client->{
                try {
                    var singleton=AfkCinematicsClient.class.getDeclaredField("instance");singleton.setAccessible(true);
                    var music=AfkCinematicsClient.class.getDeclaredField("cinematicMusicActive");music.setAccessible(true);
                    var pool=AfkCinematicsClient.class.getDeclaredField("VANILLA_MUSIC_POOL");pool.setAccessible(true);
                    var play=AfkCinematicsClient.class.getDeclaredMethod("playCinematicMusic",net.minecraft.client.Minecraft.class,net.minecraft.sounds.Music.class,boolean.class);play.setAccessible(true);
                    play.invoke(singleton.get(null),client,((Object[])pool.get(null))[0],true);
                    if(music.getBoolean(singleton.get(null))) regressions.add("AFKC started music although the add-on must be silent");
                    if(client.options.getSoundSourceVolume(net.minecraft.sounds.SoundSource.MUSIC)!=0) regressions.add("AFKC changed music volume");
                } catch(ReflectiveOperationException e){throw new AssertionError(e);}
            });
            context.runOnClient(DirectorGameTest::selectAdvisedShot);
            Vec3 nativeStart=context.computeOnClient(client->CinematicCameraRig.getInterpolatedPosition(1));
            final CinematicMove move=context.computeOnClient(client->{
                try {
                    var singleton=AfkCinematicsClient.class.getDeclaredField("instance");singleton.setAccessible(true);
                    var directorField=AfkCinematicsClient.class.getDeclaredField("cinematicDirector");directorField.setAccessible(true);
                    Object director=directorField.get(singleton.get(null));
                    var path=director.getClass().getDeclaredField("afkcd$move");path.setAccessible(true);
                    return (CinematicMove)path.get(director);
                } catch(ReflectiveOperationException e){throw new AssertionError(e);}
            });
            double[] previousDistance={0};
            for(int frame=0;frame<120;frame++){
                context.waitTick();
                context.runOnClient(client->{
                    Point actual=WorldSpace.point(CinematicCameraRig.getInterpolatedPosition(1));
                    Point from=move.at(0).camera(),line=move.at(move.durationTicks()).camera().subtract(from);
                    Point delta=actual.subtract(from);
                    double along=(delta.x()*line.x()+delta.y()*line.y()+delta.z()*line.z())/(line.length()*line.length());
                    check(actual.distance(from.add(line.scale(along)))<0.00001,"Steady camera left its planned track");
                    check(along>=previousDistance[0]-0.00001,"Steady camera reversed within a shot");previousDistance[0]=along;
                    // Minecraft's lookup-table trigonometry does not return an exactly unit vector.
                    Vec3 direction=Vec3.directionFromRotation(CinematicCameraRig.getInterpolatedPitch(1),CinematicCameraRig.getInterpolatedYaw(1)).normalize();
                    Vec3 expected=WorldSpace.vec(move.at(0).focus().subtract(actual)).normalize();
                    check(direction.dot(expected)>0.99999,"Steady camera aim drifted away from the locked subject: dot="+direction.dot(expected)
                            +", direction="+direction+", expected="+expected+", camera="+actual+", focus="+move.at(0).focus());
                });
            }
            context.runOnClient(client->{
                double travelled=CinematicCameraRig.getInterpolatedPosition(1).distanceTo(nativeStart);
                double planned=move.at(119).camera().distance(move.at(0).camera());
                check(Math.abs(travelled-planned)<0.03,"Steady shot did not follow its planned travel: actual="+travelled+", planned="+planned);
            });
            TransitionScene.verify(context);
            context.runOnClient(client->{
                try {
                    var singleton=AfkCinematicsClient.class.getDeclaredField("instance");singleton.setAccessible(true);
                    var directorField=AfkCinematicsClient.class.getDeclaredField("cinematicDirector");directorField.setAccessible(true);
                    Object director=directorField.get(singleton.get(null));
                    var cut=java.util.Arrays.stream(director.getClass().getDeclaredMethods())
                            .filter(method->method.getName().endsWith("afkcd$validateCut")).findFirst().orElseThrow();cut.setAccessible(true);
                    var advised=director.getClass().getDeclaredField("afkcd$advised");advised.setAccessible(true);advised.setBoolean(director,false);
                    Vec3 shown=CinematicCameraRig.getInterpolatedPosition(1);
                    float shownYaw=CinematicCameraRig.getInterpolatedYaw(1),shownPitch=CinematicCameraRig.getInterpolatedPitch(1);
                    int radius=DirectorClient.CONFIG.radius;
                    try {
                        DirectorClient.CONFIG.radius=8;
                        cut.invoke(director,client.player.getEyePosition().add(0,5,-12),0f,20f);
                        if(CinematicCameraRig.getInterpolatedPosition(1).distanceTo(shown)>0.001)
                            regressions.add("Out-of-range native fallback was shown before being rejected");
                    } finally {
                        DirectorClient.CONFIG.radius=radius;
                        CinematicCameraRig.cutTo(shown,shownYaw,shownPitch);
                    }
                } catch(ReflectiveOperationException e){throw new AssertionError(e);}
            });
            context.runOnClient(DirectorGameTest::selectAdvisedShot);
            context.waitTicks(40);
            context.runOnClient(client->{
                try {
                    var singleton=AfkCinematicsClient.class.getDeclaredField("instance");singleton.setAccessible(true);
                    var directorField=AfkCinematicsClient.class.getDeclaredField("cinematicDirector");directorField.setAccessible(true);
                    Object director=directorField.get(singleton.get(null));
                    var cut=java.util.Arrays.stream(director.getClass().getDeclaredMethods())
                            .filter(method->method.getName().endsWith("afkcd$validateCut")).findFirst().orElseThrow();cut.setAccessible(true);
                    cut.invoke(director,new Vec3(0,64,0),0f,0f); // Actual rejected cut into the solid floor.
                    check(CinematicCameraRig.getInterpolatedPosition(0).distanceTo(CinematicCameraRig.getInterpolatedPosition(1))<1e-9,
                            "A cut rejected from inside a guided tick did not freeze immediately");
                } catch(ReflectiveOperationException e){throw new AssertionError(e);}
            });
            context.waitTick();
            context.runOnClient(client->check(CinematicCameraRig.getInterpolatedPosition(0).distanceTo(CinematicCameraRig.getInterpolatedPosition(1))<1e-9,
                    "Rejected shot replays the last movement on every render tick"));
            context.runOnClient(DirectorGameTest::selectAdvisedShot);
            context.takeScreenshot("director-build-shot");
            context.waitTicks(200);
            context.takeScreenshot("director-next-angle");
            context.runOnClient(client->AfkCinematicsClient.markInputActivity());
            context.waitTicks(15);
            context.runOnClient(client->check(!CinematicCameraRig.isActive(),"Input did not stop cinematic"));
            context.takeScreenshot("director-return-to-game");
            if(net.fabricmc.loader.api.FabricLoader.getInstance().isModLoaded("freecam")) {
                int beforeFov=context.computeOnClient(client->client.options.fov().get());
                context.runOnClient(client->requestStart());
                context.waitTicks(2);
                context.runOnClient(client->toggleFreecam());
                context.waitTicks(1);
                context.runOnClient(client->{
                    if(CinematicCameraRig.isActive() || !CameraCompatibility.active()) regressions.add("Real Freecam ownership was not respected");
                    toggleFreecam();
                });
                context.waitTicks(2);
                context.runOnClient(client->{if(client.options.fov().get()!=beforeFov) regressions.add("FOV changed after Freecam handoff");});
            }
            context.runOnClient(client->requestStart());
            context.waitTicks(2);
            context.runOnClient(client->client.setCameraEntity(new net.minecraft.world.entity.decoration.ArmorStand(client.level,0.5,66,0.5)));
            context.waitTicks(1);
            context.runOnClient(client->{
                if(CinematicCameraRig.isActive()) regressions.add("Foreign camera lost ownership during startup grace");
                client.setCameraEntity(client.player);
            });
            context.waitTicks(12);
            context.runOnClient(client->AfkCinematicsClient.markInputActivity());
            context.waitTicks(2);
            context.runOnClient(client->requestStart());
            context.waitTicks(2);
            server.runCommand("execute in minecraft:the_nether run tp @a 0.5 65 0.5");
            context.waitFor(client->client.level!=null && client.level.dimension()==net.minecraft.world.level.Level.NETHER);
            context.waitTicks(1);
            context.runOnClient(client->{
                if(CinematicCameraRig.isActive()) regressions.add("Cinematic remained active across world change");
                if(!DirectorClient.status().contains("0 remembered areas")) regressions.add("Overworld project leaked into Nether");
            });
            server.runCommand("execute in minecraft:overworld run tp @a 0.5 66 0.5");
            context.waitFor(client->client.level!=null && client.level.dimension()==net.minecraft.world.level.Level.OVERWORLD);
            context.waitTicks(15);
            context.runOnClient(client->{
                if(DirectorClient.status().contains("0 remembered areas")) regressions.add("Built project was forgotten on return to its world");
                if(DirectorClient.advise(client,new Point(-7,67,-6))==null) regressions.add("Restored build is no longer filmable");
            });
            // An existing house, not just a newly placed block, can be registered through its visible surfaces.
            server.runCommand("fill 4 65 4 16 70 16 minecraft:oak_planks hollow");
            server.runCommand("fill 3 65 7 3 68 13 minecraft:air");
            server.runCommand("fill 4 66 7 4 68 13 minecraft:glass");
            server.runCommand("tp @a 0.5 65 0.5 -45 -5");
            context.waitTicks(20);
            context.runOnClient(client->{
                DirectorClient.clear();
                check(DirectorClient.rememberView(client)>0,"Remember command did not register the existing house");
                check(DirectorClient.advise(client,new Point(-7,67,-6))!=null,"No shot of the registered house");
                requestStart();
            });
            context.waitTicks(40);
            context.runOnClient(DirectorGameTest::selectAdvisedShot);
            context.takeScreenshot("director-whole-house");
            context.runOnClient(client->AfkCinematicsClient.markInputActivity());
            context.waitTicks(12);
            // Rain adds view-context to subject choice while AFKC keeps its own varied movement.
            server.runCommand("fill -3 69 -3 2 69 3 minecraft:oak_planks");
            server.runCommand("weather rain");
            context.waitTicks(30);
            context.runOnClient(client->{
                requestStart();
            });
            context.waitTicks(60);
            context.takeScreenshot("director-cozy-rain");
            context.runOnClient(client->AfkCinematicsClient.markInputActivity());
            context.waitTicks(12);
            // A low L corridor opens into a gallery. A blind one-block dead end is deliberately
            // no longer considered a useful screensaver composition by the depth gate.
            server.runCommand("fill -4 65 -4 6 68 6 minecraft:stone_bricks");
            server.runCommand("fill -1 65 -1 1 67 3 minecraft:air");
            server.runCommand("fill 0 65 1 8 67 3 minecraft:air");
            server.runCommand("setblock 0 68 1 minecraft:sea_lantern");
            server.runCommand("setblock 1 68 2 minecraft:sea_lantern");
            server.runCommand("tp @a 2.5 65 2.5");
            context.waitTicks(12);
            context.runOnClient(client->DirectorClient.clear());
            walkLeg(context,90,true);
            walkLeg(context,180,false);
            context.waitTicks(180);
            context.runOnClient(client->{
                check(!DirectorClient.status().contains("0 remembered areas"),"Staying in the corridor was not learned automatically");
                requestStart();
            });
            context.waitTick();
            context.runOnClient(client->{
                check(CinematicCameraRig.isActive(),"No safe initial view found in the corridor/gallery");
                check(!client.player.getBoundingBox().contains(CinematicCameraRig.getInterpolatedPosition(1)),"Initial camera is inside the rendered player");
            });
            // Let the native startup FOV settle before demanding a shot in the low corridor.
            context.waitTicks(40);
            context.runOnClient(DirectorGameTest::selectGuidedShot);
            Vec3 corridorStart=context.computeOnClient(client->CinematicCameraRig.getInterpolatedPosition(1));
            context.waitTicks(2);
            context.takeScreenshot("director-corridor-start");
            for(int i=0;i<180;i++){
                context.waitTick();
                context.runOnClient(client->{
                    Vec3 camera=CinematicCameraRig.getInterpolatedPosition(1);
                    check(camera.y>65.3 && camera.y<67.7,"Guided camera was lifted through the roof: "+camera);
                    check(new WorldSpace(client).clear(WorldSpace.point(camera),WorldSpace.point(camera)),"Guided camera touched a wall");
                });
            }
            context.runOnClient(client->{
                Vec3 end=CinematicCameraRig.getInterpolatedPosition(1);
                check(end.x-corridorStart.x>0.6 && end.z-corridorStart.z>0.7,"Camera did not follow both corridor legs: "+corridorStart+" -> "+end);
            });
            context.takeScreenshot("director-corridor-corner");
            context.runOnClient(client->AfkCinematicsClient.markInputActivity());
            context.waitTicks(12);
            WindowScene.verify(context,server);
            WideScene.setup(context,server);
            int wideOriginalFov=context.computeOnClient(client->client.options.fov().get());
            context.runOnClient(client->requestStart());context.waitTicks(30);
            WideShot wide=context.computeOnClient(WideScene::choose);
            Point wideStart=context.computeOnClient(client->WorldSpace.point(CinematicCameraRig.getInterpolatedPosition(1)));
            check(wideStart.distance(context.computeOnClient(client->WorldSpace.point(client.player.getEyePosition())))>16,"Wide shot stayed in old close radius");
            for(int i=0;i<140;i++){
                context.waitTick();context.runOnClient(client->{
                    double aspect=(double)client.getWindow().getWidth()/client.getWindow().getHeight();
                    check(wide.fits(WorldSpace.point(CinematicCameraRig.getInterpolatedPosition(1)),aspect),"Large project was cropped during movement");
                    check(client.options.fov().get()==wide.fov(),"Establishing lens changed during movement");
                });
            }
            context.runOnClient(client->{
                check(wideStart.distance(WorldSpace.point(CinematicCameraRig.getInterpolatedPosition(1)))>.3,"Establishing shot did not travel");
                check(!CreatorStats.cards().isEmpty(),"Vanilla statistics response never reached the overlay");
                check(CreatorStats.cards().stream().allMatch(c->c.player().equals(client.player.getName().getString())),"Statistics belong to another player");
            });
            context.takeScreenshot("director-large-project-stats");
            context.runOnClient(client->WideScene.checkRejectedLens(client,wide));
            context.runOnClient(client->AfkCinematicsClient.markInputActivity());context.waitTicks(12);
            context.runOnClient(client->{
                check(client.options.fov().get()==wideOriginalFov,"Wide shot failed to restore game FOV");
                check(CreatorStats.view(System.nanoTime())==null,"Statistics remained after input");
            });
            // No admissible camera view: decline cleanly and wait a fresh AFK interval.
            server.runCommand("fill -18 64 -18 -12 72 -12 minecraft:stone");
            server.runCommand("fill -15 65 -15 -15 66 -15 minecraft:air");
            server.runCommand("tp @a -14.5 65 -14.5");
            context.waitTicks(15);
            int previousFov=context.computeOnClient(client->client.options.fov().get());
            boolean previousHud=context.computeOnClient(client->client.gui.hud.isHidden());
            context.runOnClient(client->requestStart());
            context.waitTicks(20);
            context.runOnClient(client->{
                check(!CinematicCameraRig.isActive(),"Sealed room started an unsafe camera");
                check(client.options.fov().get()==previousFov && client.gui.hud.isHidden()==previousHud,"Declined startup did not restore FOV/HUD");
                try {
                    var singleton=AfkCinematicsClient.class.getDeclaredField("instance");singleton.setAccessible(true);
                    for(String timer:new String[]{"inactivityTicks","suppressActivityTicks","fadeInTicksRemaining","movementGraceTicks"}){
                        var field=AfkCinematicsClient.class.getDeclaredField(timer);field.setAccessible(true);
                        int value=field.getInt(singleton.get(null));
                        check(timer.equals("inactivityTicks")?value<40:value==0,"Declined startup kept retrying or left a timer active: "+timer+"="+value);
                    }
                }catch(ReflectiveOperationException e){throw new AssertionError(e);}
            });
        }
        context.waitTicks(2);
        context.runOnClient(client->check(DirectorClient.status().contains("0 remembered areas"),"Memory survived disconnect"));
        check(regressions.isEmpty(),String.join("; ",regressions));
    }
}

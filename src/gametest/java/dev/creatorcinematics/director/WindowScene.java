package dev.creatorcinematics.director;

import net.fabricmc.fabric.api.client.gametest.v1.context.*;
import java.util.List;

/** Physical glass and transparent sight are separate contracts, exercised in real Minecraft. */
final class WindowScene {
    static void verify(ClientGameTestContext context,TestServerContext server){
        server.runCommand("fill -18 64 -18 18 75 18 minecraft:air");
        server.runCommand("fill -18 64 -18 18 64 18 minecraft:stone");
        server.runCommand("fill 5 65 -5 13 69 5 minecraft:oak_planks");
        server.runCommand("fill 6 65 -4 12 68 4 minecraft:air");
        server.runCommand("fill 5 66 -3 5 68 0 minecraft:glass");
        server.runCommand("fill 5 65 1 5 68 3 minecraft:air");
        server.runCommand("tp @a 0.5 65 0.5 -90 0");
        context.waitTicks(20);
        context.runOnClient(client->{
            Point eye=WorldSpace.point(client.player.getEyePosition()),inside=new Point(9.5,eye.y(),.5);
            var space=new WorldSpace(client,List.of(new Point(7.5,eye.y(),2.5)));
            check(space.visible(inside,eye),"Clear glass incorrectly hides the player");
            check(!space.clear(inside,eye),"Camera movement must still collide with glass");
            check(space.reachable(eye,inside),"Open doorway cannot connect a visited interior");
            check(space.windowView(inside,eye),"Covered interior through glass was not recognized");
            var shot=CharacterShot.plan(eye,new Point(1,0,0),0,16,space,true,16.0/9);
            check(shot!=null && shot.camera().x()>5,"No indoor window portrait found");
            var move=CinematicMove.plan(shot.camera(),shot.focus(),eye,16,2,space,30,16.0/9);
            check(move.at(0).camera().distance(move.at(10000).camera())>.25,"Window portrait cannot travel");
            for(int t=0;t<move.durationTicks();t+=5){
                var pose=move.at(t);
                check(space.clear(pose.camera(),pose.camera()),"Window track clips the room");
                check(space.visible(pose.camera(),pose.focus()),"Window track loses the player");
            }
        });
        server.runCommand("fill 5 65 1 5 68 3 minecraft:oak_planks");
        server.runCommand("fill 5 66 -3 5 68 0 minecraft:tinted_glass");
        context.waitTicks(10);
        context.runOnClient(client->{
            Point eye=WorldSpace.point(client.player.getEyePosition()),inside=new Point(9.5,eye.y(),.5);
            var space=new WorldSpace(client,List.of(new Point(7.5,eye.y(),2.5)));
            check(!space.visible(inside,eye),"Opaque tinted glass was treated as a clear window");
            check(!space.reachable(eye,inside),"Stale walk history passed a sealed doorway");
        });
    }
    private static void check(boolean value,String message){if(!value)throw new AssertionError(message);}
}

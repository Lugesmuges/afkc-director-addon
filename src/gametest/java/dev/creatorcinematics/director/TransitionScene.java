package dev.creatorcinematics.director;

import com.spunkyinsaan.afkcinematics.*;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.minecraft.client.Minecraft;

final class TransitionScene {
    static void verify(ClientGameTestContext context){
        for(boolean passive:new boolean[]{false,true}){
            Object old=context.computeOnClient(client->{
                try{
                    Object director=director();
                    var move=director.getClass().getDeclaredField("afkcd$move");move.setAccessible(true);
                    Object previous=move.get(director);
                    var sequence=director.getClass().getDeclaredField("afkcd$wideSequence");sequence.setAccessible(true);sequence.setInt(director,1);
                    var before=CinematicCameraRig.getInterpolatedPosition(1);
                    if(passive){
                        var refresh=director.getClass().getDeclaredMethod("refreshAfterPassiveMovement",Minecraft.class);refresh.setAccessible(true);
                        refresh.invoke(director,client);
                    }else{
                        var remaining=director.getClass().getDeclaredField("shotTicksRemaining");remaining.setAccessible(true);remaining.setInt(director,0);
                    }
                    if(CinematicCameraRig.getInterpolatedPosition(1).distanceTo(before)>1e-9)
                        throw new AssertionError("Refresh made an uncovered immediate cut");
                    return previous;
                }catch(ReflectiveOperationException e){throw new AssertionError(e);}
            });
            boolean changed=false;
            for(int i=0;i<60;i++){
                context.waitTick();
                boolean next=context.computeOnClient(client->{
                    try{
                        Object director=director();
                        var move=director.getClass().getDeclaredField("afkcd$move");move.setAccessible(true);
                        if(move.get(director)==old)return false;
                        if(DirectorClient.TRANSITION.alpha(System.nanoTime())<.98)
                            throw new AssertionError("New camera became active before the fade covered the cut");
                        return true;
                    }catch(ReflectiveOperationException e){throw new AssertionError(e);}
                });
                if(next){changed=true;break;}
            }
            if(!changed)throw new AssertionError("Fade never allowed the next camera shot");
            context.waitTicks(25);
            context.runOnClient(client->{if(DirectorClient.TRANSITION.alpha(System.nanoTime())>.01)
                throw new AssertionError("Fade did not reveal the new camera");});
        }
    }
    private static Object director()throws ReflectiveOperationException{
        var singleton=AfkCinematicsClient.class.getDeclaredField("instance");singleton.setAccessible(true);
        var field=AfkCinematicsClient.class.getDeclaredField("cinematicDirector");field.setAccessible(true);
        return field.get(singleton.get(null));
    }
}

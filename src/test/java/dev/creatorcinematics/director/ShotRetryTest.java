package dev.creatorcinematics.director;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ShotRetryTest {
    @Test void persistentlyBadRoomDoesNotReplanEveryTick() {
        var retry=new ShotRetry();int attempts=0,nextAttempt=0;
        for(int tick=0;tick<1000;tick++)if(tick>=nextAttempt){attempts++;nextAttempt=tick+retry.rejectedDelay();}
        assertTrue(attempts<=30,"At most30 scene selections in50 seconds, got "+attempts);
        retry.accepted();
        assertTrue(retry.rejectedDelay()<=5,"After recovery the next failure should retry promptly");
    }
}

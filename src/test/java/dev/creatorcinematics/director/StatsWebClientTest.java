package dev.creatorcinematics.director;

import org.junit.jupiter.api.Test;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.Flow;
import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import static org.junit.jupiter.api.Assertions.*;

class StatsWebClientTest {
    @Test void rateLimitSurvivesFailureAndAllowsOnlyOneAttemptPerFiveMinutes() {
        var gate=new StatsWebClient.RequestGate();
        assertTrue(gate.acquire(1_000_000_000L));
        assertFalse(gate.acquire(2_000_000_000L));
        assertFalse(gate.acquire(300_999_999_999L));
        assertTrue(gate.acquire(301_000_000_000L));
    }
    @Test void streamingBodyRejectsBeforeAccumulatingOversizedResponses() {
        var body=new StatsWebClient.LimitedBody(8);
        body.onSubscribe(new Flow.Subscription() {public void request(long n){} public void cancel(){}});
        body.onNext(List.of(ByteBuffer.wrap(new byte[5])));
        body.onNext(List.of(ByteBuffer.wrap(new byte[4])));
        assertTrue(body.getBody().toCompletableFuture().isCompletedExceptionally());
    }
    @Test void streamingBodyPreservesValidUtf8AcrossChunks() {
        var body=new StatsWebClient.LimitedBody(8);
        body.onSubscribe(new Flow.Subscription() {public void request(long n){} public void cancel(){}});
        body.onNext(List.of(ByteBuffer.wrap(new byte[]{65,(byte)0xc3})));
        body.onNext(List.of(ByteBuffer.wrap(new byte[]{(byte)0xb6,66})));
        body.onComplete();
        assertEquals("AöB",body.getBody().toCompletableFuture().join());
    }
    @Test void bodyDeadlineCancelsStalledTransportInsteadOfOnlyFailingFuture() throws Exception {
        var cancelled=new CountDownLatch(1);
        var body=new StatsWebClient.LimitedBody(8,Duration.ofMillis(40));
        body.onSubscribe(new Flow.Subscription() {public void request(long n){} public void cancel(){cancelled.countDown();}});
        body.onNext(List.of(ByteBuffer.wrap(new byte[]{65})));
        assertTrue(cancelled.await(2,TimeUnit.SECONDS),"A stalled body must cancel its live transport subscription");
        var failure=assertThrows(ExecutionException.class,()->body.getBody().toCompletableFuture().get(1,TimeUnit.SECONDS));
        assertInstanceOf(TimeoutException.class,failure.getCause());
    }
    @Test void subscriptionArrivingAfterDeadlineIsCancelledWithoutRequestingBytes() throws Exception {
        var body=new StatsWebClient.LimitedBody(8,Duration.ofMillis(30));
        assertThrows(ExecutionException.class,()->body.getBody().toCompletableFuture().get(1,TimeUnit.SECONDS));
        var cancelled=new AtomicBoolean();var requested=new AtomicBoolean();
        body.onSubscribe(new Flow.Subscription() {public void request(long n){requested.set(true);} public void cancel(){cancelled.set(true);}});
        assertTrue(cancelled.get());
        assertFalse(requested.get());
    }
}

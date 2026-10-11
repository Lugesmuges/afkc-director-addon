package dev.creatorcinematics.director;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.Flow;

/** Fixed public endpoint. No player name, position, or Minecraft server address is sent. */
public final class StatsWebClient {
    private static final URI ENDPOINT=URI.create("https://craftattack.one/api/stats");
    private static final int MAX_BYTES=256*1024;
    private final RequestGate gate=new RequestGate();
    private HttpClient client;

    public CompletableFuture<List<StatsData.Card>> request(String player,UUID uuid,long now) {
        if(!gate.acquire(now))return null;
        try {
            if(client==null)client=HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(4)).followRedirects(HttpClient.Redirect.NEVER).build();
            var request=HttpRequest.newBuilder(ENDPOINT).timeout(Duration.ofSeconds(8)).header("Accept","application/json").GET().build();
            // Start the total deadline before the headers arrive. The body owns cancellation;
            // timing out a dependent response future alone leaves a stalled transport alive.
            var body=new LimitedBody(MAX_BYTES);
            var response=client.sendAsync(request,info->body);
            body.getBody().whenComplete((value,error)->{if(error!=null)response.cancel(true);});
            response.whenComplete((value,error)->{if(error!=null)body.onError(error);});
            return response
                    .thenApply(value->value.statusCode()==200?StatsData.website(value.body(),player,uuid,Instant.now()):List.<StatsData.Card>of())
                    .exceptionally(error->List.of());
        } catch(RuntimeException unavailable) {
            return CompletableFuture.completedFuture(List.of());
        }
    }

    static final class RequestGate {
        private boolean attempted;
        private long last;
        boolean acquire(long now) {
            if(attempted && now-last<300_000_000_000L)return false;
            attempted=true;last=now;return true;
        }
    }
    static final class LimitedBody implements HttpResponse.BodySubscriber<String> {
        private final int limit;
        private final ByteArrayOutputStream bytes=new ByteArrayOutputStream();
        private final CompletableFuture<String> result=new CompletableFuture<>();
        private Flow.Subscription subscription;
        LimitedBody(int limit) {this(limit,Duration.ofSeconds(10));}
        LimitedBody(int limit,Duration timeout) {
            this.limit=limit;
            result.orTimeout(timeout.toNanos(),TimeUnit.NANOSECONDS)
                    .whenComplete((value,error)->{if(error!=null)cancelSubscription();});
        }
        public CompletionStage<String> getBody(){return result;}
        public synchronized void onSubscribe(Flow.Subscription value) {
            if(subscription!=null) {value.cancel();return;}
            subscription=value;
            if(result.isDone())value.cancel();else value.request(1);
        }
        public synchronized void onNext(List<ByteBuffer> buffers) {
            if(result.isDone())return;
            for(var buffer:buffers) {
                if(buffer.remaining()>limit-bytes.size()) {
                    result.completeExceptionally(new IOException("Statistics response exceeds limit"));return;
                }
                var part=new byte[buffer.remaining()];buffer.get(part);bytes.writeBytes(part);
            }
            subscription.request(1);
        }
        public synchronized void onComplete() {result.complete(bytes.toString(StandardCharsets.UTF_8));}
        public synchronized void onError(Throwable error) {result.completeExceptionally(error);}
        private synchronized void cancelSubscription() {if(subscription!=null)subscription.cancel();}
    }
}

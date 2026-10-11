package dev.creatorcinematics.director;

import java.time.Instant;
import java.util.*;

/** Client-thread-owned data; generation tokens reject callbacks from a previous world. */
public final class StatsStore {
    private long generation;
    private List<StatsData.Card> local=List.of(),website=List.of();
    public long reset(){local=website=List.of();return ++generation;}
    public long generation(){return generation;}
    public void local(List<StatsData.Card> cards) {local=List.copyOf(cards);}
    public void website(long requestGeneration,List<StatsData.Card> cards) {
        if(requestGeneration==generation)website=List.copyOf(cards);
    }
    public List<StatsData.Card> cards(Instant now,boolean webAllowed) {
        var result=new ArrayList<StatsData.Card>();
        var covered=EnumSet.noneOf(StatsData.Metric.class);
        if(webAllowed)for(var card:website)if(StatsData.fresh(card.updatedAt(),now)) {
            result.add(card);covered.add(card.metric());
        }
        for(var card:local)if(!covered.contains(card.metric()))result.add(card);
        return List.copyOf(result);
    }
}

package dev.creatorcinematics.director;

import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class StatsDataTest {
    private static final Instant NOW=Instant.parse("2026-10-11T10:00:00Z");
    private static final UUID ID=UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static String board(String fields) {
        return "{\"updatedAt\":\"2026-10-11T09:59:00Z\",\"boards\":{"+fields+"}}";
    }
    private static String entry(String name,String value) {
        return "[{\"id\":\""+name.toLowerCase(Locale.ROOT)+"\",\"name\":\""+name+"\",\"uuid\":\""+ID+"\",\"value\":"+value+"}]";
    }
    @Test void minecraftUnitsAndIdentityStaySeparateFromWebsiteDistance() {
        var cards=StatsData.local("DemoPlayer",72000,100000,200000,50000,12345,17,430,2,NOW);
        assertEquals("1 h",card(cards,StatsData.Metric.PLAYTIME).value());
        assertEquals("3,5 km",card(cards,StatsData.Metric.WALKED).value());
        assertEquals("12.345",card(cards,StatsData.Metric.BLOCKS_BROKEN).value());
        assertEquals("430",card(cards,StatsData.Metric.JUMPS).value());
        assertTrue(cards.stream().allMatch(c->c.player().equals("DemoPlayer") && c.source()==StatsData.Source.MINECRAFT));
        assertFalse(cards.stream().anyMatch(c->c.metric()==StatsData.Metric.DISTANCE));
    }
    @Test void readsExactOwnBoardEntryWithSourceTimestampAndSeconds() {
        var cards=StatsData.website(board("\"playtimeSeconds\":"+entry("CASTCRAFTER","9000")+",\"distance\":"+entry("CastCrafter","12500")),"castcrafter",null,NOW);
        assertEquals("2 h 30 min",card(cards,StatsData.Metric.PLAYTIME).value());
        assertEquals("12,5 km",card(cards,StatsData.Metric.DISTANCE).value());
        assertTrue(cards.stream().allMatch(c->c.source()==StatsData.Source.CRAFTATTACK && c.updatedAt().equals(Instant.parse("2026-10-11T09:59:00Z"))));
        assertFalse(cards.stream().anyMatch(c->c.metric()==StatsData.Metric.WALKED));
    }
    @Test void missingCreatorIsUnknownRatherThanZeroOrSomeoneElsesNumbers() {
        var json=board("\"deaths\":"+entry("OtherPlayer","42"));
        assertTrue(StatsData.website(json,"CastCrafter",null,NOW).isEmpty());
        assertEquals("42",card(StatsData.website(json,"RenamedPlayer",ID,NOW),StatsData.Metric.DEATHS).value());
        assertTrue(StatsData.website(json,"Other",null,NOW).isEmpty());
    }
    @Test void uuidConflictRejectsReusedNameButRenamedSamePlayerStillMatches() {
        var json=board("\"deaths\":"+entry("CastCrafter","42"));
        var other=UUID.fromString("22222222-2222-2222-2222-222222222222");
        assertTrue(StatsData.website(json,"CastCrafter",other,NOW).isEmpty(),"Matching name must not override a conflicting identity");
        assertEquals("42",card(StatsData.website(json,"RenamedPlayer",ID,NOW),StatsData.Metric.DEATHS).value());
        var withoutUuid=json.replace(",\"uuid\":\""+ID+"\"","");
        assertEquals("42",card(StatsData.website(withoutUuid,"castcrafter",other,NOW),StatsData.Metric.DEATHS).value());
    }
    @Test void unknownBoardsAndInvalidNumbersCannotBecomeCards() {
        String fields="\"deaths\":"+entry("CastCrafter","0")+",\"distance\":"+entry("CastCrafter","-1")+",\"mobsKilled\":"+entry("CastCrafter","\"42\"")+",\"diamonds\":"+entry("CastCrafter","1.2")+",\"playtimeSeconds\":"+entry("CastCrafter","1e100")+",\"fake\":"+entry("CastCrafter","123");
        var cards=StatsData.website(board(fields),"CastCrafter",null,NOW);
        assertEquals(1,cards.size());
        assertEquals("0",cards.getFirst().value());
    }
    @Test void staleFutureAndMalformedPayloadsAreOmitted() {
        var valid=board("\"deaths\":"+entry("CastCrafter","1"));
        for(String invalid:List.of("not json","[]","{}",valid.replace("09:59:00","09:44:59"),valid.replace("09:59:00","10:02:00")))
            assertTrue(StatsData.website(invalid,"CastCrafter",null,NOW).isEmpty(),invalid);
    }
    @Test void autoOnlyUsesCraftAttackNamedMultiplayerAndExplicitModesOverride() {
        assertTrue(StatsData.externalAllowed(0,false,"Craft Attack 14","example.org"));
        assertTrue(StatsData.externalAllowed(0,false,"Friends","play.craftattack.one"));
        assertFalse(StatsData.externalAllowed(0,true,"CraftAttack","localhost"));
        assertFalse(StatsData.externalAllowed(0,false,"Friends","example.org"));
        assertTrue(StatsData.externalAllowed(1,true,"",""));
        assertFalse(StatsData.externalAllowed(2,false,"CraftAttack","craftattack.one"));
    }
    @Test void sessionResetRejectsLateRepliesAndExpiredWebsiteKeepsMinecraft() {
        var store=new StatsStore();
        long old=store.reset();
        var web=StatsData.website(board("\"deaths\":"+entry("CastCrafter","1")),"CastCrafter",null,NOW);
        store.reset();
        store.website(old,web);
        assertTrue(store.cards(NOW,true).isEmpty());
        store.website(store.generation(),web);
        store.local(StatsData.local("CastCrafter",72000,0,0,0,10,4,8,2,NOW));
        assertEquals(StatsData.Source.CRAFTATTACK,store.cards(NOW,true).getFirst().source());
        assertTrue(store.cards(NOW.plusSeconds(1000),true).stream().allMatch(c->c.source()==StatsData.Source.MINECRAFT));
        assertTrue(store.cards(NOW,false).stream().allMatch(c->c.source()==StatsData.Source.MINECRAFT));
    }
    private static StatsData.Card card(List<StatsData.Card> cards,StatsData.Metric metric) {
        return cards.stream().filter(c->c.metric()==metric).findFirst().orElseThrow();
    }
}

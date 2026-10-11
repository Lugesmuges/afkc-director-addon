package dev.creatorcinematics.director;

import com.google.gson.*;
import java.text.NumberFormat;
import java.time.Instant;
import java.util.*;

/** Unit conversion and strict, identity-bound interpretation of the public top-ten boards. */
public final class StatsData {
    public enum Metric { PLAYTIME,WALKED,DISTANCE,BLOCKS_BROKEN,MOBS_KILLED,JUMPS,DEATHS,DIAMONDS }
    public enum Source { MINECRAFT,CRAFTATTACK }
    public record Card(Metric metric,String value,String player,Source source,Instant updatedAt) { }
    private static final Map<String,Metric> BOARDS=new LinkedHashMap<>();
    static {
        BOARDS.put("playtimeSeconds",Metric.PLAYTIME);
        BOARDS.put("distance",Metric.DISTANCE);
        BOARDS.put("blocksBroken",Metric.BLOCKS_BROKEN);
        BOARDS.put("mobsKilled",Metric.MOBS_KILLED);
        BOARDS.put("deaths",Metric.DEATHS);
        BOARDS.put("diamonds",Metric.DIAMONDS);
    }
    private StatsData() { }

    /** Only call after a real server statistics response, including in an integrated world. */
    public static List<Card> local(String player,long ticks,long walk,long sprint,long crouch,long broken,long mobs,long jumps,long deaths,Instant at) {
        return List.of(
                new Card(Metric.PLAYTIME,time(Math.max(0,ticks)/20),player,Source.MINECRAFT,at),
                new Card(Metric.WALKED,distance((Math.max(0,walk)+(double)Math.max(0,sprint)+Math.max(0,crouch))/100),player,Source.MINECRAFT,at),
                new Card(Metric.BLOCKS_BROKEN,count(broken),player,Source.MINECRAFT,at),
                new Card(Metric.MOBS_KILLED,count(mobs),player,Source.MINECRAFT,at),
                new Card(Metric.JUMPS,count(jumps),player,Source.MINECRAFT,at),
                new Card(Metric.DEATHS,count(deaths),player,Source.MINECRAFT,at));
    }

    public static List<Card> website(String json,String player,UUID uuid,Instant now) {
        if(json==null || player==null || player.isBlank())return List.of();
        try {
            var root=JsonParser.parseString(json).getAsJsonObject();
            Instant updated=Instant.parse(root.get("updatedAt").getAsString());
            if(!fresh(updated,now))return List.of();
            var boards=root.getAsJsonObject("boards");
            if(boards==null)return List.of();
            var cards=new ArrayList<Card>();
            for(var board:BOARDS.entrySet()) {
                var entries=boards.get(board.getKey());
                if(entries==null || !entries.isJsonArray())continue;
                for(var entry:entries.getAsJsonArray()) {
                    if(!entry.isJsonObject())continue;
                    var item=entry.getAsJsonObject();
                    if(!matches(item,player,uuid))continue;
                    Long value=nonnegativeInteger(item.get("value"));
                    if(value==null)continue;
                    String formatted=switch(board.getValue()) {
                        case PLAYTIME -> time(value);
                        case DISTANCE -> distance(value);
                        default -> count(value);
                    };
                    cards.add(new Card(board.getValue(),formatted,player,Source.CRAFTATTACK,updated));
                    break;
                }
            }
            return List.copyOf(cards);
        } catch(RuntimeException ignored) {
            // An unavailable or changed website schema must never interrupt a cinematic.
            return List.of();
        }
    }

    static boolean fresh(Instant updated,Instant now) {
        return !updated.isBefore(now.minusSeconds(15*60)) && !updated.isAfter(now.plusSeconds(60));
    }
    private static boolean matches(JsonObject entry,String player,UUID uuid) {
        if(uuid!=null && entry.has("uuid") && !entry.get("uuid").isJsonNull())
            return uuid.toString().equalsIgnoreCase(string(entry,"uuid"));
        return player.equalsIgnoreCase(string(entry,"name")) || player.equalsIgnoreCase(string(entry,"id"));
    }
    private static String string(JsonObject object,String key) {
        var value=object.get(key);
        return value!=null && value.isJsonPrimitive() && value.getAsJsonPrimitive().isString()?value.getAsString():"";
    }
    private static Long nonnegativeInteger(JsonElement value) {
        if(value==null || !value.isJsonPrimitive() || !value.getAsJsonPrimitive().isNumber())return null;
        try {
            long number=value.getAsBigDecimal().longValueExact();
            return number>=0?number:null;
        } catch(ArithmeticException | NumberFormatException ignored) {return null;}
    }
    private static String count(long value) {return NumberFormat.getIntegerInstance(Locale.GERMANY).format(Math.max(0,value));}
    private static String distance(double meters) {
        if(meters<1000)return count(Math.round(meters))+" m";
        var format=NumberFormat.getNumberInstance(Locale.GERMANY);
        format.setMinimumFractionDigits(1);format.setMaximumFractionDigits(1);
        return format.format(meters/1000)+" km";
    }
    private static String time(long seconds) {
        long hours=seconds/3600,minutes=seconds/60%60;
        if(hours==0)return seconds<60?seconds+" s":minutes+" min";
        return count(hours)+" h"+(minutes==0?"":" "+minutes+" min");
    }
    public static boolean externalAllowed(int mode,boolean singleplayer,String serverName,String host) {
        return mode==1 || mode==0 && !singleplayer && (craftAttack(serverName) || craftAttack(host));
    }
    private static boolean craftAttack(String value) {
        return value!=null && value.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]","").contains("craftattack");
    }
}

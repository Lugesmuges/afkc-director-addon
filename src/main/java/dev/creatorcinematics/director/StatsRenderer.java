package dev.creatorcinematics.director;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/** Compact native-font card; proportionally fits small windows regardless of GUI scale. */
final class StatsRenderer {
    private static final DateTimeFormatter STAMP=DateTimeFormatter.ofPattern("dd.MM. HH:mm").withZone(ZoneId.systemDefault());
    private StatsRenderer() { }
    static void render(Minecraft client,GuiGraphicsExtractor graphics,CreatorStats.View view) {
        float scale=.72f*Math.min(1,Math.min(graphics.guiWidth()/640f,graphics.guiHeight()/360f));
        if(scale<=0)return;
        float x=16*scale;
        int bar=Math.max(18,Math.round(graphics.guiHeight()*.11f));
        float y=graphics.guiHeight()-bar-(86+12)*scale+(float)view.animation().offsetY()*scale;
        double alpha=view.animation().alpha();
        var card=view.card();
        graphics.pose().pushMatrix();
        graphics.pose().translate(x,y).scale(scale,scale);
        graphics.fillGradient(0,0,220,86,color(alpha*.72,0x0C1118),color(alpha*.45,0x0C1118));
        graphics.fill(0,10,2,76,color(alpha,0xD2B681));
        text(client,graphics,card.player(),14,10,.82f,alpha,0xDCD6C9,192);
        text(client,graphics,card.value(),13,25,2.15f,alpha,0xFFF8E9,194);
        String metric=switch(card.metric()) {
            case PLAYTIME -> "SPIELZEIT";
            case WALKED -> "ZU FUSS UNTERWEGS";
            case DISTANCE -> "STRECKE";
            case BLOCKS_BROKEN -> "BLÖCKE ABGEBAUT";
            case MOBS_KILLED -> "KREATUREN BESIEGT";
            case JUMPS -> "SPRÜNGE";
            case DEATHS -> "TODE";
            case DIAMONDS -> "DIAMANTEN";
        };
        text(client,graphics,metric,14,51,.96f,alpha,0xF1E6D3,192);
        String source=card.source()==StatsData.Source.MINECRAFT?"Minecraft · Welt/Server":"craftattack.one";
        if(card.source()==StatsData.Source.CRAFTATTACK)source+=" · "+STAMP.format(card.updatedAt());
        text(client,graphics,source,14,70,.72f,alpha,0xAAB5BE,192);
        graphics.pose().popMatrix();
    }
    private static void text(Minecraft client,GuiGraphicsExtractor graphics,String value,float x,float y,float size,double alpha,int rgb,int maxWidth) {
        float fitted=Math.min(size,maxWidth/(float)Math.max(1,client.font.width(value)));
        graphics.pose().pushMatrix();
        graphics.pose().translate(x,y).scale(fitted,fitted);
        graphics.text(client.font,value,0,0,color(alpha,rgb),false);
        graphics.pose().popMatrix();
    }
    private static int color(double alpha,int rgb) {return (int)Math.round(Math.clamp(alpha,0,1)*255)<<24 | rgb;}
}

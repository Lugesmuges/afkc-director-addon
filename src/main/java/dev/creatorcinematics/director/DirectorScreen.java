package dev.creatorcinematics.director;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class DirectorScreen extends Screen {
    private final Screen parent;
    public DirectorScreen(Screen parent){super(Component.literal("AFKC Director Add-on"));this.parent=parent;}
    @Override protected void init() {
        int x=width/2-110,y=height/2-80;
        addRenderableWidget(Button.builder(enabledLabel(),b->{DirectorClient.CONFIG.enabled=!DirectorClient.CONFIG.enabled;DirectorClient.CONFIG.save();b.setMessage(enabledLabel());}).bounds(x,y,220,20).build());
        addRenderableWidget(Button.builder(radiusLabel(),b->{DirectorClient.CONFIG.radius=DirectorClient.CONFIG.radius==16?8:DirectorClient.CONFIG.radius+4;DirectorClient.CONFIG.save();b.setMessage(radiusLabel());}).bounds(x,y+24,220,20).build());
        addRenderableWidget(Button.builder(wideLabel(),b->{DirectorClient.CONFIG.wideShots=!DirectorClient.CONFIG.wideShots;DirectorClient.CONFIG.save();b.setMessage(wideLabel());}).bounds(x,y+48,220,20).build());
        addRenderableWidget(Button.builder(wideRadiusLabel(),b->{DirectorClient.CONFIG.wideRadius=DirectorClient.CONFIG.wideRadius>=128?32:DirectorClient.CONFIG.wideRadius+32;DirectorClient.CONFIG.save();b.setMessage(wideRadiusLabel());}).bounds(x,y+72,220,20).build());
        addRenderableWidget(Button.builder(statsLabel(),b->{DirectorClient.CONFIG.statsEnabled=!DirectorClient.CONFIG.statsEnabled;DirectorClient.CONFIG.save();b.setMessage(statsLabel());}).bounds(x,y+96,220,20).build());
        addRenderableWidget(Button.builder(webLabel(),b->{DirectorClient.CONFIG.webStatsMode=(DirectorClient.CONFIG.webStatsMode+1)%3;DirectorClient.CONFIG.save();b.setMessage(webLabel());}).bounds(x,y+120,220,20).build());
        addRenderableWidget(Button.builder(Component.translatable("afkc_director.clear"),b->DirectorClient.clear()).bounds(x,y+144,220,20).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.done"),b->onClose()).bounds(x,y+172,220,20).build());
    }
    private Component enabledLabel(){return Component.translatable("afkc_director.enabled",Component.translatable(DirectorClient.CONFIG.enabled?"options.on":"options.off"));}
    private Component radiusLabel(){return Component.translatable("afkc_director.radius",DirectorClient.CONFIG.radius);}
    private Component wideLabel(){return Component.translatable("afkc_director.wide",Component.translatable(DirectorClient.CONFIG.wideShots?"options.on":"options.off"));}
    private Component wideRadiusLabel(){return Component.translatable("afkc_director.wide_radius",DirectorClient.CONFIG.wideRadius);}
    private Component statsLabel(){return Component.translatable("afkc_director.stats",Component.translatable(DirectorClient.CONFIG.statsEnabled?"options.on":"options.off"));}
    private Component webLabel(){return Component.translatable("afkc_director.web_stats",Component.translatable(new String[]{"afkc_director.auto","options.on","options.off"}[DirectorClient.CONFIG.webStatsMode]));}
    @Override public void onClose(){if(minecraft!=null)minecraft.gui.setScreen(parent);}
    @Override public void extractRenderState(GuiGraphicsExtractor graphics,int mouseX,int mouseY,float delta) {
        super.extractRenderState(graphics,mouseX,mouseY,delta);
        graphics.text(font,title,width/2-font.width(title)/2,height/2-108,0xFFFFFFFF,true);
        var note=Component.translatable("afkc_director.note");
        graphics.text(font,note,width/2-font.width(note)/2,height/2-94,0xFFAAAAAA,true);
    }
}

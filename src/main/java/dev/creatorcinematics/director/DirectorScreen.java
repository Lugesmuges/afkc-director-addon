package dev.creatorcinematics.director;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class DirectorScreen extends Screen {
    private final Screen parent;
    public DirectorScreen(Screen parent){super(Component.literal("AFKC Director Add-on"));this.parent=parent;}
    @Override protected void init() {
        int x=width/2-110,y=height/2-38;
        addRenderableWidget(Button.builder(enabledLabel(),b->{DirectorClient.CONFIG.enabled=!DirectorClient.CONFIG.enabled;DirectorClient.CONFIG.save();b.setMessage(enabledLabel());}).bounds(x,y,220,20).build());
        addRenderableWidget(Button.builder(radiusLabel(),b->{DirectorClient.CONFIG.radius=DirectorClient.CONFIG.radius==16?8:DirectorClient.CONFIG.radius+4;DirectorClient.CONFIG.save();b.setMessage(radiusLabel());}).bounds(x,y+24,220,20).build());
        addRenderableWidget(Button.builder(Component.translatable("afkc_director.clear"),b->DirectorClient.clear()).bounds(x,y+48,220,20).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.done"),b->onClose()).bounds(x,y+80,220,20).build());
    }
    private Component enabledLabel(){return Component.translatable("afkc_director.enabled",Component.translatable(DirectorClient.CONFIG.enabled?"options.on":"options.off"));}
    private Component radiusLabel(){return Component.translatable("afkc_director.radius",DirectorClient.CONFIG.radius);}
    @Override public void onClose(){if(minecraft!=null)minecraft.gui.setScreen(parent);}
    @Override public void extractRenderState(GuiGraphicsExtractor graphics,int mouseX,int mouseY,float delta) {
        super.extractRenderState(graphics,mouseX,mouseY,delta);
        graphics.text(font,title,width/2-font.width(title)/2,height/2-70,0xFFFFFFFF,true);
        var note=Component.translatable("afkc_director.note");
        graphics.text(font,note,width/2-font.width(note)/2,height/2-54,0xFFAAAAAA,true);
    }
}

package dev.creatorcinematics.director;
import com.terraformersmc.modmenu.api.*;
public final class DirectorModMenu implements ModMenuApi {
    @Override public ConfigScreenFactory<?> getModConfigScreenFactory(){return DirectorScreen::new;}
}

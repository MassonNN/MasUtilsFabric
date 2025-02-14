package ru.massonnn.masutils.client.compatibility.modmenu;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.massonnn.masutils.client.config.MasUtilsConfigManager;

@Environment(EnvType.CLIENT)
public class ModMenuEntry implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return MasUtilsConfigManager::createGUI;
    }
}
package ru.massonnn.masutils.client.features.mineshaft;

import net.minecraft.text.Text;
import ru.massonnn.masutils.client.config.MasUtilsConfig;
import ru.massonnn.masutils.client.config.MasUtilsConfigManager;
import ru.massonnn.masutils.client.events.MineshaftEvent;
import ru.massonnn.masutils.client.hypixel.MineshaftType;
import ru.massonnn.masutils.client.utils.ModMessage;

public class MineshaftHinter implements MineshaftEvent.OnEnterMineshaft {
    @Override
    public void onEnterMineshaft(MineshaftType type) {
        MasUtilsConfig config = MasUtilsConfigManager.get();
        if (config.mineshaft.mineshaftFeaturesToggle && config.mineshaft.mineshaftProfitHint) {
            this.sendMineshaftHint(type);
        }
    }

    public void sendMineshaftHint(MineshaftType type) {
        ModMessage
                .sendModMessage(
                        Text.translatable("masutils.mineshaft.hint", type.getStringPath()));

    }
}

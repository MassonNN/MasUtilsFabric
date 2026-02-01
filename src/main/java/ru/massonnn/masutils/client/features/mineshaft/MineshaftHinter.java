package ru.massonnn.masutils.client.features.mineshaft;

import com.mojang.brigadier.Command;
import net.minecraft.text.Text;
import ru.massonnn.masutils.Masutils;
import ru.massonnn.masutils.client.config.MasUtilsConfig;
import ru.massonnn.masutils.client.config.MasUtilsConfigManager;
import ru.massonnn.masutils.client.events.MineshaftEvent;
import ru.massonnn.masutils.client.hypixel.MineshaftType;
import ru.massonnn.masutils.client.utils.CommandExecutor;
import ru.massonnn.masutils.client.utils.ModMessage;

public class MineshaftHinter implements MineshaftEvent.OnEnterMineshaft, MineshaftEvent.OnSpawnedMineshaft {
    @Override
    public void onEnterMineshaft(MineshaftType type) {
        MasUtilsConfig config = MasUtilsConfigManager.get();
        if (config.mineshaft.mineshaftFeaturesToggle && config.mineshaft.mineshaftProfitHint) {
            this.sendMineshaftHint(type);
            if (config.mineshaft.mineshaftParty.mineshaftPartyMode) {
                if (config.mineshaft.mineshaftParty.autoWarpToMineshaft) {
                    CommandExecutor.executeCommand("p warp");
                }
                CommandExecutor.sendPartyMessage(Text.translatable("masutils.mineshaft.party.entered", type.getStringPath()).getString());
            }
        }
    }

    public void sendMineshaftHint(MineshaftType type) {
        ModMessage
                .sendModMessage(
                        Text.translatable("masutils.mineshaft.hint", type.getStringPath()));

    }

    @Override
    public void onSpawnedMineshaft() {
        MasUtilsConfig config = MasUtilsConfigManager.get();
        Masutils.LOGGER.info("Options:" + config.mineshaft.mineshaftFeaturesToggle + " " + config.mineshaft.mineshaftParty.mineshaftPartyMode);
        if (config.mineshaft.mineshaftFeaturesToggle && config.mineshaft.mineshaftParty.mineshaftPartyMode) {
            CommandExecutor.sendPartyMessage(config.mineshaft.mineshaftParty.messageOnMineshaftSpawned);
        }
    }
}

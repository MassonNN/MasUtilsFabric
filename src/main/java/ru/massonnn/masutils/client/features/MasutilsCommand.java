package ru.massonnn.masutils.client.features;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.massonnn.masutils.client.config.MasUtilsConfigManager;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class MasutilsCommand {
    private static final Logger logger = LoggerFactory.getLogger(MasutilsCommand.class);

    public static void initialize() {
        logger.info("Initializing MasutilsCommand");
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(
                    literal("masutils")
                            .executes(context -> openConfig(MinecraftClient.getInstance()))
//                            .then(literal(""))
                            .then(literal("config").executes(context -> openConfig(MinecraftClient.getInstance())))
            );
        });
    }

    private static int openConfig(MinecraftClient client) {
        if (client == null)
            return 0;

        client.execute(() -> {
            client.setScreen(MasUtilsConfigManager.createGUI(null));
        });
        return 1;
    }
}

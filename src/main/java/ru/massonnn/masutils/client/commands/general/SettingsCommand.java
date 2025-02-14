package ru.massonnn.masutils.client.commands.general;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import ru.massonnn.masutils.client.commands.MasUtilsCommand;
import ru.massonnn.masutils.client.config.MasUtilsConfigManager;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class SettingsCommand implements MasUtilsCommand {
    @Override
    public void register(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
        dispatcher.register(
                literal("masutils")
                        .executes(
                        context -> {
                            FabricClientCommandSource source = context.getSource();
                            source.getClient().setScreen(
                                    MasUtilsConfigManager.createGUI(source.getClient().currentScreen)
                            );
                            return Command.SINGLE_SUCCESS;
                        }
                )
        );
    }
}

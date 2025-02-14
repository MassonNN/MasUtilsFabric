package ru.massonnn.masutils.client.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import ru.massonnn.masutils.client.commands.general.SettingsCommand;

import java.util.ArrayList;
import java.util.List;

public class CommandRegistry {
    private List<MasUtilsCommand> COMMANDS = new ArrayList<>();

    public void apply(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
        for (MasUtilsCommand command : COMMANDS) {
            command.register(dispatcher, registryAccess);
        }
    }

    public void command(MasUtilsCommand command) {
        this.COMMANDS.add(command);
    }

    @SuppressWarnings("unused")
    public void unregister(MasUtilsCommand command) {
        this.COMMANDS.remove(command);
    }

    public void init() {
        this.command(new SettingsCommand());
    }
}

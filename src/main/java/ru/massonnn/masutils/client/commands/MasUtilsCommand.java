package ru.massonnn.masutils.client.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;

public interface MasUtilsCommand {
    void register(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess registryAccess);
}

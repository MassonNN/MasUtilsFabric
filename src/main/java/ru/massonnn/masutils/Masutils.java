package ru.massonnn.masutils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.MinecraftClient;
import ru.massonnn.masutils.client.commands.CommandRegistry;

import java.nio.file.Path;

public class Masutils implements ClientModInitializer {
    // Namespace used in Fabric containers
    public static final String NAMESPACE = "masutils";

    public static final ModContainer MOD_CONTAINER = FabricLoader.getInstance().getModContainer(NAMESPACE).orElseThrow();
    public static final String VERSION = MOD_CONTAINER.getMetadata().getVersion().getFriendlyString();
    public static final Path CONFIG_DIR = FabricLoader.getInstance().getConfigDir().resolve(NAMESPACE);
    public static final Gson GSON = new GsonBuilder().create();
    private static Masutils INSTANCE;

    public static Masutils getInstance() {
        return INSTANCE;
    }

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(this::tick);
        init();
        applyCommands();
    }

    private void tick(MinecraftClient client) {
        //
        return;
    }

    private void applyCommands() {
        CommandRegistry registry = new CommandRegistry();
        registry.init();
        ClientCommandRegistrationCallback.EVENT.register(registry::apply);
    }

    private static void init() {

    }
}

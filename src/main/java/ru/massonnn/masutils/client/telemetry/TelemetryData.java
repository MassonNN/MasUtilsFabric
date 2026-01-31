package ru.massonnn.masutils.client.telemetry;

import com.google.gson.Gson;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import ru.massonnn.masutils.Masutils;
import ru.massonnn.masutils.client.config.MasUtilsConfig;
import ru.massonnn.masutils.client.config.MasUtilsConfigManager;

import java.nio.file.Path;

public class TelemetryData {
    private String modVersion;
    private String playerName;
    private String system;

    private MasUtilsConfig config;

    public void collectTelemetry() {
        this.config = MasUtilsConfigManager.get();
        this.modVersion = Masutils.VERSION;
        this.system = System.getProperty("os.name") + " " + System.getProperty("os.version") + " " + System.getProperty("os.arch");
        this.playerName = MinecraftClient.getInstance().getSession().getUsername();
    }
}

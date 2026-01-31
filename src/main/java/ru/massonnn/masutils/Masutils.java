package ru.massonnn.masutils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.massonnn.masutils.client.config.MasUtilsConfigManager;
import ru.massonnn.masutils.client.events.ChatEvent;
import ru.massonnn.masutils.client.events.LocationEvents;
import ru.massonnn.masutils.client.events.MineshaftEvent;
import ru.massonnn.masutils.client.features.MasutilsCommand;
import ru.massonnn.masutils.client.features.Party;
import ru.massonnn.masutils.client.features.PartyCommandHandler;
import ru.massonnn.masutils.client.features.mineshaft.CorpseFinder;
import ru.massonnn.masutils.client.features.mineshaft.MineshaftESP;
import ru.massonnn.masutils.client.features.mineshaft.MineshaftHinter;
import ru.massonnn.masutils.client.features.mining.GrottoFinder;
import ru.massonnn.masutils.client.features.qol.BlockHeadPlacement;
import ru.massonnn.masutils.client.features.updater.ModVersion;
import ru.massonnn.masutils.client.features.updater.UpdateChannel;
import ru.massonnn.masutils.client.features.updater.UpdateManager;
import ru.massonnn.masutils.client.features.updater.VersionInfo;
import ru.massonnn.masutils.client.hypixel.Location;
import ru.massonnn.masutils.client.hypixel.LocationUtils;
import ru.massonnn.masutils.client.hypixel.MineshaftType;
import ru.massonnn.masutils.client.telemetry.ColorAdapter;
import ru.massonnn.masutils.client.telemetry.TelemetryManager;
import ru.massonnn.masutils.client.utils.CommandExecutor;
import ru.massonnn.masutils.client.utils.MasUtilsScheduler;
import ru.massonnn.masutils.client.utils.ModMessage;
import ru.massonnn.masutils.client.utils.render.MasutilsRenderPipeline;
import ru.massonnn.masutils.client.utils.render.RenderHelper;
import ru.massonnn.masutils.client.waypoints.WaypointManager;

import java.nio.file.Path;

public class Masutils implements ClientModInitializer, ModInitializer {
    public static final String NAMESPACE = "masutils";
    public static final Logger LOGGER = LoggerFactory.getLogger("MasUtils");

    public static final ModContainer MOD_CONTAINER = FabricLoader.getInstance().getModContainer(NAMESPACE)
            .orElseThrow();
    public static final String VERSION = MOD_CONTAINER.getMetadata().getVersion().getFriendlyString();
    public static final Path CONFIG_DIR = FabricLoader.getInstance().getConfigDir().resolve(NAMESPACE);
    public static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(java.awt.Color.class, new ColorAdapter())
            .setPrettyPrinting()
            .create();
    private static Masutils INSTANCE;
    public static boolean DEBUG = true;
    private MineshaftType curMineshaft = MineshaftType.UNDEF;
    private int tickCounter = 0;

    public static Masutils getInstance() {
        return INSTANCE;
    }

    @Override
    public void onInitializeClient() {
        INSTANCE = this;
        ClientTickEvents.END_CLIENT_TICK.register(this::tick);
        init();

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            String brand = handler.getBrand();
            LocationUtils.setOnHypixel(brand != null && brand.toLowerCase().contains("hypixel"));
            WaypointManager.clearWaypoints();
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            LocationUtils.setOnHypixel(false);
            LocationUtils.setOnSkyblock(false);
            WaypointManager.clearWaypoints();
        });

        ChatEvent.RECEIVE_STRING.register(message -> {
            if (message.startsWith("{") && message.endsWith("}") && message.contains("gametype")) {
                try {
                    JsonObject json = GSON.fromJson(message, JsonObject.class);
                    if (json.has("gametype") && "SKYBLOCK".equals(json.get("gametype").getAsString())) {
                        LocationUtils.setOnSkyblock(true);
                        if (json.has("mode")) {
                            LocationUtils.setLocation(Location.from(json.get("mode").getAsString()));
                        }
                        if (json.has("server")) {
                            LocationUtils.setServer(json.get("server").getAsString());
                        }
                    }
                } catch (Exception ignored) {
                }
            }
        });

        LocationEvents.ON_LOCATION_CHANGE.register(location -> {
            if (location == Location.GLACITE_MINESHAFT) {
                processMineshaftEntry();
            } else {
                if (this.curMineshaft != MineshaftType.UNDEF) {
                    this.curMineshaft = MineshaftType.UNDEF;
                    MineshaftEvent.ON_LEAVE_MINESHAFT.invoker().onLeaveMineshaft();
                }
            }
        });

        MineshaftEvent.ON_ENTER_MINESHAFT.register(type -> new MineshaftHinter().onEnterMineshaft(type));
        MineshaftEvent.ON_LEAVE_MINESHAFT.register(() -> {
            CorpseFinder.getInstance().clearCorpses();
            WaypointManager.clearWaypoints();
        });
        MineshaftEvent.ON_SPAWNED_MINESHAFT_EVENT.register(() -> new MineshaftHinter().onSpawnedMineshaft());

        // Telemetry
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
            if(!MasUtilsConfigManager.get().dev.telemetry) return;
            TelemetryManager.sendTelemetry();
        });
        LocationEvents.JOIN.register(() -> {
            if (MasUtilsConfigManager.get().general.checkForUpdates) {
                UpdateChannel channel = MasUtilsConfigManager.get().general.updateChannel;

                switch (MasUtilsConfigManager.get().general.updateAction) {
                    case DOWNLOAD -> {
                        UpdateManager.checkAndDownload(channel);
                    }
                    case NOTIFY -> {
                        UpdateManager.check(channel).thenAccept(versionInfo -> {
                            if (versionInfo != null) {
                                MinecraftClient.getInstance().execute(() -> {
                                    ModMessage.sendModMessage(Text.translatable(
                                            "masutils.update.available",
                                            versionInfo.getVersionName()
                                    ));
                                });
                            }
                        }).exceptionally(throwable -> {
                            Masutils.LOGGER.error("Failed to check for updates", throwable);
                            return null;
                        });
                    }
                }
            }

            if (MasUtilsConfigManager.get().general.updateChannel == UpdateChannel.ALPHA && VERSION.contains("alpha")) {
                ModMessage.sendAlphaMessage("You are using alpha version of mod! Some features might not work, switch to main channel in config to download latest stable version if needed at the next game start");
            }

            if (MasUtilsConfigManager.get().general.updateChannel == UpdateChannel.BETA && VERSION.contains("beta")) {
                ModMessage.sendAlphaMessage("You are using beta version of mod! Some features might not work, switch to main channel in config to download latest stable version if needed at the next game start");
            }
        });
    }

    private void processMineshaftEntry() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world != null && this.curMineshaft == MineshaftType.UNDEF) {
            Scoreboard scb = client.world.getScoreboard();
            MineshaftType type = MineshaftType.detectByScoreboard(scb);
            if (type != MineshaftType.UNDEF) {
                this.curMineshaft = type;
                MineshaftEvent.ON_ENTER_MINESHAFT.invoker().onEnterMineshaft(type);
            }
        }
    }

    private void tick(MinecraftClient client) {
        tickCounter++;

        if (tickCounter % 20 == 0) {
            CorpseFinder.getInstance().update();
            if (client.world != null) {
                LocationUtils.detectLocationFromScoreboard(client.world.getScoreboard());

                // Active polling for mineshaft type if we are in one but type is unknown
                if (LocationUtils.getLocation() == Location.GLACITE_MINESHAFT
                        && this.curMineshaft == MineshaftType.UNDEF) {
                    processMineshaftEntry();
                }
            }
        }

        PartyCommandHandler handler = Party.getCommandHandler();
        if (handler != null) {
            if (tickCounter % 40 == 0) {
                handler.checkPing();
            }
            handler.onTimeUpdate();

            if (tickCounter % 20 == 0 && client.player != null) {
                handler.incrementBlocksIfNotInMineshaft();
            }
        }
    }

    private static void init() {
        MasutilsRenderPipeline.init();
        MasUtilsConfigManager.init();
        WaypointManager.applyHooks();
        Party.initialize();
        MasutilsCommand.initialize();
        MasUtilsScheduler.init();
        BlockHeadPlacement.init();
        MineshaftESP.init();
        RenderHelper.init();
        GrottoFinder.initialize();
        CommandExecutor.init();
    }

    @Override
    public void onInitialize() {

    }

    public void setCurrentMineshaft(MineshaftType type) {
        this.curMineshaft = type;
    }

    public MineshaftType getCurrentMineshaft() {
        return this.curMineshaft;
    }

    public static Identifier id(String path) {
        return Identifier.of(NAMESPACE, path);
    }
}

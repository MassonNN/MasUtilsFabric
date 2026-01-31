package ru.massonnn.masutils.client.features;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.EntityType;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.massonnn.masutils.Masutils;
import ru.massonnn.masutils.client.config.MasUtilsConfigManager;
import ru.massonnn.masutils.client.features.updater.UpdateManager;
import ru.massonnn.masutils.client.telemetry.TelemetryManager;
import ru.massonnn.masutils.client.utils.MasUtilsScheduler;
import ru.massonnn.masutils.client.utils.ModMessage;
import ru.massonnn.masutils.client.waypoints.Waypoint;
import ru.massonnn.masutils.client.waypoints.WaypointManager;
import ru.massonnn.masutils.client.waypoints.WaypointType;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class MasutilsCommand {
    private static final Logger logger = LoggerFactory.getLogger(MasutilsCommand.class);

    public static void initialize() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(
                    literal("masutils")
                            .executes(context -> openConfig())
//                            .then(literal(""))
                            .then(literal("config").executes(context -> openConfig()))
                            .then(literal("telemetry").executes(context -> sendTelemetry()))
                            .then(literal("versions").executes(context -> listVersions()))
                            .then(literal("waypoint").executes(context -> createWaypoint()))
            );
        });
    }

    private static int openConfig() {
        MasUtilsScheduler.schedule(MasUtilsConfigManager::openConfigScreen);
        return 1;
    }

    private static int sendTelemetry() {
        Masutils.LOGGER.info("Request to send telemetry");
        TelemetryManager.sendTelemetry();
        return 1;
    }

    private static int listVersions() {
        UpdateManager.check(MasUtilsConfigManager.get().general.updateChannel)
                .thenAccept(versionInfo -> {
                    MinecraftClient.getInstance().execute(() -> {
                        if (versionInfo != null) {
                            ModMessage.sendModMessage(
                                    Text.translatable("masutils.update.available", versionInfo.getVersionName())
                            );
                        } else {
                            ModMessage.sendModMessage(
                                    Text.translatable("masutils.update.latest", Masutils.VERSION)
                            );
                        }
                    });
                })
                .exceptionally(throwable -> {
                    throwable.printStackTrace();
                    return null;
                });

        return 1;
    }

    private static int createWaypoint() {
        MinecraftClient client = MinecraftClient.getInstance();
        WaypointManager.addWaypoint(
                new Waypoint(
                        client.player.getBlockPos(),
                        "Mineshaft",
                        MasUtilsConfigManager.get().mineshaft.mineshaftESP.mineshaftESPColor,
                        WaypointType.ESP_WITH_CURSOR_LINE,
                        MasUtilsConfigManager.get().mineshaft.traceThickness)
        );
        return 1;
    }


    private static int scanEntities() {
        MinecraftClient client = MinecraftClient.getInstance();
        client.world.getEntitiesByType(
                EntityType.ARMOR_STAND,
                client.player.getBoundingBox().expand(300),
                (entity) -> {
                    ModMessage.sendModMessage("Armor Stand | " + entity.getDisplayName().getString() + " | " + entity.getName().getString());
                    return false;
                }).forEach(entity -> {});
        return 1;
    }
}

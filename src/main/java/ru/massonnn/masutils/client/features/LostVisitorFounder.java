package ru.massonnn.masutils.client.features;

import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.EntityType;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import ru.massonnn.masutils.client.config.MasUtilsConfigManager;
import ru.massonnn.masutils.client.events.LocationEvents;
import ru.massonnn.masutils.client.events.WorldRenderExtractionCallback;
import ru.massonnn.masutils.client.hypixel.Location;
import ru.massonnn.masutils.client.utils.ModMessage;
import ru.massonnn.masutils.client.utils.render.primitive.PrimitiveCollector;

import java.awt.*;
import java.util.HashMap;

public class LostVisitorFounder {
    private static HashMap<String, BlockPos> lostVisitors = new HashMap<>();
    public static void init () {
        WorldRenderEvents.AFTER_ENTITIES.register(LostVisitorFounder::checkEntities);
        LocationEvents.ON_LOCATION_CHANGE.register(
            (newLocation, prevLocation) -> {
                    if (newLocation != Location.CRYSTAL_HOLLOWS || prevLocation == Location.CRYSTAL_HOLLOWS) {
                        lostVisitors.clear();
                    }
            }
        );
        WorldRenderExtractionCallback.EVENT.register(LostVisitorFounder::extractRendering);
    }

    public static void checkEntities(WorldRenderContext context) {
        if (!MasUtilsConfigManager.get().crystalHollowsFinder.lostVisitorsFinder) return;
        MinecraftClient client = MinecraftClient.getInstance();
        client.world.getEntitiesByType(
                EntityType.ARMOR_STAND,
                client.player.getBoundingBox().expand(400d),
                entity -> entity.getName() != null
        ).forEach(
                entity -> {
                    String name = entity.getName().getString();
                    if (name.contains("Xalx")) {
                        addLostVisitor(name, entity.getBlockPos());
                    } else if (name.contains("Pete")) {
                        addLostVisitor("Three bears", entity.getBlockPos());
                    } else if (name.contains("Chunk")) {
                        addLostVisitor(name, entity.getBlockPos());
                    }
                }
        );
    }

    public static void addLostVisitor(String name, BlockPos pos) {
        if (!lostVisitors.containsKey(name)) {
            lostVisitors.put(name, pos);
            ModMessage.sendModMessage(Text.translatable("masutils.crystalHollowsFinder.foundLostVisitor", name));
        }
    }

    public static void extractRendering(PrimitiveCollector collector) {
        lostVisitors.forEach(
                (name, blockPos) -> {
                    collector.submitOutlinedBox(
                            new Box(blockPos),
                            Color.blue.getComponents(null),
                            Color.blue.getAlpha(),
                            10f,
                            true
                    );
                    collector.submitText(
                            Text.literal(name),
                            blockPos.toCenterPos(),
                            true
                    );
                }
        );
//        MinecraftClient client = MinecraftClient.getInstance();
//        client.world.getEntitiesByType(
//                EntityType.ARMOR_STAND,
//                client.player.getBoundingBox().expand(400d),
//                entity -> entity.getName() != null
//        ).forEach(
//                entity -> {
//                    collector.submitOutlinedBox(
//                            new Box(entity.getBlockPos()),
//                            Color.blue.getComponents(null),
//                            Color.blue.getAlpha(),
//                            3f,
//                            true
//                    );
//                }
//
//        );
    }
}

package ru.massonnn.masutils.client.features.dungeons;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import ru.massonnn.masutils.client.config.MasUtilsConfigManager;
import ru.massonnn.masutils.client.events.WorldRenderExtractionCallback;
import ru.massonnn.masutils.client.hypixel.LocationUtils;
import ru.massonnn.masutils.client.utils.render.primitive.PrimitiveCollector;

import java.awt.*;

public class StarMobs {

    private static final MinecraftClient mc = MinecraftClient.getInstance();

    public static void init() {
        WorldRenderExtractionCallback.EVENT.register(StarMobs::extractRendering);
    }

    public static void extractRendering(PrimitiveCollector collector) {
        if (mc.world == null || mc.player == null) return;
        if (!MasUtilsConfigManager.get().dungeonsConfig.highlightStarredMobs) return;
        if (!LocationUtils.isInDungeons()) return;

        for (Entity entity : mc.world.getEntities()) {
            if (entity == mc.player || entity.isRemoved()) continue;

            Text customName = entity.getCustomName();
            if (customName == null) continue;

            String fullText = customName.getString();
            double height = -1;
            double yOffset = 0;

            if (fullText.contains("Lost Adventurer")) {
                height = 2.0;
            } else if (fullText.contains("Shadow Assassin")) {
                height = 1.9;
            } else if (fullText.contains("Angry Archaeologist")) {
                height = 2.0;
            }
            else if (fullText.contains("✯")) {
                height = 2.0;
                if (fullText.contains("Fels") || fullText.contains("Withermancer")) {
                    height = 3.0;
                    yOffset = -1.0;
                }
            }

            if (height != -1) {
                if (isStandAloneName(entity)) {
                    continue;
                }
                renderStarMob(collector, entity, height, yOffset);
            }
        }
    }

    private static boolean isStandAloneName(Entity nameTagEntity) {
        if (!(nameTagEntity instanceof net.minecraft.entity.decoration.ArmorStandEntity)) {
            return false;
        }

        net.minecraft.util.math.Box searchBox = nameTagEntity.getBoundingBox().expand(0.5, 2.0, 0.5).offset(0, -2.0, 0);

        return mc.world.getOtherEntities(nameTagEntity, searchBox, e ->
                e instanceof net.minecraft.entity.LivingEntity &&
                        !(e instanceof net.minecraft.entity.decoration.ArmorStandEntity) &&
                        !e.isRemoved() &&
                        e.isAlive()
        ).isEmpty();
    }

    private static void renderStarMob(PrimitiveCollector collector, Entity entity, double height, double yOffset) {
        Color color = new Color(255, 215, 0, 200);

        double r = 0.35;
        double minX = entity.getX() - r;
        double maxX = entity.getX() + r;
        double minZ = entity.getZ() - r;
        double maxZ = entity.getZ() + r;

        double currentY = entity.getY() + yOffset;

        boolean isNameTag = entity.getBoundingBox().getLengthY() < 0.5;
        double finalMinY = isNameTag ? currentY - 2.0 : currentY;
        double finalMaxY = finalMinY + height;

        net.minecraft.util.math.Box renderBox = new net.minecraft.util.math.Box(minX, finalMinY, minZ, maxX, finalMaxY, maxZ);
        collector.submitOutlinedBox(
                renderBox,
                color.getComponents(null),
                color.getAlpha(),
                2.5f,
                true
        );
    }
}
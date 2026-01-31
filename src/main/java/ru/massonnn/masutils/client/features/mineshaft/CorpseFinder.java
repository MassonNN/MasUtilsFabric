
package ru.massonnn.masutils.client.features.mineshaft;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.text.Text;
import net.minecraft.item.ItemStack;
import ru.massonnn.masutils.Masutils;
import ru.massonnn.masutils.client.config.MasUtilsConfigManager;
import ru.massonnn.masutils.client.hypixel.CorpseType;
import ru.massonnn.masutils.client.hypixel.LocationUtils;
import ru.massonnn.masutils.client.hypixel.MineshaftType;
import ru.massonnn.masutils.client.utils.ItemUtils;
import ru.massonnn.masutils.client.utils.ModMessage;
import ru.massonnn.masutils.client.waypoints.Waypoint;
import ru.massonnn.masutils.client.waypoints.WaypointManager;
import ru.massonnn.masutils.client.waypoints.WaypointType;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class CorpseFinder {
    private final List<Corpse> currentCorpses = new ArrayList<>();
    private static CorpseFinder instance;

    public static CorpseFinder getInstance() {
        if (instance == null) {
            instance = new CorpseFinder();
        }
        return instance;
    }

    public void update() {
        if (!MasUtilsConfigManager.get().mineshaft.mineshaftFeaturesToggle
                || !MasUtilsConfigManager.get().mineshaft.corpseFinder) {
            if (!currentCorpses.isEmpty()) {
                clearCorpses();
            }
            return;
        }
        if (!LocationUtils.isInDwarvenMines())
            return;
        if (Masutils.getInstance().getCurrentMineshaft() != MineshaftType.UNDEF)
            scanCorpses();
    }

    public void clearCorpses() {
        currentCorpses.clear();
        WaypointManager.clearWaypoints();
    }

    public void scanCorpses() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null || client.player == null)
            return;

        client.world.getEntitiesByType(
                EntityType.ARMOR_STAND,
                client.player.getBoundingBox().expand(300),
                (entity) -> true).forEach(entity -> {
                    String helmetId = null;
                    ItemStack helmetStack = ((ArmorStandEntity) entity).getEquippedStack(EquipmentSlot.HEAD);
                    if (helmetStack != null) {
                        helmetId = ItemUtils.getSkyblockID(helmetStack);
                    }
                    if (helmetId == null)
                        helmetId = "";
                    // Masutils.LOGGER.info("Scan entity: " + helmetId + " | " + name);

                    CorpseType type = null;
                    Color color = Color.WHITE;
                    if (!helmetId.isEmpty()) {
                        if (helmetId.contains("LAPIS_ARMOR_HELMET")) {
                            type = CorpseType.LAPIZ;
                            color = Color.BLUE;
                        } else if (helmetId.contains("MINERAL_HELMET")) {
                            type = CorpseType.TUNGSTEN;
                            color = Color.GRAY;
                        } else if (helmetId.contains("ARMOR_OF_YOG_HELMET")) {
                            type = CorpseType.UMBER;
                            color = Color.ORANGE;
                        } else if (helmetId.contains("VANGUARD_HELMET")) {
                            type = CorpseType.VANGUARD;
                            color = Color.MAGENTA;
                        }
                    }

                    if (type != null) {
                        addCorpse(new Corpse(type, entity.getBlockPos(), false), color);
                    }
                });
    }

    private void addCorpse(Corpse corpse, Color color) {
        for (Corpse curCorpse : this.currentCorpses) {
            if (curCorpse.pos.isWithinDistance(corpse.pos, 2d)) {
                return;
            }
        }
        ModMessage.sendModMessage(Text.translatable("masutils.mineshaft.foundCorpse", corpse.type.name()));
        this.currentCorpses.add(corpse);
        WaypointManager.addWaypoint(new Waypoint(corpse.pos, corpse.type.name(), color, WaypointType.ESP, 10f));
    }
}

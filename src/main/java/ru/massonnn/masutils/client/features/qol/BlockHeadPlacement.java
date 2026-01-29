package ru.massonnn.masutils.client.features.qol;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResult;
import ru.massonnn.masutils.client.config.MasUtilsConfigManager;

public class BlockHeadPlacement {
    public static void init() {
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (world.isClient() && MasUtilsConfigManager.get().qol.blockHeadPlacement) {
                ItemStack itemStack = player.getStackInHand(hand);
                if (isHeadItem(itemStack)) {
                    return ActionResult.CONSUME;
                }
            }
            return ActionResult.PASS;
        });
    }

    public static boolean isHeadItem(ItemStack itemStack) {
        return itemStack.getItem() == Items.PLAYER_HEAD ||
                itemStack.getItem() == Items.CREEPER_HEAD ||
                itemStack.getItem() == Items.DRAGON_HEAD ||
                itemStack.getItem() == Items.ZOMBIE_HEAD ||
                itemStack.getItem() == Items.SKELETON_SKULL ||
                itemStack.getItem() == Items.WITHER_SKELETON_SKULL;
    }
}

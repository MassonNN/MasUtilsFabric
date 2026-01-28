package ru.massonnn.masutils.client.utils;

import java.util.Optional;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

public class ItemUtils {
    public static Optional<NbtCompound> getCustomData(ItemStack iStack) {
        NbtComponent cmpt = iStack.get(DataComponentTypes.CUSTOM_DATA);
        if (cmpt != null)
            return Optional.of(cmpt.copyNbt());
        else {
            return Optional.empty();
        }
    }

    public static String getSkyblockID(ItemStack iStack) {
        Optional<NbtCompound> customData = getCustomData(iStack);
        if (customData.isEmpty())
            return "";
        else {
            return customData.get().getString("id", "");
        }
    }

}

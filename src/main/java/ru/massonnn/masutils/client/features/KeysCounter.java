package ru.massonnn.masutils.client.features;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;

public class KeysCounter {
    public static KeysCount countKeys() {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null || player.getInventory() == null) {
            return new KeysCount(0, 0, 0);
        }

        int tungsten = 0;
        int umber = 0;
        int vanguard = 0;

        for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (!stack.isEmpty()) {
                String name = stack.getName().getString().toLowerCase();
                int count = stack.getCount();
                
                if (name.contains("tungsten")) {
                    tungsten += count;
                } else if (name.contains("umber")) {
                    umber += count;
                } else if (name.contains("vanguard")) {
                    vanguard += count;
                }
            }
        }

        return new KeysCount(tungsten, umber, vanguard);
    }

    public static class KeysCount {
        public final int tungsten;
        public final int umber;
        public final int vanguard;

        public KeysCount(int tungsten, int umber, int vanguard) {
            this.tungsten = tungsten;
            this.umber = umber;
            this.vanguard = vanguard;
        }
    }
}

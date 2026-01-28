package ru.massonnn.masutils.client.features.mineshaft;

import net.minecraft.util.math.BlockPos;
import ru.massonnn.masutils.client.hypixel.CorpseType;

public class Corpse {
    CorpseType type;
    BlockPos pos;
    boolean claimed;

    public Corpse(CorpseType type, BlockPos pos, boolean claimed) {
        this.type = type;
        this.pos = pos;
        this.claimed = claimed;
    }

}

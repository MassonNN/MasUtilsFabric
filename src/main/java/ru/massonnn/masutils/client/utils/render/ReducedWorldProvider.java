package ru.massonnn.masutils.client.utils.render;

import net.minecraft.block.BlockState;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.math.BlockPos;

public class ReducedWorldProvider extends WorldProvider {

    @Override
    public boolean isOpaqueFullCube(int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        BlockState state = this.world.getBlockState(pos);

        boolean isException = state.isIn(BlockTags.STAIRS) || state.isIn(BlockTags.WALLS) || state.isIn(BlockTags.FENCES);

        return isException || this.world.getBlockState(pos).isOpaqueFullCube();
    }
}
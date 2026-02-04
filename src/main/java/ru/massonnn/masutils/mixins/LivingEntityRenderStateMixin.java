package ru.massonnn.masutils.mixins;

import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import ru.massonnn.masutils.client.utils.StarMobStateAccessor;

@Mixin(LivingEntityRenderState.class)
public class LivingEntityRenderStateMixin implements StarMobStateAccessor {

    @Unique
    private boolean masutils$isStarMob;

    @Override
    public void setStarMob(boolean star) {
        this.masutils$isStarMob = star;
    }

    @Override
    public boolean isStarMob() {
        return masutils$isStarMob;
    }

    @Unique private boolean masutils$hasNameTag;
    @Override public void setHasNameTag(boolean hasTag) { this.masutils$hasNameTag = hasTag; }
    @Override public boolean hasNameTag() { return masutils$hasNameTag; }
}
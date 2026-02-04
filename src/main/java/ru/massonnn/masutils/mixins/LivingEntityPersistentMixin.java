package ru.massonnn.masutils.mixins;

import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import ru.massonnn.masutils.client.utils.StarMobPersistentAccessor;

@Mixin(LivingEntity.class)
public abstract class LivingEntityPersistentMixin implements StarMobPersistentAccessor {
    @Unique private boolean masutils$isPotato;
    @Unique private boolean masutils$isStar;

    @Override public void masutils$setPotato(boolean val) { this.masutils$isPotato = val; }
    @Override public boolean masutils$isPotato() { return this.masutils$isPotato; }
    @Override public void masutils$setStar(boolean val) { this.masutils$isStar = val; }
    @Override public boolean masutils$isStar() { return this.masutils$isStar; }
}
package ru.massonnn.masutils.mixins;

import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.state.ArmorStandEntityRenderState;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.massonnn.masutils.client.config.MasUtilsConfigManager;

@Mixin(LivingEntityRenderer.class)
public class PotatoFeaturesMixin {

    @Inject(method = "shouldRenderFeatures", at = @At("HEAD"), cancellable = true)
    private void stopFeatures(LivingEntityRenderState state, CallbackInfoReturnable<Boolean> cir) {
        if (MasUtilsConfigManager.get().qol.potatoMode) {
            if (!(state instanceof PlayerEntityRenderState || state instanceof ArmorStandEntityRenderState)) {
                cir.setReturnValue(false);
            }
        }
    }
}
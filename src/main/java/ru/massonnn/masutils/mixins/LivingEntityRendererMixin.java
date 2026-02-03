package ru.massonnn.masutils.mixins;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.massonnn.masutils.client.config.MasUtilsConfigManager;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<ST extends LivingEntityRenderState> {

    @Inject(method = "getRenderLayer", at = @At("HEAD"), cancellable = true)
    private void onGetRenderLayer(ST state, boolean showBody, boolean translucent, boolean showOutline, CallbackInfoReturnable<RenderLayer> cir) {
        if (MasUtilsConfigManager.get().qol.potatoMode) {
            Identifier texture = Identifier.of("minecraft", "textures/misc/white.png");
            cir.setReturnValue(RenderLayer.getOutline(texture));
        }
    }
}
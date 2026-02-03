//package ru.massonnn.masutils.mixins;
//
//import net.minecraft.client.render.VertexConsumerProvider;
//import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer;
//import net.minecraft.client.render.entity.state.BipedEntityRenderState;
//import net.minecraft.client.util.math.MatrixStack;
//import org.spongepowered.asm.mixin.Mixin;
//import org.spongepowered.asm.mixin.injection.At;
//import org.spongepowered.asm.mixin.injection.Inject;
//import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//import ru.massonnn.masutils.client.config.MasUtilsConfigManager;
//
//@Mixin(ArmorFeatureRenderer.class)
//public class RemoveArmorMixin {
//
//    @Inject(
//            method = "render",
//            at = @At("HEAD"),
//            cancellable = true
//    )
//    private void onRenderArmor(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, BipedEntityRenderState state, float limbAngle, float limbDistance, CallbackInfo ci) {
//        if (MasUtilsConfigManager.get().qol.potatoMode) {
//            ci.cancel();
//        }
//    }
//}
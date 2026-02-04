package ru.massonnn.masutils.mixins;

import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.massonnn.masutils.client.config.MasUtilsConfigManager;

import java.util.regex.Pattern;

@Mixin(EntityRenderer.class)
public abstract class DamageIndicatorMixin {

    @Unique
    private static final Pattern DAMAGE_PATTERN = Pattern.compile(".*[0-9,✧].*");

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void hideDamageIndicators(EntityRenderState state, MatrixStack matrices, OrderedRenderCommandQueue queue, net.minecraft.client.render.state.CameraRenderState camera, CallbackInfo ci) {
        if (MasUtilsConfigManager.get().qol.hideDamageIndicator) {

            if (state.displayName != null) {
                String text = state.displayName.getString();

                if (isDamageIndicator(text)) {
                    ci.cancel();
                }
            }
        }
    }

    @Unique
    private boolean isDamageIndicator(String text) {
        String cleanText = text.replaceAll("§[0-9a-fk-or]", "");
        if (cleanText.contains("[Lvl") || cleanText.contains("❤") || cleanText.contains("✯")) {
            return false;
        }
        return DAMAGE_PATTERN.matcher(cleanText).matches();
    }
}
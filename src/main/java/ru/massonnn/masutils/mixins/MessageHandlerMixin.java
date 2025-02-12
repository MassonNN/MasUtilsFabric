package ru.massonnn.masutils.mixins;

import net.minecraft.client.network.message.MessageHandler;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.massonnn.masutils.client.events.ChatEvent;

@Mixin(value = MessageHandler.class, priority = 600) // 600 because of bypassing Fabric injections
public class MessageHandlerMixin {
    @Inject(method = "onGameMessage", at = @At("HEAD"))
    private void masutils$monitorGsameMessage(Text message, boolean overlay, CallbackInfo ci) {
        if (overlay) return;
        ChatEvent.RECEIVE_MESSAGE.invoker().onMessage(message);
        ChatEvent.RECEIVE_STRING.invoker().onMessage(message.getString());
    }
}

package ru.massonnn.masutils.client.events;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
public class ChatEvent {
    public static final Event<ChatTextEvent> RECEIVE_MESSAGE = EventFactory.createArrayBacked(
            ChatTextEvent.class,
            listeners -> message -> {
                for (ChatTextEvent listener : listeners) {
                    listener.onMessage(message);
                }
            }
    );
    public static final Event<ChatStringEvent> RECEIVE_STRING = EventFactory.createArrayBacked(
        ChatStringEvent.class,
        listeners -> message -> {
        for (ChatStringEvent listener : listeners) {
            listener.onMessage(message);
        }
    });

    @FunctionalInterface
    public interface ChatTextEvent {
        void onMessage(Text message);
    }

    @FunctionalInterface
    public interface ChatStringEvent {
        void onMessage(String message);
    }
}

package ru.massonnn.masutils.client.features;

import net.minecraft.text.Text;
import ru.massonnn.masutils.client.events.ChatEvent;

public class Party implements ChatEvent.ChatTextEvent {
    private static PartyCommandHandler commandHandler;

    public static void initialize() {
        if (commandHandler == null) {
            commandHandler = new PartyCommandHandler();
            ChatEvent.RECEIVE_STRING.register(commandHandler);
        }
    }

    public static PartyCommandHandler getCommandHandler() {
        return commandHandler;
    }

    @Override
    public void onMessage(Text message) {
    }
}

package ru.massonnn.masutils.client.features;

import net.minecraft.client.MinecraftClient;
import ru.massonnn.masutils.client.events.ChatEvent;
import ru.massonnn.masutils.client.hypixel.HypixelManager;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ru.massonnn.masutils.client.events.PartyEvent.PARTY_FINDER_JOINED;

public class Party implements ChatEvent.ChatStringEvent {
    private static PartyCommandHandler commandHandler;

    private static final Pattern DUNGEONS_JOIN_PATTERN = Pattern.compile(
            "Party Finder > (\\w+) joined the dungeon group! \\((\\w+) Level (\\d+)\\)"
    );

    public static void initialize() {
        if (commandHandler == null) {
            commandHandler = new PartyCommandHandler();

            Party partyInstance = new Party();
            ChatEvent.RECEIVE_STRING.register(partyInstance);
            ChatEvent.RECEIVE_STRING.register(commandHandler);
        }
    }

    @Override
    public void onMessage(String message) {
        String cleanMessage = message.replaceAll("§.", "");
        Matcher matcher = DUNGEONS_JOIN_PATTERN.matcher(cleanMessage);

        if (matcher.find()) {
            PARTY_FINDER_JOINED.invoker().onPartyFinderJoined(matcher.group(1), matcher.group(2));
        }
    }

    public static PartyCommandHandler getCommandHandler() {
        return commandHandler;
    }
}
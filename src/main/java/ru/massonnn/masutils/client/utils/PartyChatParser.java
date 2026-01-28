package ru.massonnn.masutils.client.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PartyChatParser {
    private static final Pattern PARTY_MESSAGE_PATTERN = Pattern.compile(
            ".*Party.*>\\s*([^:]+?)\\s*:\\s*(.*)"
    );

    public static PartyMessage parsePartyMessage(String message) {
        String plainMessage = message.replaceAll("§[0-9a-fk-or]", "");
        Matcher matcher = PARTY_MESSAGE_PATTERN.matcher(plainMessage);
        
        if (matcher.find()) {
            String playerName = matcher.group(1).trim();
            String messageText = matcher.group(2).trim();
            return new PartyMessage(playerName, messageText);
        }
        
        return null;
    }

    public static class PartyMessage {
        public final String playerName;
        public final String message;

        public PartyMessage(String playerName, String message) {
            this.playerName = playerName;
            this.message = message;
        }
    }
}

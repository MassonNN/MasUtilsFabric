package ru.massonnn.masutils.client.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PartyChatParser {
    private static final Pattern PARTY_MESSAGE_PATTERN = Pattern.compile(
            ".*Party.*>\\s*(?:\\[(.*?)\\]\\s+)?([^:]+?)\\s*:\\s*(.*)"
    );

    public static PartyMessage parsePartyMessage(String message) {
        String plainMessage = message.replaceAll("§[0-9a-fk-or]", "");
        Matcher matcher = PARTY_MESSAGE_PATTERN.matcher(plainMessage);
        
        if (matcher.find()) {
            String prefix = matcher.group(1).trim();
            String playerName = matcher.group(2).trim();
            String messageText = matcher.group(3).trim();
            return new PartyMessage(prefix, playerName, messageText);
        }
        
        return null;
    }

    public static class PartyMessage {
        public final String playerName;
        public final String message;
        public final String prefix;

        public PartyMessage(String prefix, String playerName, String message) {
            this.prefix = prefix;
            this.playerName = playerName;
            this.message = message;
        }
    }
}

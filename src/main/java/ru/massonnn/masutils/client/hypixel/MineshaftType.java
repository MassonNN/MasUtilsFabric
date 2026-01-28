package ru.massonnn.masutils.client.hypixel;

import net.minecraft.client.MinecraftClient;
import net.minecraft.scoreboard.*;

import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import ru.massonnn.masutils.Masutils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public enum MineshaftType {
    TOPA1,
    SAPP1,
    AMET1,
    AMBE1,
    JADE1,
    TITA1,
    UMBE1,
    TUNG1,
    FAIR1,
    JASP1,
    JASP2,
    OPAL1,
    OPAL2,
    RUBY1,
    ONYX1,
    ONYX2,
    AQUA1,
    AQUA2,
    CITR1,
    CITR2,
    UNDEF;

    public String getStringPath() {
        return Text.translatable("masutils.mineshaft.types." + this.name().toUpperCase()).getString();
    }

    public static MineshaftType detectByScoreboard(Scoreboard scb) {
        if (scb == null) {
            return UNDEF;
        }

        List<String> sidebarLines = fetchSidebarLines(scb);

        if (!sidebarLines.isEmpty()) {
            String firstLine = sidebarLines.get(0);
            MineshaftType type = extractMineshaftTypeFromLine(firstLine);
            if (type != UNDEF) {
                return type;
            }
        }

        for (String line : sidebarLines) {
            MineshaftType type = extractMineshaftTypeFromLine(line);
            if (type != UNDEF) {
                return type;
            }
        }

        return UNDEF;
    }

    public static List<String> fetchSidebarLines(Scoreboard scoreboard) {
        List<String> lines = new ArrayList<>();

        try {
            ScoreboardObjective objective = scoreboard.getObjectiveForSlot(ScoreboardDisplaySlot.SIDEBAR);
            if (objective == null)
                return lines;

            Collection<ScoreboardEntry> entries = scoreboard.getScoreboardEntries(objective);

            List<ScoreboardEntry> sortedEntries = new ArrayList<>(entries);
            sortedEntries.sort(Comparator.comparingInt(ScoreboardEntry::value).reversed());

            for (ScoreboardEntry entry : sortedEntries) {
                String ownerName = entry.owner();
                Team team = scoreboard.getScoreHolderTeam(ownerName);

                if (team != null) {
                    MutableText fullLine = Text.empty();
                    fullLine.append(team.getPrefix());
                    if (!ownerName.startsWith("§") || ownerName.length() > 2) {
                        fullLine.append(Text.literal(ownerName));
                    }

                    fullLine.append(team.getSuffix());
                    lines.add(fullLine.getString());
                } else {
                    lines.add(ownerName);
                }
            }

        } catch (Exception e) {
            Masutils.LOGGER.warn("[DETECT] Error fetching sidebar lines: {}", e.getMessage());
        }

        return lines;
    }

    /**
     * Extract mineshaft type from a line like "27/02/2026 m62F CITR_1"
     */
    public static MineshaftType extractMineshaftTypeFromLine(String line) {
        if (line == null || line.isEmpty()) {
            return UNDEF;
        }

        String clean = stripColorCodes(line).toUpperCase().trim();
        String[] parts = clean.split("\\s+");

        for (int i = parts.length - 1; i >= 0; i--) {
            String part = parts[i];

            for (MineshaftType type : MineshaftType.values()) {
                if (type == UNDEF)
                    continue;

                if (part.equals(type.name())) {
                    return type;
                }

                if (part.replace("_C", "2").equals(type.name()))
                    return type;

                String withUnderscore = type.name().replaceFirst("(\\d+)$", "_$1");
                if (part.equals(withUnderscore)) {
                    return type;
                }

                if (part.contains(type.name())) {
                    return type;
                }
            }
        }
        return UNDEF;
    }

    public static boolean isInMineshaft() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null)
            return false;
        if (client.world == null)
            return false;
        Scoreboard scb = client.world.getScoreboard();
        if (scb == null)
            return false;
        return detectByScoreboard(scb) != UNDEF;
    }

    public static String stripColorCodes(String s) {
        if (s == null)
            return "";
        return s.replaceAll("(?i)§[0-9A-FK-ORX]", "");
    }

}

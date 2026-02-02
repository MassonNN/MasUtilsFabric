package ru.massonnn.masutils.client.hypixel;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.scoreboard.ScoreboardObjective;
import org.jetbrains.annotations.NotNull;
import ru.massonnn.masutils.client.events.LocationEvents;

import java.util.List;

public class LocationUtils {
    private static boolean isOnHypixel = false;
    private static boolean isOnSkyblock = false;

    @NotNull
    private static Location location = Location.UNKNOWN;

    @NotNull
    private static String server = "";

    public static boolean isOnHypixel() {
        return isOnHypixel;
    }

    public static void setOnHypixel(boolean onHypixel) {
        isOnHypixel = onHypixel;
    }

    public static boolean isOnSkyblock() {
        return isOnSkyblock;
    }

    public static void setOnSkyblock(boolean onSkyblock) {
        if (isOnSkyblock != onSkyblock) {
            isOnSkyblock = onSkyblock;
            if (isOnSkyblock) {
                LocationEvents.JOIN.invoker().onSkyblockJoin();
            } else {
                LocationEvents.LEAVE.invoker().onSkyblockLeave();
                setLocation(Location.UNKNOWN);
            }
        }
    }

    public static void detectLocationFromTab() {
        if (!isOnSkyblock)
            return;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.getNetworkHandler() == null)
            return;

        for (PlayerListEntry entry : client.getNetworkHandler().getPlayerList()) {
            if (entry.getDisplayName() == null)
                continue;
            String clean = MineshaftType.stripColorCodes(entry.getDisplayName().getString());

            if (clean.contains("Area:") || clean.contains("⏣")) {
                String potentialName = clean.toLowerCase();
                if (processLocationMatch(potentialName))
                    return;
            }
            if (clean.contains("Server:")) {
                setServer(clean.replace("Server:", "").trim());
            }
        }
    }

    private static boolean processLocationMatch(String text) {
        if (text.contains("hub") && !text.contains("dungeon"))
            setLocation(Location.HUB);
        else if (text.contains("private island"))
            setLocation(Location.PRIVATE_ISLAND);
        else if (text.contains("garden"))
            setLocation(Location.GARDEN);
        else if (text.contains("dwarven mines"))
            setLocation(Location.DWARVEN_MINES);
        else if (text.contains("crystal hollows"))
            setLocation(Location.CRYSTAL_HOLLOWS);
        else if (text.contains("mineshaft"))
            setLocation(Location.GLACITE_MINESHAFT);
        else if (text.contains("crimson isle"))
            setLocation(Location.CRIMSON_ISLE);
        else if (text.contains("the end"))
            setLocation(Location.THE_END);
        else if (text.contains("spider's den"))
            setLocation(Location.SPIDERS_DEN);
        else if (text.contains("park"))
            setLocation(Location.THE_PARK);
        else if (text.contains("deep caverns"))
            setLocation(Location.DEEP_CAVERNS);
        else if (text.contains("gold mine"))
            setLocation(Location.GOLD_MINE);
        else if (text.contains("dungeon hub"))
            setLocation(Location.DUNGEON_HUB);
        else if (text.contains("dungeon") || text.contains("catacomb"))
            setLocation(Location.DUNGEON);
        else if (text.contains("kuudra"))
            setLocation(Location.KUUDRAS_HOLLOW);
        else if (text.contains("rift"))
            setLocation(Location.THE_RIFT);
        else
            return false;
        return true;
    }

    public static void detectLocationFromScoreboard(Scoreboard scb) {
        if (scb == null)
            return;
        ScoreboardObjective sidebar = scb.getObjectiveForSlot(ScoreboardDisplaySlot.SIDEBAR);
        if (sidebar != null) {
            String title = MineshaftType.stripColorCodes(sidebar.getDisplayName().getString());
            setOnSkyblock(title.contains("SKYBLOCK"));
        } else {
            return;
        }

        if (isOnSkyblock()) {
            detectLocationFromTab();

            List<String> lines = MineshaftType.fetchSidebarLines(scb);
            boolean areaDetected = false;
            for (String line : lines) {
                String clean = MineshaftType.stripColorCodes(line);
                if (clean.contains("Area:") || clean.contains("⏣") || clean.contains("District:")) {
                    if (processLocationMatch(clean.toLowerCase())) {
                        areaDetected = true;
                    }
                }
            }

            if (!areaDetected && location != Location.GLACITE_MINESHAFT) {
                for (String line : lines) {
                    if (MineshaftType.extractMineshaftTypeFromLine(line) != MineshaftType.UNDEF) {
                        setLocation(Location.GLACITE_MINESHAFT);
                        break;
                    }
                }
            }
        }
    }

    public static boolean isInDungeons() {
        return location == Location.DUNGEON;
    }

    public static boolean isInCrystalHollows() {
        return location == Location.CRYSTAL_HOLLOWS;
    }

    public static boolean isInDwarvenMines() {
        return location == Location.DWARVEN_MINES || location == Location.GLACITE_MINESHAFT;
    }

    public static boolean isInTheRift() {
        return location == Location.THE_RIFT;
    }

    /**
     * @return if the player is in the end island
     */
    public static boolean isInTheEnd() {
        return location == Location.THE_END;
    }

    public static boolean isInKuudra() {
        return location == Location.KUUDRAS_HOLLOW;
    }

    public static boolean isInCrimson() {
        return location == Location.CRIMSON_ISLE;
    }

    public static boolean isInModernForagingIsland() {
        return location == Location.MODERN_FORAGING_ISLAND;
    }

    @NotNull
    public static Location getLocation() {
        return location;
    }

    public static void setLocation(@NotNull Location newLocation) {
        if (location != newLocation) {
            LocationEvents.ON_LOCATION_CHANGE.invoker().onLocationChange(newLocation, location);
            location = newLocation;
        }
    }

    @NotNull
    public static String getServer() {
        return server;
    }

    public static void setServer(@NotNull String newServer) {
        server = newServer;
    }

}

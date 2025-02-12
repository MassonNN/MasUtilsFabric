package ru.massonnn.masutils.client.config;

import dev.isxander.yacl3.config.v2.api.SerialEntry;

import java.awt.*;

public class Mineshaft {
    @SerialEntry
    public boolean mineshaftFeaturesToggle = true;

    @SerialEntry
    public boolean corpseFinder = true;

    @SerialEntry
    public boolean mineshaftProfitHint = true;

    @SerialEntry
    public MineshaftParty mineshaftParty = new MineshaftParty();

    public static class MineshaftParty {
        @SerialEntry
        public boolean mineshaftPartyMode = false;

        @SerialEntry
        public boolean autoWarpToMineshaft = false;

        @SerialEntry
        public boolean autoTransferPartyOnMineshaft = false;

        @SerialEntry
        public String messageOnMineshaftSpawned = "!ptme Found mineshaft!";
    }

    @SerialEntry
    public MineshaftESP mineshaftESP = new MineshaftESP();

    public static class MineshaftESP {
        @SerialEntry
        public boolean createWaypointToMineshaft = false;

        @SerialEntry
        public Color mineshaftESPColor = Color.CYAN;
    }

}

package ru.massonnn.masutils.client.config;

import java.awt.*;

public class Mineshaft {
    public boolean mineshaftFeaturesToggle = true;

    public boolean corpseFinder = true;

    public boolean mineshaftProfitHint = true;

    public MineshaftParty mineshaftParty = new MineshaftParty();

    public static class MineshaftParty {
        public boolean mineshaftPartyMode = false;

        public boolean autoWarpToMineshaft = false;

        public boolean autoTransferPartyOnMineshaft = false;

        public String messageOnMineshaftSpawned = "!ptme Found mineshaft!";
    }

    public MineshaftESP mineshaftESP = new MineshaftESP();

    public static class MineshaftESP {
        public boolean createWaypointToMineshaft = false;

        public Color mineshaftESPColor = Color.CYAN;
    }

    public boolean mineshaftCommands = false;

    public int traceThickness = 4;
}

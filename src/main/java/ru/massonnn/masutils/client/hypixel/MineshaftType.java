package ru.massonnn.masutils.client.hypixel;

import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.text.Text;

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

    public Text getStringPath() {
        return Text.translatable("masutils.mineshaft.types." + this.toString().toLowerCase());
    }

    public static MineshaftType detectByScoreboard(Scoreboard scb) {
        for (String line : scb.getObjectiveNames()) {
            for (MineshaftType type : MineshaftType.values()) {
                if (line.contains(type.name())) return type;
            }
        }
        return MineshaftType.UNDEF;
    }
}

package ru.massonnn.masutils.client.hypixel.model;

import com.google.gson.annotations.SerializedName;

public record ItemData(
        @SerializedName("terminator") boolean hasTerminator,
        @SerializedName("hyperion") boolean hasHyperion,
        @SerializedName("wither_impact") boolean hasWitherImpact,
        @SerializedName("gyrokinetic_wand") boolean hasGyroWand,
        @SerializedName("giant_sword") boolean hasGiantSword,
        @SerializedName("midas_sword") boolean hasMidasSword
) {}
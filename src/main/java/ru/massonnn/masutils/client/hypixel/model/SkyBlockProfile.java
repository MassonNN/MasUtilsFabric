package ru.massonnn.masutils.client.hypixel.model;

import com.google.gson.annotations.SerializedName;

public record SkyBlockProfile(
        boolean current,
        @SerializedName("magical_power") int magicalPower,
        @SerializedName("dungeons") DungeonData dungeons,
        @SerializedName("items") ItemData items,
        @SerializedName("pets") PetData pets,
        @SerializedName("bank") Double bankBalance
) {}
package ru.massonnn.masutils.client.hypixel.model;

import com.google.gson.annotations.SerializedName;

import java.util.Map;

public record DungeonData(
        @SerializedName("completions") Map<String, Integer> comps,
        @SerializedName("personal_bests") Map<String, Double> pbs
) {}
package ru.massonnn.masutils.client.hypixel.model;

import com.google.gson.annotations.SerializedName;

public record PetData(
        @SerializedName("active_pet") String activePet,
        @SerializedName("pets") java.util.List<Pet> allPets
) {}

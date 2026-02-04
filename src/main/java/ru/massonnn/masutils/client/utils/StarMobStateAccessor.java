package ru.massonnn.masutils.client.utils;

public interface StarMobStateAccessor {
    void setStarMob(boolean star);
    boolean isStarMob();
    void setHasNameTag(boolean hasTag);
    boolean hasNameTag();
}
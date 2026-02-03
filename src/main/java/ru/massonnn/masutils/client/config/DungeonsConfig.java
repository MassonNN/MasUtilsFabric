package ru.massonnn.masutils.client.config;


public class DungeonsConfig {
    public boolean highlightStarredMobs = true;

    public boolean autoKickEnabled = false;
    public boolean doKick = true;

    public NotifyMode notifyMode = NotifyMode.NOTIFY_REASON_ALL;

    public int minMagicalPower = 600;
    public int minBankBalance = 0;

    public boolean requireTerminator = true;
    public boolean requireTerminatorOnlyForArcher = true;
    public boolean requireGyroWand = false;
    public boolean requireGoldenDragon = false;
    public boolean requireSpiritPet = false;
    public int minGDragLevel = 100;

    public int minF7Comps = 50;
    public int minM4Comps = 0;
    public int minM5Comps = 0;
    public int minM6Comps = 0;
    public int minM7Comps = 0;

    public int maxF7Time = 420;
    public int maxM7Time = 600;
}
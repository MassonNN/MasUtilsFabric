package ru.massonnn.masutils.client.config;

public class MasUtilsConfig {
    public int version = 1;
    public final GeneralConfig general = new GeneralConfig();
    public final Mineshaft mineshaft = new Mineshaft();
    public final DevConfig dev = new DevConfig();
    public final NucleusRunsConfig nucleusRuns = new NucleusRunsConfig();
    public final QoLConfig qol = new QoLConfig();
}

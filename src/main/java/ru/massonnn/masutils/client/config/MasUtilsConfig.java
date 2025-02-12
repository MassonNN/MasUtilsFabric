package ru.massonnn.masutils.client.config;

import dev.isxander.yacl3.config.v2.api.SerialEntry;

public class MasUtilsConfig {
    @SerialEntry
    public int version = 1;
    @SerialEntry
    public GeneralConfig general = new GeneralConfig();
    @SerialEntry
    public Mineshaft mineshaft = new Mineshaft();
}

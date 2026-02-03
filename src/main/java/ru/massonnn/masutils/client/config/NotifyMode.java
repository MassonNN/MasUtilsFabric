package ru.massonnn.masutils.client.config;

import net.minecraft.text.Text;

public enum NotifyMode {
    NOTIFY_REASON_ALL("masutils.config.dungeons.kickmode.notifyReasonAll"),
    NOTIFY_REASON_PARTY("masutils.config.dungeons.kickmode.notifyReasonParty"),
    NOTIFY_REASON_ONLY_ME("masutils.config.dungeons.kickmode.notifyReasonOnlyMe");

    private final String name;

    NotifyMode(String namePath) {
        this.name = namePath;
    }

    public Text getName() {
        return Text.translatable(this.name);
    }

    public String getDescription() {
        return Text.translatable(this.name + ".@Tooltip").getString();
    }
}

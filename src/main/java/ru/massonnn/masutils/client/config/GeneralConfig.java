package ru.massonnn.masutils.client.config;

import ru.massonnn.masutils.client.features.updater.UpdateAction;
import ru.massonnn.masutils.client.features.updater.UpdateChannel;

public class GeneralConfig {
    public boolean updateNotifications = true;
    public boolean checkForUpdates = true;
    public UpdateChannel updateChannel = UpdateChannel.MAIN;
    public UpdateAction updateAction = UpdateAction.DOWNLOAD;
    public boolean partyCommands = true;
    public boolean masterSwitch = true;
}

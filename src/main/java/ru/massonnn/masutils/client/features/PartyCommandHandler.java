package ru.massonnn.masutils.client.features;

import net.minecraft.client.MinecraftClient;
import ru.massonnn.masutils.client.config.MasUtilsConfig;
import ru.massonnn.masutils.client.config.MasUtilsConfigManager;
import ru.massonnn.masutils.client.events.ChatEvent;
import ru.massonnn.masutils.client.hypixel.MineshaftType;
import ru.massonnn.masutils.client.utils.CommandExecutor;
import ru.massonnn.masutils.client.utils.ModMessage;
import ru.massonnn.masutils.client.utils.PartyChatParser;

import java.util.ArrayList;
import java.util.List;

public class PartyCommandHandler implements ChatEvent.ChatStringEvent {
    private final StatsTracker statsTracker;
    private final TpsChecker tpsChecker;
    private final PingChecker pingChecker;
    private final List<String> noWarpList = new ArrayList<>();

    public PartyCommandHandler() {
        this.statsTracker = new StatsTracker();
        this.tpsChecker = new TpsChecker();
        this.pingChecker = new PingChecker();
    }

    @Override
    public void onMessage(String message) {
        MasUtilsConfig config = MasUtilsConfigManager.get();

        if (!config.general.masterSwitch || !config.general.partyCommands) {
            return;
        }

        PartyChatParser.PartyMessage partyMessage = PartyChatParser.parsePartyMessage(message);
        if (partyMessage == null) {
            return;
        }

        String command = partyMessage.message.toLowerCase().trim();
        String[] parts = command.split("\\s+");
        String cmd = parts[0];
        String[] args = parts.length > 1 ? java.util.Arrays.copyOfRange(parts, 1, parts.length) : new String[0];

        String playerName = partyMessage.playerName;

        switch (cmd) {
            case "!w":
            case "!warp":
                handleWarp(playerName);
                break;
            case "!ptme":
            case "!pt":
            case "!transfer":
                handleTransfer(playerName);
                break;
            case "!tps":
                handleTps();
                break;
            case "!ping":
                handlePing();
                break;
            case "!fps":
                handleFps();
                break;
            case "!nowarp":
            case "!nwp":
                handleNoWarp(playerName);
                break;
            case "!conwarp":
            case "!cwp":
                handleConWarp(playerName);
                break;
            case "!keys":
                handleKeys();
                break;
            case "!sinceshaft":
            case "!ss":
                handleSinceShaft();
                break;
            case "!sincevang":
            case "!sv":
                handleSinceVang();
                break;
            case "!stats":
                handleStats();
                break;
            case "!inv":
                if (args.length > 0) {
                    handleInv(args[0]);
                }
                break;
            case "!allinv":
                handleAllInv();
                break;
        }
    }

    private void handleWarp(String playerName) {
        kickToNoWarp();
    }

    private void handleTransfer(String playerName) {
        CommandExecutor.executeCommand("p transfer " + playerName);
    }

    private void handleTps() {
        tpsChecker.requestTps();
        CommandExecutor.sendPartyMessage("[MasUtils] Waiting for TPS check...");
    }

    private void handlePing() {
        long ping = pingChecker.getLastPing();
        CommandExecutor.sendPartyMessage("[MasUtils] PING: " + ping);
    }

    private void handleFps() {
        int fps = MinecraftClient.getInstance().getCurrentFps();
        CommandExecutor.sendPartyMessage("[MasUtils] FPS: " + fps);
    }

    private void handleNoWarp(String playerName) {
        CommandExecutor.sendPartyMessage("[MasUtils] Stop warping " + playerName);
        if (!noWarpList.contains(playerName)) {
            noWarpList.add(playerName);
        }
    }

    private void handleConWarp(String playerName) {
        CommandExecutor.sendPartyMessage("[MasUtils] Continue warping " + playerName);
        noWarpList.remove(playerName);
    }

    private void handleKeys() {
        KeysCounter.KeysCount keys = KeysCounter.countKeys();
        CommandExecutor.sendPartyMessage("[MasUtils] Tungsten: " + keys.tungsten +
                " | Umber: " + keys.umber + " | Vanguard: " + keys.vanguard);
    }

    private void handleSinceShaft() {
        int blocks = statsTracker.getBlocksSinceLastMineshaft();
        CommandExecutor.sendPartyMessage("[MasUtils] Blocks since last mineshaft: " + blocks);
    }

    private void handleSinceVang() {
        int mineshafts = statsTracker.getMineshaftsSinceFairy();
        CommandExecutor.sendPartyMessage("[MasUtils] Mineshafts since Fairy: " + mineshafts);
    }

    private void handleStats() {
        int sessionMineshafts = statsTracker.getSessionMineshafts();
        CommandExecutor.sendPartyMessage("[MasUtils] Session mineshafts: " + sessionMineshafts);
    }

    private void handleInv(String playerName) {
        CommandExecutor.executeCommand("p invite " + playerName);
    }

    private void handleAllInv() {
        CommandExecutor.executeCommand("p settings allinvite");
    }

    private void kickToNoWarp() {
        for (String playerName : noWarpList) {
            CommandExecutor.executeCommand("p kick " + playerName);
        }
        new Thread(() -> {
            try {
                Thread.sleep(100);
                excludeWarp();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    private void excludeWarp() {
        CommandExecutor.executeCommand("p warp");
        new Thread(() -> {
            try {
                Thread.sleep(100);
                reInviteAfterWarp();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    private void reInviteAfterWarp() {
        for (String playerName : noWarpList) {
            CommandExecutor.executeCommand("p invite " + playerName);
        }
    }

    public void onTimeUpdate() {
        if (tpsChecker.isRequested()) {
            tpsChecker.onTimeUpdate();
            if (!tpsChecker.isRequested()) {
                double tps = tpsChecker.getLastTps();
                CommandExecutor.sendPartyMessage("[MasUtils] TPS: " + String.format("%.1f", tps));
            }
        }
    }

    public void onStatisticsReceived() {
        pingChecker.onStatisticsReceived();
    }

    public void onJoinGame() {
        pingChecker.onJoinGame();
    }

    public void checkPing() {
        pingChecker.checkPing();
    }

    public void incrementBlocksIfNotInMineshaft() {
        if (!MineshaftType.isInMineshaft()) {
            statsTracker.incrementBlocks();
        }
    }
}

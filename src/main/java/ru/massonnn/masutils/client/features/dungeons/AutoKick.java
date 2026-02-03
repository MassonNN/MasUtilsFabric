package ru.massonnn.masutils.client.features.dungeons;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import ru.massonnn.masutils.client.config.DungeonsConfig;
import ru.massonnn.masutils.client.config.MasUtilsConfigManager;
import ru.massonnn.masutils.client.events.PartyEvent;
import ru.massonnn.masutils.client.hypixel.HypixelManager;
import ru.massonnn.masutils.client.hypixel.model.SkyBlockProfile;
import ru.massonnn.masutils.client.telemetry.ErrorManager;
import ru.massonnn.masutils.client.utils.CommandExecutor;
import ru.massonnn.masutils.client.utils.ModMessage;

import java.util.HashMap;

public class AutoKick {

    public static void init() {
        PartyEvent.PARTY_FINDER_JOINED.register(AutoKick::joinedPartyFinder);
    }

    public static void joinedPartyFinder(String username, String dungeonClass) {
        HypixelManager.fetchProfile(username).thenAccept(profile -> {
            if (profile == null) {
                MinecraftClient.getInstance().execute(() ->
                        ModMessage.sendErrorMessage("Cant check " + username + " (API disabled)")
                );
                return;
            }

            MinecraftClient.getInstance().execute(() -> {
                checkEligible(username, profile, dungeonClass);
            });
        }).exceptionally(ex -> {
            ex.printStackTrace();

            Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
            HashMap<Object, Object> locals = new HashMap<>();
            locals.put("username", username);
            locals.put("dungeonClass", dungeonClass);
            locals.put("target", "Joined Party Finder");

            ErrorManager.sendError(cause.toString(), locals);
            return null;
        });
    }

    public static void checkEligible(String username, SkyBlockProfile profile, String dungeonClass) {
        DungeonsConfig config = MasUtilsConfigManager.get().dungeonsConfig;

        if (!config.autoKickEnabled) return;

        if (profile.magicalPower() < config.minMagicalPower) {
            doAction(username, Text.translatable("masutils.dungeons.lowMagicalPower", profile.magicalPower(), config.minMagicalPower).getString());
            return;
        }

        var comps = profile.dungeons().comps();
        if (checkComp(username, comps.getOrDefault("7", 0), config.minF7Comps, "F7")) return;
        if (checkComp(username, comps.getOrDefault("m4", 0), config.minM4Comps, "M4")) return;
        if (checkComp(username, comps.getOrDefault("m5", 0), config.minM5Comps, "M5")) return;
        if (checkComp(username, comps.getOrDefault("m6", 0), config.minM6Comps, "M6")) return;
        if (checkComp(username, comps.getOrDefault("m7", 0), config.minM7Comps, "M7")) return;

        if (config.requireTerminator && !profile.items().hasTerminator()) {
            boolean isArcher = dungeonClass.equalsIgnoreCase("Archer");
            if (isArcher) {
                doAction(username, Text.translatable("masutils.dungeons.termRequiredForArcher").getString());
                return;
            } else if (!config.requireTerminatorOnlyForArcher) {
                doAction(username, Text.translatable("masutils.dungeons.termRequired").getString());
                return;
            }
        }

        if (config.requireGyroWand && !profile.items().hasGyroWand()) {
            doAction(username, Text.translatable("masutils.dungeons.itemMissing", "Gyrokinetic Wand").getString());
            return;
        }

        if (config.requireGoldenDragon) {
            boolean hasGDrag = profile.pets().allPets().stream().anyMatch(pet -> pet.type().equals("GOLDEN_DRAGON"));
            boolean hasGDragMinLevel = profile.pets().allPets().stream().anyMatch(pet -> pet.type().equals("GOLDEN_DRAGON") && pet.level() >= config.minGDragLevel);

            if (!hasGDrag) {
                doAction(username, Text.translatable("masutils.dungeons.petMissing", "Golden Dragon").getString());
                return;
            }

            if (!hasGDragMinLevel) {
                doAction(username, Text.translatable("masutils.dungeons.petMissing", config.minGDragLevel).getString());
                return;
            }

//            if (profile.bankBalance() != null && profile.bankBalance() < config.minBankBalance) {
//                doAction(username, Text.translatable("masutils.dungeons.lowBank", config.minBankBalance).getString());
//                return;
//            }
        }

        if (config.requireSpiritPet) {
            boolean hasSpirit = profile.pets().allPets().stream()
                    .anyMatch(pet -> pet.type().equals("SPIRIT"));
            if (!hasSpirit) {
                doAction(username, Text.translatable("masutils.dungeons.petMissing", "Spirit").getString());
                return;
            }
        }

        var pbs = profile.dungeons().pbs();
        if (checkPB(username, pbs.get("7"), config.maxF7Time, "F7")) return;
        if (checkPB(username, pbs.get("m7"), config.maxM7Time, "M7")) return;

        ModMessage.sendModMessage(Text.translatable("masutils.dungeons.passed", username));
    }

    private static boolean checkComp(String username, int actual, int required, String floor) {
        if (required > 0 && actual < required) {
            doAction(username, Text.translatable("masutils.dungeons.lowComps", floor, actual, required).getString());
            return true;
        }
        return false;
    }

    private static void doAction(String username, String reason) {
        DungeonsConfig config = MasUtilsConfigManager.get().dungeonsConfig;
        String message = Text.translatable("masutils.dungeons.notPassed", username, reason).getString();

        switch (config.notifyMode) {
            case NOTIFY_REASON_ALL -> {
                CommandExecutor.sendPartyMessage(message);
                if (config.doKick)
                    CommandExecutor.executeCommand("p kick " + username);
            }
            case NOTIFY_REASON_PARTY -> {
                if (config.doKick)
                    CommandExecutor.executeCommand("p kick " + username);
                CommandExecutor.sendPartyMessage(message);
            }
            case NOTIFY_REASON_ONLY_ME -> {
                if (config.doKick)
                    CommandExecutor.executeCommand("p kick " + username);
                ModMessage.sendModMessage(message);
            }
        }
    }

    private static String formatPB(int seconds) {
        return (seconds / 60) + "m " + (seconds % 60) + "s";
    }

    private static boolean checkPB(String username, Double actualMs, int maxSeconds, String floor) {
        if (maxSeconds <= 0 || actualMs == null) return false;

        int actualSeconds = (int) (actualMs / 1000);
        if (actualSeconds > maxSeconds) {
            doAction(username, Text.translatable("masutils.dungeons.badPB",
                    floor, formatPB(actualSeconds), formatPB(maxSeconds)).getString());
            return true;
        }
        return false;
    }

}
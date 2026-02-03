package ru.massonnn.masutils.client.config.categories;

import net.azureaaron.dandelion.systems.ConfigCategory;
import net.azureaaron.dandelion.systems.Option;
import net.azureaaron.dandelion.systems.OptionGroup;
import net.azureaaron.dandelion.systems.controllers.IntegerController;
import net.azureaaron.dandelion.systems.controllers.StringController;
import net.minecraft.text.Text;
import ru.massonnn.masutils.Masutils;
import ru.massonnn.masutils.client.config.MasUtilsConfig;
import ru.massonnn.masutils.client.config.NotifyMode;
import ru.massonnn.masutils.client.utils.ConfigUtils;

public class DungeonsCategory {

    public static ConfigCategory create(MasUtilsConfig defaults, MasUtilsConfig config) {
        return ConfigCategory.createBuilder()
                .id(Masutils.id("config/dungeons"))
                .name(Text.translatable("masutils.config.dungeons"))
                .option(
                        Option.<Boolean>createBuilder()
                                .name(Text.translatable("masutils.config.dungeons.starMobs"))
                                .description(Text.translatable("masutils.config.dungeons.starMobs.@Tooltip"))
                                .binding(
                                        defaults.dungeonsConfig.highlightStarredMobs,
                                        () -> config.dungeonsConfig.highlightStarredMobs,
                                        newValue -> config.dungeonsConfig.highlightStarredMobs = newValue)
                                .controller(ConfigUtils.createBooleanController())
                                .build()
                )
                .option(
                        Option.<Boolean>createBuilder()
                                .name(Text.translatable("masutils.config.dungeons.autoKickEnabled"))
                                .binding(
                                        defaults.dungeonsConfig.autoKickEnabled,
                                        () -> config.dungeonsConfig.autoKickEnabled,
                                        newValue -> config.dungeonsConfig.autoKickEnabled = newValue)
                                .controller(ConfigUtils.createBooleanController())
                                .build()
                )
                .group(
                    OptionGroup.createBuilder()
                    .name(Text.translatable("masutils.config.dungeons.autoKick"))
                    .id(Masutils.id("config/dungeonsautokick"))
                    .option(
                            Option.<Boolean>createBuilder()
                                    .name(Text.translatable("masutils.config.dungeons.doKick"))
                                    .description(Text.translatable("masutils.config.dungeons.doKick.@Tooltip"))
                                    .binding(
                                            defaults.dungeonsConfig.doKick,
                                            () -> config.dungeonsConfig.doKick,
                                            newValue -> config.dungeonsConfig.doKick = newValue)
                                    .controller(ConfigUtils.createBooleanController())
                                    .build()
                    )
                    .option(
                            Option.<NotifyMode>createBuilder()
                                    .name(Text.translatable("masutils.config.dungeons.notifyMode"))
                                    .description(Text.translatable("masutils.config.dungeons.notifyMode.@Tooltip"))
                                    .binding(
                                            defaults.dungeonsConfig.notifyMode,
                                            () -> config.dungeonsConfig.notifyMode,
                                            newValue -> config.dungeonsConfig.notifyMode = newValue
                                    )
                                    .controller(ConfigUtils.createEnumDropdownController(NotifyMode::getName))
                                    .build()
                    )
                    .option(
                            Option.<Integer>createBuilder()
                                    .name(Text.translatable("masutils.config.dungeons.minMagicalPower"))
                                    .binding(
                                            defaults.dungeonsConfig.minMagicalPower,
                                            () -> config.dungeonsConfig.minMagicalPower,
                                            newValue -> config.dungeonsConfig.minMagicalPower = newValue)
                                    .controller(IntegerController.createBuilder().build())
                                    .build()
                    )
//                    .option(
//                            Option.<Integer>createBuilder()
//                                    .name(Text.translatable("masutils.config.dungeons.minBankBalance"))
//                                    .binding(
//                                            defaults.dungeonsConfig.minBankBalance,
//                                            () -> config.dungeonsConfig.minBankBalance,
//                                            newValue -> config.dungeonsConfig.minBankBalance = newValue)
//                                    .controller(IntegerController.createBuilder().build())
//                                    .build()
//                    )
                    .option(
                            Option.<Boolean>createBuilder()
                                    .name(Text.translatable("masutils.config.dungeons.requireTerminator"))
                                    .binding(
                                            defaults.dungeonsConfig.requireTerminator,
                                            () -> config.dungeonsConfig.requireTerminator,
                                            newValue -> config.dungeonsConfig.requireTerminator = newValue)
                                    .controller(ConfigUtils.createBooleanController())
                                    .build()
                    )
                    .option(
                            Option.<Boolean>createBuilder()
                                    .name(Text.translatable("masutils.config.dungeons.requireTerminatorOnlyForArcher"))
                                    .binding(
                                            defaults.dungeonsConfig.requireTerminatorOnlyForArcher,
                                            () -> config.dungeonsConfig.requireTerminatorOnlyForArcher,
                                            newValue -> config.dungeonsConfig.requireTerminatorOnlyForArcher = newValue)
                                    .controller(ConfigUtils.createBooleanController())
                                    .build()
                    )
                    .option(
                            Option.<Boolean>createBuilder()
                                    .name(Text.translatable("masutils.config.dungeons.requireGyroWand"))
                                    .binding(
                                            defaults.dungeonsConfig.requireGyroWand,
                                            () -> config.dungeonsConfig.requireGyroWand,
                                            newValue -> config.dungeonsConfig.requireGyroWand = newValue)
                                    .controller(ConfigUtils.createBooleanController())
                                    .build()
                    )
                    .option(
                            Option.<Boolean>createBuilder()
                                    .name(Text.translatable("masutils.config.dungeons.requireGoldenDragon"))
                                    .binding(
                                            defaults.dungeonsConfig.requireGoldenDragon,
                                            () -> config.dungeonsConfig.requireGoldenDragon,
                                            newValue -> config.dungeonsConfig.requireGoldenDragon = newValue)
                                    .controller(ConfigUtils.createBooleanController())
                                    .build()
                    )
                    .option(
                            Option.<Integer>createBuilder()
                                    .name(Text.translatable("masutils.config.dungeons.minGDragLevel"))
                                    .binding(
                                            defaults.dungeonsConfig.minGDragLevel,
                                            () -> config.dungeonsConfig.minGDragLevel,
                                            newValue -> config.dungeonsConfig.minGDragLevel = newValue)
                                    .controller(IntegerController.createBuilder().build())
                                    .build()
                    )
                    .option(
                            Option.<Boolean>createBuilder()
                                    .name(Text.translatable("masutils.config.dungeons.requireSpiritPet"))
                                    .binding(
                                            defaults.dungeonsConfig.requireSpiritPet,
                                            () -> config.dungeonsConfig.requireSpiritPet,
                                            newValue -> config.dungeonsConfig.requireSpiritPet = newValue)
                                    .controller(ConfigUtils.createBooleanController())
                                    .build()
                    )
                    .option(
                            Option.<Integer>createBuilder()
                                    .name(Text.translatable("masutils.config.dungeons.minComps", "F7"))
                                    .binding(
                                            defaults.dungeonsConfig.minF7Comps,
                                            () -> config.dungeonsConfig.minF7Comps,
                                            newValue -> config.dungeonsConfig.minF7Comps = newValue)
                                    .controller(IntegerController.createBuilder().build())
                                    .build()
                    )
                    .option(
                            Option.<Integer>createBuilder()
                                    .name(Text.translatable("masutils.config.dungeons.minComps", "M4"))
                                    .binding(
                                            defaults.dungeonsConfig.minM4Comps,
                                            () -> config.dungeonsConfig.minM4Comps,
                                            newValue -> config.dungeonsConfig.minM4Comps = newValue)
                                    .controller(IntegerController.createBuilder().build())
                                    .build()
                    )
                    .option(
                            Option.<Integer>createBuilder()
                                    .name(Text.translatable("masutils.config.dungeons.minComps", "M5"))
                                    .binding(
                                            defaults.dungeonsConfig.minM5Comps,
                                            () -> config.dungeonsConfig.minM5Comps,
                                            newValue -> config.dungeonsConfig.minM5Comps = newValue)
                                    .controller(IntegerController.createBuilder().build())
                                    .build()
                    )
                    .option(
                            Option.<Integer>createBuilder()
                                    .name(Text.translatable("masutils.config.dungeons.minComps", "M6"))
                                    .binding(
                                            defaults.dungeonsConfig.minM6Comps,
                                            () -> config.dungeonsConfig.minM6Comps,
                                            newValue -> config.dungeonsConfig.minM6Comps = newValue)
                                    .controller(IntegerController.createBuilder().build())
                                    .build()
                    )
                    .option(
                            Option.<Integer>createBuilder()
                                    .name(Text.translatable("masutils.config.dungeons.minComps", "M7"))
                                    .binding(
                                            defaults.dungeonsConfig.minM7Comps,
                                            () -> config.dungeonsConfig.minM7Comps,
                                            newValue -> config.dungeonsConfig.minM7Comps = newValue)
                                    .controller(IntegerController.createBuilder().build())
                                    .build()
                    )
                    .option(
                            Option.<String>createBuilder()
                                    .name(Text.translatable("masutils.config.dungeons.maxPB", "F7"))
                                    .description(Text.translatable("masutils.config.dungeons.maxPB.@Tooltip"))
                                    .binding(
                                            formatTimeForDisplay(defaults.dungeonsConfig.maxF7Time),
                                            () -> formatTimeForDisplay(config.dungeonsConfig.maxF7Time),
                                            newValue -> {
                                                int seconds = parseTime(newValue);
                                                if (seconds >= 0) {
                                                    config.dungeonsConfig.maxF7Time = seconds;
                                                }
                                            })
                                    .controller(StringController.createBuilder().build())
                                    .build()
                    ).build())
            .build();
    }

    public static int parseTime(String input) {
        if (input == null || input.isBlank()) return -1;

        input = input.toLowerCase().replaceAll("\\s+", ""); // Убираем пробелы

        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("(\\d+)([hms])");
        java.util.regex.Matcher matcher = pattern.matcher(input);

        int totalSeconds = 0;
        boolean found = false;

        while (matcher.find()) {
            found = true;
            int value = Integer.parseInt(matcher.group(1));
            char unit = matcher.group(2).charAt(0);

            switch (unit) {
                case 'h' -> totalSeconds += value * 3600;
                case 'm' -> totalSeconds += value * 60;
                case 's' -> totalSeconds += value;
            }
        }

        if (!found) {
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                return -1;
            }
        }

        return totalSeconds;
    }

    public static String formatTimeForDisplay(int totalSeconds) {
        if (totalSeconds <= 0) return "0s";
        int h = totalSeconds / 3600;
        int m = (totalSeconds % 3600) / 60;
        int s = totalSeconds % 60;

        StringBuilder sb = new StringBuilder();
        if (h > 0) sb.append(h).append("h ");
        if (m > 0) sb.append(m).append("m ");
        if (s > 0 || sb.length() == 0) sb.append(s).append("s");

        return sb.toString().trim();
    }
}
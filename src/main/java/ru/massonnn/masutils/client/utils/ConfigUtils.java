package ru.massonnn.masutils.client.utils;

import net.azureaaron.dandelion.systems.ButtonOption;
import net.azureaaron.dandelion.systems.controllers.BooleanController;
import net.azureaaron.dandelion.systems.controllers.ColourController;
import net.azureaaron.dandelion.systems.controllers.EnumController;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.option.KeybindsScreen;
import net.minecraft.text.Text;

import java.util.function.Function;

public class ConfigUtils {
    public static BooleanController createBooleanController() {
        return BooleanController.createBuilder()
                .coloured(true)
                .booleanStyle(BooleanController.BooleanStyle.YES_NO)
                .build();
    }

    public static ColourController createColourController(boolean hasAlpha) {
        return ColourController.createBuilder()
                .hasAlpha(hasAlpha)
                .build();
    }

    @SuppressWarnings("unchecked")
    public static <T extends Enum<T>> EnumController<T> createEnumController() {
        return (EnumController<T>) EnumController.createBuilder().build();
    }

    @SuppressWarnings("unchecked")
    public static <T extends Enum<T>> EnumController<T> createEnumController(Function<T, Text> formatter) {
        return (EnumController<T>) EnumController.createBuilder().formatter((Function) formatter).build();
    }

    @SuppressWarnings("unchecked")
    public static <T extends Enum<T>> EnumController<T> createEnumDropdownController(Function<T, Text> formatter) {
        return (EnumController<T>) EnumController.createBuilder().dropdown(true).formatter((Function) formatter).build();
    }

//    public static ButtonOption createShortcutToKeybindsScreen() {
//        MinecraftClient client = MinecraftClient.getInstance();
//        return ButtonOption.createBuilder()
//                .name(Text.translatable("skyblocker.config.shortcutToKeybindsSettings"))
//                .action(screen -> client.setScreen(new KeybindsScreen(screen, client.options)))
//                .prompt(Text.translatable("skyblocker.config.shortcutToKeybindsSettings.@Text"))
//                .build();
//    }

    //FIXME Would probably be a good idea to add a utility method for creating a waypoint type option
}

package ru.massonnn.masutils.mixins;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.Coerce;
import ru.massonnn.masutils.Masutils;

@Pseudo
@IfModLoaded("firmament")
@Mixin(targets = "moe.nea.firmament.features.misc.ModAnnouncer")
public class FirmamentPatcher {
    @WrapMethod(method = "onServerJoin", require = 0)
    private void onServerJoin(@Coerce Object event, Operation<Void> original) {
        Masutils.LOGGER.info("Firmament mod announcer intercepted");
    }
}
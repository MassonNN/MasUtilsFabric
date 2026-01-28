package ru.massonnn.masutils.mixins;

import net.minecraft.scoreboard.ScoreHolder;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ScoreboardScore;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.massonnn.masutils.client.hypixel.LocationUtils;

@Mixin(value = Scoreboard.class)
public class ScoreboardChange {

    @Inject(method = "updateScore", at = @At("HEAD"))
    private void onUpdateScore(ScoreHolder scoreHolder, ScoreboardObjective objective, ScoreboardScore score,
            CallbackInfo ci) {
        Scoreboard scb = (Scoreboard) (Object) this;
        LocationUtils.detectLocationFromScoreboard(scb);
    }
}

package ru.massonnn.masutils.mixins;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import net.minecraft.scoreboard.ScoreHolder;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ScoreboardScore;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.massonnn.masutils.client.events.MineshaftEvent;
import ru.massonnn.masutils.client.hypixel.MineshaftType;

@Mixin(value = Scoreboard.class)
public class ScoreboardChange {
    @Shadow @Final private Object2ObjectMap<String, ScoreboardObjective> objectives;
    private boolean inMineshaft = false;

    @Inject(method = "updateScore", at = @At("HEAD"))
    private void onUpdateScore(ScoreHolder scoreHolder, ScoreboardObjective objective, ScoreboardScore score, CallbackInfo ci) {
        MineshaftType type = MineshaftType.detectByScoreboard(objective.getScoreboard());
        if (type != MineshaftType.UNDEF) {
            if (!inMineshaft) {
                inMineshaft = true;
                MineshaftEvent.MINESHAFT_JOINED_EVENT.invoker().onMineshaftJoined();
            }
        } else {
            if (inMineshaft) {
                inMineshaft = false;
                MineshaftEvent.MINESHAFT_LEFT_EVENT.invoker().onMineshaftLeft();
            }
        }
    }
}

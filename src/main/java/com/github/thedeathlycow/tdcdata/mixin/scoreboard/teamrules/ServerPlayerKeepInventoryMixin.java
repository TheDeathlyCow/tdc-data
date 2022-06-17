package com.github.thedeathlycow.tdcdata.mixin.scoreboard.teamrules;

import com.github.thedeathlycow.tdcdata.scoreboard.RuledTeam;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerKeepInventoryMixin {

    @Inject(
            method = "copyFrom",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/network/ServerPlayerInteractionManager;setGameMode(Lnet/minecraft/world/GameMode;Lnet/minecraft/world/GameMode;)V",
                    shift = At.Shift.AFTER
            )
    )
    private void copyInventoryIfOnKeepInvTeam(ServerPlayerEntity oldPlayer, boolean alive, CallbackInfo ci) {
        ServerPlayerEntity instance = (ServerPlayerEntity) (Object) this;

        boolean willCopy = !alive && (instance.world.getGameRules().getBoolean(GameRules.KEEP_INVENTORY) || oldPlayer.isSpectator());
        boolean onKeepInvTeam = instance.getScoreboardTeam() instanceof RuledTeam ruledTeam && ruledTeam.tdcdata$shouldKeepInventory();

        if (!willCopy && onKeepInvTeam) {
            instance.getInventory().clone(oldPlayer.getInventory());
            instance.experienceLevel = oldPlayer.experienceLevel;
            instance.totalExperience = oldPlayer.totalExperience;
            instance.experienceProgress = oldPlayer.experienceProgress;
            instance.setScore(oldPlayer.getScore());
        }
    }

}

package com.github.thedeathlycow.tdcdata.mixin.scoreboard.teamrules;

import com.github.thedeathlycow.tdcdata.DatapackUtils;
import com.github.thedeathlycow.tdcdata.scoreboard.RuledTeam;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public class KeepInventoryOnDeathMixin {

    @Inject(
            method = "dropInventory",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/player/PlayerEntity;vanishCursedItems()V",
                    shift = At.Shift.BEFORE
            ),
            cancellable = true
    )
    private void stopLootDropIfOnKeepInvTeam(CallbackInfo ci) {
        PlayerEntity instance = (PlayerEntity) (Object) this;
        AbstractTeam playerTeam = instance.getScoreboardTeam();

        if (playerTeam instanceof RuledTeam ruledTeam && ruledTeam.tdcdata$shouldKeepInventory()) {
            ci.cancel();
        }
    }

    @Inject(
            method = "getXpToDrop",
            at = @At(
                    value = "HEAD"
            ),
            cancellable = true
    )
    private void stopXpDropIfOnKeepInvTeam(PlayerEntity player, CallbackInfoReturnable<Integer> cir) {
        PlayerEntity instance = (PlayerEntity) (Object) this;
        AbstractTeam playerTeam = instance.getScoreboardTeam();
        if (playerTeam instanceof RuledTeam ruledTeam && ruledTeam.tdcdata$shouldKeepInventory()) {
            cir.setReturnValue(0);
        }
    }

}

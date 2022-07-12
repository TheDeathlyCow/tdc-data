package com.github.thedeathlycow.tdcdata.mixin.advancement_triggers;

import com.github.thedeathlycow.tdcdata.advancement.TdcDataAdvancementTriggers;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.WardenEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.event.listener.GameEventListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WardenEntity.class)
public class TriggerWardenCriterionMixin {

    @Inject(
            method = "accept",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/mob/WardenBrain;lookAtDisturbance(Lnet/minecraft/entity/mob/WardenEntity;Lnet/minecraft/util/math/BlockPos;)V"
            )
    )
    private void triggerAlertWardenAdvancement(ServerWorld world, GameEventListener listener, BlockPos pos, GameEvent event, Entity entity, Entity sourceEntity, float distance, CallbackInfo ci) {
        if (entity instanceof ServerPlayerEntity serverPlayer) {
            final WardenEntity instance = (WardenEntity) (Object) this;
            TdcDataAdvancementTriggers.PLAYER_ALERTS_WARDEN.trigger(serverPlayer, event, world, instance);
        }
    }
}

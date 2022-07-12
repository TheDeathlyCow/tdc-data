package com.github.thedeathlycow.tdcdata.mixin.advancement_triggers;

import com.github.thedeathlycow.tdcdata.advancement.TdcDataAdvancementTriggers;
import net.minecraft.block.entity.SculkSensorBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.event.listener.GameEventListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SculkSensorBlockEntity.class)
public class TriggerSculkSensorCriterionMixin {

    @Inject(
            method = "accept",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/block/SculkSensorBlock;setActive(Lnet/minecraft/entity/Entity;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;I)V"
            )
    )
    private void triggerSculkSensorAdvancement(ServerWorld world, GameEventListener listener, BlockPos pos, GameEvent event, Entity entity, Entity sourceEntity, float distance, CallbackInfo ci) {
        if (entity instanceof ServerPlayerEntity serverPlayer) {
            TdcDataAdvancementTriggers.PLAYER_TRIGGER_SCULK_SENSOR.trigger(serverPlayer, event, world, pos);
        }
    }
}

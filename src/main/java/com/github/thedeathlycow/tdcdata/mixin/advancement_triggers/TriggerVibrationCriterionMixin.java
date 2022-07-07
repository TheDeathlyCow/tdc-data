package com.github.thedeathlycow.tdcdata.mixin.advancement_triggers;

import com.github.thedeathlycow.tdcdata.advancement.TdcDataAdvancementTriggers;
import net.minecraft.block.SculkSensorBlock;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.event.GameEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerWorld.class)
public class TriggerVibrationCriterionMixin {

    @Inject(
            method = "emitGameEvent",
            at = @At("RETURN")
    )
    private void triggerVibrationCriterion(GameEvent event, Vec3d emitterPos, GameEvent.Emitter emitter, CallbackInfo ci) {
        if (SculkSensorBlock.FREQUENCIES.containsKey(event)) {
            Entity entity = emitter.sourceEntity();
            if (entity instanceof ServerPlayerEntity serverPlayer) {
                final int frequency = SculkSensorBlock.FREQUENCIES.getInt(event);
                TdcDataAdvancementTriggers.TRIGGER_VIBRATION.trigger(serverPlayer, event, frequency);
            }
        }
    }

}

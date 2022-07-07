package com.github.thedeathlycow.tdcdata.mixin.event;

import com.github.thedeathlycow.tdcdata.advancement.TdcDataAdvancementTriggers;
import net.fabricmc.fabric.api.registry.SculkSensorFrequencyRegistry;
import net.minecraft.block.SculkSensorBlock;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.event.listener.VibrationListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(VibrationListener.Callback.class)
public class VibrationListenerMixin {


    @Inject(
            method = "canAccept",
            at = @At("RETURN")
    )
    private void triggerVibrationCriterion(GameEvent gameEvent, GameEvent.Emitter emitter, CallbackInfoReturnable<Boolean> cir) {
        final boolean vibrationWasTriggered = cir.getReturnValue();

        if (SculkSensorBlock.FREQUENCIES.containsKey(gameEvent) && vibrationWasTriggered) {
            Entity entity = emitter.sourceEntity();
            if (entity instanceof ServerPlayerEntity serverPlayer) {
                final int frequency = SculkSensorBlock.FREQUENCIES.getInt(gameEvent);
                TdcDataAdvancementTriggers.TRIGGER_VIBRATION.trigger(serverPlayer, gameEvent, frequency);
            }
        }
    }

}

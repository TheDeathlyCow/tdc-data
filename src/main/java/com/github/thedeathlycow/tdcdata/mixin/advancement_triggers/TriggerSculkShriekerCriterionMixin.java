package com.github.thedeathlycow.tdcdata.mixin.advancement_triggers;

import com.github.thedeathlycow.tdcdata.advancement.TdcDataAdvancementTriggers;
import net.minecraft.block.entity.SculkShriekerBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.event.listener.GameEventListener;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SculkShriekerBlockEntity.class)
public class TriggerSculkShriekerCriterionMixin {

    @Inject(
            method = "accept",
            at = @At("HEAD")
    )
    private void triggerSculkShriekerAdvancement(ServerWorld world, GameEventListener listener, BlockPos pos, GameEvent event, Entity entity, Entity sourceEntity, float distance, CallbackInfo ci) {
        @Nullable ServerPlayerEntity player = SculkShriekerBlockEntity.findResponsiblePlayerFromEntity(sourceEntity != null ? sourceEntity : entity);
        if (player != null) {
            TdcDataAdvancementTriggers.PLAYER_TRIGGER_SCULK_SHRIEKER.trigger(player, event, world, pos, null);
        }
    }
}

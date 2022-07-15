package com.github.thedeathlycow.tdcdata.mixin.stat_trackers;

import com.github.thedeathlycow.tdcdata.scoreboard.stat.TdcDataCustomStats;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.PickFromInventoryC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public class PickBlockPacketListenerMixin {

    @Inject(
            method = "onPickFromInventory",
            at = @At("TAIL")
    )
    private void incrementPickblockStat(PickFromInventoryC2SPacket packet, CallbackInfo ci) {
        final PlayerEntity player = ((ServerPlayNetworkHandler) (Object) this).player;
        if (!player.world.isClient) {
            player.incrementStat(TdcDataCustomStats.PICK_BLOCK);
        }
    }
}

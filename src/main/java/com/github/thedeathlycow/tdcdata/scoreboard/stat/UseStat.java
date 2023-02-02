package com.github.thedeathlycow.tdcdata.scoreboard.stat;

import com.github.thedeathlycow.tdcdata.DatapackExtensions;
import com.github.thedeathlycow.tdcdata.advancement.TdcDataAdvancementTriggers;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;

public class UseStat {
    static int useBlockCount = 0;
    public static void registerEventListeners() {
        // Right click detection
        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (!world.isClient) {
                TdcDataAdvancementTriggers.USE_ITEM.trigger((ServerPlayerEntity) player, player.getStackInHand(hand));
                player.incrementStat(TdcDataCustomStats.USE_ITEM);
            }
            return TypedActionResult.pass(ItemStack.EMPTY);
        });
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (!world.isClient && hand == Hand.MAIN_HAND) {
                player.incrementStat(TdcDataCustomStats.USE_BLOCK);
            }
            return ActionResult.PASS;
        });
        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (!world.isClient) {
                player.incrementStat(TdcDataCustomStats.USE_ENTITY);
            }
            return ActionResult.PASS;
        });
    }

}

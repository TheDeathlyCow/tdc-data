package com.github.thedeathlycow.tdcdata.scoreboard.stat;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.TypedActionResult;

public class UseStat {

    public static void registerEventListeners() {
        // Right click detection
        UseItemCallback.EVENT.register((player, world, hand) -> {
            player.incrementStat(TdcDataCustomStats.USE);

            return TypedActionResult.pass(ItemStack.EMPTY);
        });
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            player.incrementStat(TdcDataCustomStats.USE);

            return ActionResult.PASS;
        });
        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            player.incrementStat(TdcDataCustomStats.USE);

            return ActionResult.PASS;
        });
    }

}

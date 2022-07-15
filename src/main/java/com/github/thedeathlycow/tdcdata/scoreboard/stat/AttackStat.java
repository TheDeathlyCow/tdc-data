package com.github.thedeathlycow.tdcdata.scoreboard.stat;

import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.util.ActionResult;

public class AttackStat {


    public static void registerEventListeners() {

        // Left click detection
        AttackEntityCallback.EVENT.register(((player, world, hand, entity, hitResult) -> {
            player.incrementStat(TdcDataCustomStats.ATTACK);

            return ActionResult.PASS;
        }));
        AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
            player.incrementStat(TdcDataCustomStats.ATTACK);

            return ActionResult.PASS;
        });
    }

}

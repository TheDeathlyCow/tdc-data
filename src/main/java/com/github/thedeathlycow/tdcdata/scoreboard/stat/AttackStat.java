package com.github.thedeathlycow.tdcdata.scoreboard.stat;

import com.github.thedeathlycow.tdcdata.DatapackExtensions;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.util.ActionResult;

public class AttackStat {


    public static void registerEventListeners() {

        // Left click detection
        AttackEntityCallback.EVENT.register(((player, world, hand, entity, hitResult) -> {
            if (!world.isClient) {
                player.incrementStat(TdcDataCustomStats.ATTACK_ENTITY);
            }
            return ActionResult.PASS;
        }));
        AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
            if (!world.isClient) {
                player.incrementStat(TdcDataCustomStats.ATTACK_BLOCK);
            }
            return ActionResult.PASS;
        });
    }

}

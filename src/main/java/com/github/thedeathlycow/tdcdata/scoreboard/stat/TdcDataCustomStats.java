package com.github.thedeathlycow.tdcdata.scoreboard.stat;

import com.github.thedeathlycow.tdcdata.DatapackExtensions;
import net.minecraft.stat.StatFormatter;
import net.minecraft.stat.Stats;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class TdcDataCustomStats {

    public static final Identifier ATTACK = new Identifier(DatapackExtensions.MODID, "attack");
    public static final Identifier USE = new Identifier(DatapackExtensions.MODID, "use");
    public static final Identifier PICK_BLOCK = new Identifier(DatapackExtensions.MODID, "pick_block");

    public static void registerCustomStats() {
        Registry.register(Registry.CUSTOM_STAT, ATTACK, ATTACK);
        Registry.register(Registry.CUSTOM_STAT, USE, USE);
        Registry.register(Registry.CUSTOM_STAT, PICK_BLOCK, PICK_BLOCK);

        Stats.CUSTOM.getOrCreateStat(ATTACK, StatFormatter.DEFAULT);
        Stats.CUSTOM.getOrCreateStat(USE, StatFormatter.DEFAULT);
        Stats.CUSTOM.getOrCreateStat(PICK_BLOCK, StatFormatter.DEFAULT);

        AttackStat.registerEventListeners();
        UseStat.registerEventListeners();
    }

}

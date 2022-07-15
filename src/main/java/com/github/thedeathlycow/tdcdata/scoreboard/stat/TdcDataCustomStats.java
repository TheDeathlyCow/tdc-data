package com.github.thedeathlycow.tdcdata.scoreboard.stat;

import com.github.thedeathlycow.tdcdata.DatapackExtensions;
import net.minecraft.stat.StatFormatter;
import net.minecraft.stat.Stats;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class TdcDataCustomStats {

    public static final Identifier ATTACK = new Identifier(DatapackExtensions.MODID, "attack");
    public static final Identifier USE = new Identifier(DatapackExtensions.MODID, "use");

    public static void registerCustomStats() {
        Registry.register(Registry.CUSTOM_STAT, ATTACK, ATTACK);
        Registry.register(Registry.CUSTOM_STAT, USE, USE);

        Stats.CUSTOM.getOrCreateStat(ATTACK, StatFormatter.DEFAULT);
        Stats.CUSTOM.getOrCreateStat(USE, StatFormatter.DEFAULT);

        AttackStat.registerEventListeners();
        UseStat.registerEventListeners();
    }

}

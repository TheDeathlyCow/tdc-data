package com.github.thedeathlycow.tdcdata.scoreboard.stat;

import com.github.thedeathlycow.tdcdata.DatapackExtensions;
import net.minecraft.stat.StatFormatter;
import net.minecraft.stat.Stats;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class TdcDataCustomStats {

    public static final Identifier ATTACK_ENTITY = new Identifier(DatapackExtensions.MODID, "attack_entity");
    public static final Identifier ATTACK_BLOCK = new Identifier(DatapackExtensions.MODID, "attack_block");
    public static final Identifier USE_ITEM = new Identifier(DatapackExtensions.MODID, "use_item");
    public static final Identifier USE_ENTITY = new Identifier(DatapackExtensions.MODID, "use_entity");
    public static final Identifier USE_BLOCK = new Identifier(DatapackExtensions.MODID, "use_block");
    public static final Identifier PICK_BLOCK = new Identifier(DatapackExtensions.MODID, "pick_block");

    public static void registerCustomStats() {
        Registry.register(Registry.CUSTOM_STAT, ATTACK_ENTITY, ATTACK_ENTITY);
        Registry.register(Registry.CUSTOM_STAT, ATTACK_BLOCK, ATTACK_BLOCK);
        Registry.register(Registry.CUSTOM_STAT, USE_ITEM, USE_ITEM);
        Registry.register(Registry.CUSTOM_STAT, USE_ENTITY, USE_ENTITY);
        Registry.register(Registry.CUSTOM_STAT, USE_BLOCK, USE_BLOCK);
        Registry.register(Registry.CUSTOM_STAT, PICK_BLOCK, PICK_BLOCK);

        Stats.CUSTOM.getOrCreateStat(ATTACK_ENTITY, StatFormatter.DEFAULT);
        Stats.CUSTOM.getOrCreateStat(ATTACK_BLOCK, StatFormatter.DEFAULT);
        Stats.CUSTOM.getOrCreateStat(USE_ITEM, StatFormatter.DEFAULT);
        Stats.CUSTOM.getOrCreateStat(USE_ENTITY, StatFormatter.DEFAULT);
        Stats.CUSTOM.getOrCreateStat(USE_BLOCK, StatFormatter.DEFAULT);
        Stats.CUSTOM.getOrCreateStat(PICK_BLOCK, StatFormatter.DEFAULT);

        AttackStat.registerEventListeners();
        UseStat.registerEventListeners();
    }

}

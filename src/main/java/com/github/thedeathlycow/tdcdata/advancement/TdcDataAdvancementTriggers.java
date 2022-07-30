package com.github.thedeathlycow.tdcdata.advancement;

import com.github.thedeathlycow.tdcdata.DatapackExtensions;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.util.Identifier;

public class TdcDataAdvancementTriggers {

    public static final PlayerTriggerGameEventCriterion TRIGGER_VIBRATION = new PlayerTriggerGameEventCriterion();
    public static final UseItemCriterion USE_ITEM = new UseItemCriterion();
    public static final PlayerTriggerVibrationBlockListener PLAYER_TRIGGER_SCULK_SENSOR = new PlayerTriggerVibrationBlockListener(new Identifier(DatapackExtensions.MODID, "player_trigger_sculk_sensor"));
    public static final PlayerTriggerVibrationBlockListener PLAYER_TRIGGER_SCULK_SHRIEKER = new PlayerTriggerVibrationBlockListener(new Identifier(DatapackExtensions.MODID, "player_trigger_sculk_shrieker"));
    public static final PlayerTriggerVibrationEntityListener PLAYER_ALERTS_WARDEN = new PlayerTriggerVibrationEntityListener(new Identifier(DatapackExtensions.MODID, "player_alerts_warden"));

    public static void registerTriggers() {
        Criteria.register(USE_ITEM);
        Criteria.register(TRIGGER_VIBRATION);
        Criteria.register(PLAYER_TRIGGER_SCULK_SENSOR);
        Criteria.register(PLAYER_TRIGGER_SCULK_SHRIEKER);
        Criteria.register(PLAYER_ALERTS_WARDEN);
    }

}

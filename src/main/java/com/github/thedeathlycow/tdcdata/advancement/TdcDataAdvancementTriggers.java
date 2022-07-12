package com.github.thedeathlycow.tdcdata.advancement;

import com.github.thedeathlycow.tdcdata.DatapackExtensions;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.util.Identifier;

public class TdcDataAdvancementTriggers {

    public static final PlayerTriggerGameEventCriterion TRIGGER_VIBRATION = new PlayerTriggerGameEventCriterion();
    public static final PlayerTriggerVibrationListener TRIGGER_SCULK_SENSOR = new PlayerTriggerVibrationListener(new Identifier(DatapackExtensions.MODID, "player_trigger_sculk_sensor"));
    public static final PlayerTriggerVibrationListener TRIGGER_SCULK_SHRIEKER = new PlayerTriggerVibrationListener(new Identifier(DatapackExtensions.MODID, "player_trigger_sculk_shrieker"));

    public static void registerTriggers() {
        Criteria.register(TRIGGER_VIBRATION);
        Criteria.register(TRIGGER_SCULK_SENSOR);
        Criteria.register(TRIGGER_SCULK_SHRIEKER);
    }

}

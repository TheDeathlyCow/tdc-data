package com.github.thedeathlycow.tdcdata.advancement;

import net.minecraft.advancement.criterion.Criteria;

public class TdcDataAdvancementTriggers {

    public static final PlayerTriggerGameEventCriterion TRIGGER_VIBRATION = new PlayerTriggerGameEventCriterion();

    public static void registerTriggers() {
        Criteria.register(TRIGGER_VIBRATION);
    }

}

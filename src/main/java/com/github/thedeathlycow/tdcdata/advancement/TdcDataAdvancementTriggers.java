package com.github.thedeathlycow.tdcdata.advancement;

import net.minecraft.advancement.criterion.Criteria;

public class TdcDataAdvancementTriggers {

    public static final TriggerVibrationCriterion TRIGGER_VIBRATION = new TriggerVibrationCriterion();

    public static void registerTriggers() {
        Criteria.register(TRIGGER_VIBRATION);
    }

}

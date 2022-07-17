package com.github.thedeathlycow.tdcdata.advancement;

import com.github.thedeathlycow.tdcdata.DatapackExtensions;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.advancement.criterion.ItemCriterion;
import net.minecraft.util.Identifier;

public class TdcDataAdvancementTriggers {

    public static final PlayerTriggerGameEventCriterion TRIGGER_VIBRATION = new PlayerTriggerGameEventCriterion();
    public static final UseItemCriterion USE_ITEM = new UseItemCriterion();

    public static void registerTriggers() {
        Criteria.register(TRIGGER_VIBRATION);
        Criteria.register(USE_ITEM);
    }

}

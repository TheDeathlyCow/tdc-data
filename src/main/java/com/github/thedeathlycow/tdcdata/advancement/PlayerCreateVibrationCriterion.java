package com.github.thedeathlycow.tdcdata.advancement;

import com.github.thedeathlycow.tdcdata.DatapackUtils;
import com.google.gson.JsonObject;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.advancement.criterion.AbstractCriterionConditions;
import net.minecraft.predicate.entity.AdvancementEntityPredicateDeserializer;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.util.Identifier;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.event.listener.SculkSensorListener;

public class PlayerCreateVibrationCriterion extends AbstractCriterion<PlayerCreateVibrationCriterion.Conditions> {
    static final Identifier ID = new Identifier(DatapackUtils.MODID, "player_create_vibration");

    @Override
    protected Conditions conditionsFromJson(JsonObject obj, EntityPredicate.Extended playerPredicate, AdvancementEntityPredicateDeserializer predicateDeserializer) {


        return null;
    }

    @Override
    public Identifier getId() {
        return ID;
    }

    public static class Conditions extends AbstractCriterionConditions {



        public Conditions(EntityPredicate.Extended player, GameEvent event, int frequency, SculkSensorListener listener) {
            super(PlayerCreateVibrationCriterion.ID, player);
            this.event = event;
            this.frequency = frequency;
            this.listener = listener;
        }
    }
}

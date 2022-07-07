package com.github.thedeathlycow.tdcdata.advancement;

import com.github.thedeathlycow.tdcdata.DatapackExtensions;
import com.github.thedeathlycow.tdcdata.predicate.GameEventPredicate;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.advancement.criterion.AbstractCriterionConditions;
import net.minecraft.predicate.NumberRange;
import net.minecraft.predicate.entity.AdvancementEntityPredicateDeserializer;
import net.minecraft.predicate.entity.AdvancementEntityPredicateSerializer;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

public class TriggerVibrationCriterion extends AbstractCriterion<TriggerVibrationCriterion.Conditions> {
    private static final Identifier ID = new Identifier(DatapackExtensions.MODID, "trigger_vibration");

    @Override
    protected TriggerVibrationCriterion.Conditions conditionsFromJson(JsonObject json, EntityPredicate.Extended player, AdvancementEntityPredicateDeserializer predicateDeserializer) {

        GameEventPredicate eventPredicate = GameEventPredicate.fromJson(json);
        NumberRange.IntRange frequency = null;
        if (json.has("frequency")) {
            frequency = NumberRange.IntRange.fromJson(json.get("frequency"));
        }

        return new Conditions(player, eventPredicate, frequency);
    }

    public void trigger(ServerPlayerEntity player, GameEvent event, Integer frequency) {
        super.trigger(player, (conditions) -> {
            return conditions.matches(event, frequency);
        });
    }

    @Override
    public Identifier getId() {
        return ID;
    }

    public static class Conditions extends AbstractCriterionConditions {

        @Nullable
        private final GameEventPredicate eventPredicate;
        @Nullable
        private final NumberRange.IntRange frequencyRange;

        public Conditions(EntityPredicate.Extended player, GameEventPredicate eventPredicate, NumberRange.IntRange frequencyRange) {
            super(TriggerVibrationCriterion.ID, player);
            this.eventPredicate = eventPredicate;
            this.frequencyRange = frequencyRange;
        }

        public JsonObject toJson(AdvancementEntityPredicateSerializer predicateSerializer) {
            JsonObject json = super.toJson(predicateSerializer);
            JsonElement eventJson = this.eventPredicate != null ? this.eventPredicate.toJson() : JsonNull.INSTANCE;
            json.add("event", eventJson);
            JsonElement frequencyJson = this.frequencyRange != null ? this.frequencyRange.toJson() : JsonNull.INSTANCE;
            json.add("frequency", frequencyJson);
            return json;
        }

        public boolean matches(GameEvent event, int frequency) {
            if (this.eventPredicate == null && this.frequencyRange == null) {
                return true;
            }
            if (this.eventPredicate != null && !this.eventPredicate.test(event)) {
                return false;
            } else {
                return this.frequencyRange != null && this.frequencyRange.test(frequency);
            }
        }
    }
}

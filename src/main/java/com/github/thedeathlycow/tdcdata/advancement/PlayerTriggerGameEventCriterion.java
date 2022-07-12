package com.github.thedeathlycow.tdcdata.advancement;

import com.github.thedeathlycow.tdcdata.DatapackExtensions;
import com.github.thedeathlycow.tdcdata.predicate.GameEventPredicate;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.advancement.criterion.AbstractCriterionConditions;
import net.minecraft.predicate.entity.AdvancementEntityPredicateDeserializer;
import net.minecraft.predicate.entity.AdvancementEntityPredicateSerializer;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

public class PlayerTriggerGameEventCriterion extends AbstractCriterion<PlayerTriggerGameEventCriterion.Conditions> {
    private static final Identifier ID = new Identifier(DatapackExtensions.MODID, "player_trigger_game_event");

    @Override
    protected PlayerTriggerGameEventCriterion.Conditions conditionsFromJson(JsonObject json, EntityPredicate.Extended player, AdvancementEntityPredicateDeserializer predicateDeserializer) {
        GameEventPredicate eventPredicate = GameEventPredicate.fromJson(json.get("event"));
        return new Conditions(player, eventPredicate);
    }

    public void trigger(ServerPlayerEntity player, GameEvent event) {
        super.trigger(player, (conditions) -> {
            return conditions.matches(event);
        });
    }

    @Override
    public Identifier getId() {
        return ID;
    }

    public static class Conditions extends AbstractCriterionConditions {

        @Nullable
        private final GameEventPredicate eventPredicate;

        public Conditions(EntityPredicate.Extended player, GameEventPredicate eventPredicate) {
            super(PlayerTriggerGameEventCriterion.ID, player);
            this.eventPredicate = eventPredicate;
        }

        public JsonObject toJson(AdvancementEntityPredicateSerializer predicateSerializer) {
            JsonObject json = super.toJson(predicateSerializer);
            JsonElement eventJson = this.eventPredicate != null ? this.eventPredicate.toJson() : JsonNull.INSTANCE;
            json.add("event", eventJson);
            return json;
        }

        public boolean matches(GameEvent event) {
            if (this.eventPredicate == null || this.eventPredicate == GameEventPredicate.ANY) {
                return true;
            } else {
                return this.eventPredicate.test(event);
            }
        }
    }
}

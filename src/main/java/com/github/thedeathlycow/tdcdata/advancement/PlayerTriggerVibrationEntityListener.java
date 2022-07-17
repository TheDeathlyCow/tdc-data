package com.github.thedeathlycow.tdcdata.advancement;

import com.github.thedeathlycow.tdcdata.predicate.GameEventPredicate;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.advancement.criterion.AbstractCriterionConditions;
import net.minecraft.entity.Entity;
import net.minecraft.predicate.entity.AdvancementEntityPredicateDeserializer;
import net.minecraft.predicate.entity.AdvancementEntityPredicateSerializer;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.predicate.entity.LocationPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

public class PlayerTriggerVibrationEntityListener extends AbstractCriterion<PlayerTriggerVibrationEntityListener.Conditions> {

    private final Identifier id;

    public PlayerTriggerVibrationEntityListener(Identifier id) {
        this.id = id;
    }

    @Override
    protected Conditions conditionsFromJson(JsonObject json, EntityPredicate.Extended player, AdvancementEntityPredicateDeserializer predicateDeserializer) {
        GameEventPredicate eventPredicate = GameEventPredicate.fromJson(json.get("event"));
        EntityPredicate entityPredicate = EntityPredicate.fromJson(json.get("listener"));
        return new Conditions(this.id, player, eventPredicate, entityPredicate);
    }

    public void trigger(ServerPlayerEntity player, GameEvent event, ServerWorld world, Entity entity) {
        super.trigger(player, (conditions) -> {
            return conditions.matches(event, world, entity);
        });
    }

    @Override
    public Identifier getId() {
        return this.id;
    }


    public static class Conditions extends AbstractCriterionConditions {

        @Nullable
        private final GameEventPredicate eventPredicate;
        @Nullable
        private final EntityPredicate entityPredicate;

        public Conditions(Identifier id, EntityPredicate.Extended entity, @Nullable GameEventPredicate eventPredicate, @Nullable EntityPredicate entityPredicate) {
            super(id, entity);
            this.eventPredicate = eventPredicate;
            this.entityPredicate = entityPredicate;
        }

        public JsonObject toJson(AdvancementEntityPredicateSerializer predicateSerializer) {
            JsonObject json = super.toJson(predicateSerializer);
            JsonElement eventJson = this.eventPredicate != null ? this.eventPredicate.toJson() : JsonNull.INSTANCE;
            json.add("event", eventJson);
            JsonElement locationJson = this.entityPredicate != null ? this.entityPredicate.toJson() : JsonNull.INSTANCE;
            json.add("listener", locationJson);
            return json;
        }

        public boolean matches(GameEvent event, ServerWorld world, Entity entity) {
            if (this.eventPredicate != null && !this.eventPredicate.test(event)) {
                return false;
            } else if (this.entityPredicate != null && !this.entityPredicate.test(world, entity.getPos(), entity)) {
                return false;
            } else {
                return true;
            }
        }
    }
}

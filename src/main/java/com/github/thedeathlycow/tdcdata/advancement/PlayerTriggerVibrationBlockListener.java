package com.github.thedeathlycow.tdcdata.advancement;

import com.github.thedeathlycow.tdcdata.predicate.GameEventPredicate;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.advancement.criterion.AbstractCriterionConditions;
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


public class PlayerTriggerVibrationBlockListener extends AbstractCriterion<PlayerTriggerVibrationBlockListener.Conditions> {

    private final Identifier id;

    public PlayerTriggerVibrationBlockListener(Identifier id) {
        this.id = id;
    }

    @Override
    protected Conditions conditionsFromJson(JsonObject json, EntityPredicate.Extended player, AdvancementEntityPredicateDeserializer predicateDeserializer) {
        GameEventPredicate eventPredicate = GameEventPredicate.fromJson(json.get("event"));
        LocationPredicate block = LocationPredicate.fromJson(json.get("listener"));
        return new Conditions(this.id, player, eventPredicate, block);
    }

    public void trigger(ServerPlayerEntity player, GameEvent event, ServerWorld world, BlockPos position) {
        super.trigger(player, (conditions) -> {
            return conditions.matches(event, world, position);
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
        private final LocationPredicate locationPredicate;

        public Conditions(Identifier id, EntityPredicate.Extended player, @Nullable GameEventPredicate eventPredicate, @Nullable LocationPredicate locationPredicate) {
            super(id, player);
            this.eventPredicate = eventPredicate;
            this.locationPredicate = locationPredicate;
        }

        public JsonObject toJson(AdvancementEntityPredicateSerializer predicateSerializer) {
            JsonObject json = super.toJson(predicateSerializer);
            JsonElement eventJson = this.eventPredicate != null ? this.eventPredicate.toJson() : JsonNull.INSTANCE;
            json.add("event", eventJson);
            JsonElement locationJson = this.locationPredicate != null ? this.locationPredicate.toJson() : JsonNull.INSTANCE;
            json.add("listener", locationJson);
            return json;
        }

        public boolean matches(GameEvent event, ServerWorld world, BlockPos position) {
            if (this.eventPredicate != null && !this.eventPredicate.test(event)) {
                return false;
            } else if (this.locationPredicate != null && !this.locationPredicate.test(world, position.getX(), position.getY(), position.getZ())) {
                return false;
            } else {
                return true;
            }
        }
    }
}

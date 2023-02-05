package com.github.thedeathlycow.tdcdata.advancement;

import com.github.thedeathlycow.tdcdata.DatapackExtensions;
import com.github.thedeathlycow.tdcdata.predicate.HandPredicate;
import com.google.gson.JsonObject;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.advancement.criterion.AbstractCriterionConditions;
import net.minecraft.advancement.criterion.UsingItemCriterion;
import net.minecraft.item.ItemStack;
import net.minecraft.predicate.entity.AdvancementEntityPredicateDeserializer;
import net.minecraft.predicate.entity.AdvancementEntityPredicateSerializer;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.predicate.item.ItemPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;

public class UseItemCriterion extends AbstractCriterion<UseItemCriterion.Conditions> {

    public static final Identifier ID = new Identifier(DatapackExtensions.MODID, "use_item");

    public Conditions conditionsFromJson(JsonObject json, EntityPredicate.Extended extended, AdvancementEntityPredicateDeserializer advancementEntityPredicateDeserializer) {
        ItemPredicate itemPredicate = ItemPredicate.fromJson(json.get("item"));
        HandPredicate handPredicate = HandPredicate.fromJson(json.get("hand"));
        return new Conditions(extended, itemPredicate, handPredicate);
    }

    public void trigger(ServerPlayerEntity player, ItemStack stack, Hand hand) {
        this.trigger(player, (conditions) -> {
            return conditions.test(stack, hand);
        });
    }

    @Override
    public Identifier getId() {
        return ID;
    }

    public static class Conditions extends AbstractCriterionConditions {

        private final ItemPredicate item;
        private final HandPredicate hand;

        public Conditions(EntityPredicate.Extended player, ItemPredicate item, HandPredicate hand) {
            super(ID, player);
            this.item = item;
            this.hand = hand;
        }

        public boolean test(ItemStack stack, Hand hand) {
            if (!this.hand.test(hand)) {
                return false;
            } else {
                return this.item.test(stack);
            }
        }

        public JsonObject toJson(AdvancementEntityPredicateSerializer predicateSerializer) {
            JsonObject jsonObject = super.toJson(predicateSerializer);
            jsonObject.add("item", this.item.toJson());
            return jsonObject;
        }
    }
}

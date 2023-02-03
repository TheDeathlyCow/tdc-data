package com.github.thedeathlycow.tdcdata.data.item.predicate.type;

import com.github.thedeathlycow.tdcdata.data.item.predicate.ItemPredicateWrapper;
import com.github.thedeathlycow.tdcdata.data.item.predicate.ItemPredicateWrapperType;
import com.github.thedeathlycow.tdcdata.data.item.predicate.ItemPredicateWrapperTypes;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.condition.LootConditionTypes;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.JsonSerializer;

import java.util.function.Predicate;

public class OrItemPredicateWrapper implements ItemPredicateWrapper {

    private final ItemPredicateWrapper[] terms;
    private final Predicate<ItemStack> predicate;

    public OrItemPredicateWrapper(ItemPredicateWrapper[] predicates) {
        this.predicate = LootConditionTypes.joinOr(predicates);
        this.terms = predicates;
    }

    @Override
    public ItemPredicateWrapperType getType() {
        return ItemPredicateWrapperTypes.OR;
    }

    @Override
    public boolean test(ItemStack itemStack) {
        return this.predicate.test(itemStack);
    }

    public static class Serializer implements JsonSerializer<OrItemPredicateWrapper> {
        public Serializer() {
        }

        public void toJson(JsonObject jsonObject, OrItemPredicateWrapper predicateWrapper, JsonSerializationContext jsonSerializationContext) {
            jsonObject.add(
                    "terms",
                    jsonSerializationContext.serialize(predicateWrapper.terms)
            );
        }

        public OrItemPredicateWrapper fromJson(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
            ItemPredicateWrapper[] terms = JsonHelper.deserialize(
                    jsonObject,
                    "terms",
                    jsonDeserializationContext,
                    ItemPredicateWrapper[].class
            );
            return new OrItemPredicateWrapper(terms);
        }
    }
}

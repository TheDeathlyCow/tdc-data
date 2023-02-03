package com.github.thedeathlycow.tdcdata.data.item.predicate.type;

import com.github.thedeathlycow.tdcdata.data.item.predicate.ItemPredicateWrapper;
import com.github.thedeathlycow.tdcdata.data.item.predicate.ItemPredicateWrapperType;
import com.github.thedeathlycow.tdcdata.data.item.predicate.ItemPredicateWrapperTypes;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.item.ItemStack;
import net.minecraft.predicate.item.ItemPredicate;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.JsonSerializer;

public class AtomItemPredicateWrapper implements ItemPredicateWrapper {

    private final ItemPredicate term;

    public AtomItemPredicateWrapper(ItemPredicate predicate) {
        this.term = predicate;
    }

    @Override
    public ItemPredicateWrapperType getType() {
        return ItemPredicateWrapperTypes.ATOM;
    }

    @Override
    public boolean test(ItemStack itemStack) {
        return this.term.test(itemStack);
    }

    public static class Serializer implements JsonSerializer<AtomItemPredicateWrapper> {
        public Serializer() {
        }

        public void toJson(JsonObject jsonObject, AtomItemPredicateWrapper alternativeLootCondition, JsonSerializationContext jsonSerializationContext) {
            jsonObject.add(
                    "term",
                    jsonSerializationContext.serialize(alternativeLootCondition.term)
            );
        }

        public AtomItemPredicateWrapper fromJson(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
            ItemPredicate term = JsonHelper.deserialize(
                    jsonObject,
                    "term",
                    jsonDeserializationContext,
                    ItemPredicate.class
            );
            return new AtomItemPredicateWrapper(term);
        }
    }
}

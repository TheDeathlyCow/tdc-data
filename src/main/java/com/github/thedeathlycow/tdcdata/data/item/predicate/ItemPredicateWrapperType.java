package com.github.thedeathlycow.tdcdata.data.item.predicate;

import net.minecraft.util.JsonSerializableType;
import net.minecraft.util.JsonSerializer;

public class ItemPredicateWrapperType extends JsonSerializableType<ItemPredicateWrapper> {

    public ItemPredicateWrapperType(JsonSerializer<? extends ItemPredicateWrapper> serializer) {
        super(serializer);
    }

}

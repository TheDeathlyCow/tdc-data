package com.github.thedeathlycow.tdcdata.data.item.predicate;

import net.minecraft.item.ItemStack;

import java.util.function.Predicate;

public interface ItemPredicateWrapper extends Predicate<ItemStack> {

    ItemPredicateWrapperType getType();
}

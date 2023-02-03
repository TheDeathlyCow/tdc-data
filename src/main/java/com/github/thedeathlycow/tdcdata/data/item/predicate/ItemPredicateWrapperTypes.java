package com.github.thedeathlycow.tdcdata.data.item.predicate;

import com.github.thedeathlycow.tdcdata.data.item.predicate.type.AndItemPredicateWrapper;
import com.github.thedeathlycow.tdcdata.data.item.predicate.type.AtomItemPredicateWrapper;
import com.github.thedeathlycow.tdcdata.data.item.predicate.type.OrItemPredicateWrapper;

public class ItemPredicateWrapperTypes {

    public static final ItemPredicateWrapperType ATOM = new ItemPredicateWrapperType(new AtomItemPredicateWrapper.Serializer());

    public static final ItemPredicateWrapperType OR = new ItemPredicateWrapperType(new OrItemPredicateWrapper.Serializer());

    public static final ItemPredicateWrapperType AND = new ItemPredicateWrapperType(new AndItemPredicateWrapper.Serializer());

}

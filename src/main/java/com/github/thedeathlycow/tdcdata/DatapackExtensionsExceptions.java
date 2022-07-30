package com.github.thedeathlycow.tdcdata;

import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

public class DatapackExtensionsExceptions {
    public static final DynamicCommandExceptionType ENTITY_NOT_LIVING_EXCEPTION = new DynamicCommandExceptionType(
            (targetName) -> DatapackExtensionsTranslator.translateAsText("commands.errors.entity_not_living", targetName));

    public static final DynamicCommandExceptionType ENTITY_DEAD_EXCEPTION = new DynamicCommandExceptionType(
            (targetName) -> DatapackExtensionsTranslator.translateAsText("commands.errors.entity_dead", targetName));

    public static final DynamicCommandExceptionType ENTITY_HAND_EMPTY = new DynamicCommandExceptionType(
            (targetName) -> DatapackExtensionsTranslator.translateAsText("commands.errors.entity_hand_empty", targetName));

    public static final SimpleCommandExceptionType ENTITY_CANNOT_JUMP_EXCEPTION = new SimpleCommandExceptionType(
            DatapackExtensionsTranslator.translateAsText("commands.errors.entity_cannot_jump"));

}

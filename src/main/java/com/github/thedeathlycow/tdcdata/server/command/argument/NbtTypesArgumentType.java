package com.github.thedeathlycow.tdcdata.server.command.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.command.CommandSource;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public class NbtTypesArgumentType implements ArgumentType<String> {
    private static final Collection<String> EXAMPLES = Arrays.asList("byte", "short", "int", "long", "float", "double", "string", "boolean");

    private NbtTypesArgumentType() {
    }

    public static NbtTypesArgumentType types() {
        return new NbtTypesArgumentType();
    }

    public static String getTypes(final CommandContext<?> context, final String name) {
        return context.getArgument(name, String.class);
    }

    @Override
    public String parse(StringReader stringReader) {
        return stringReader.readUnquotedString();
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(final CommandContext<S> context, final SuggestionsBuilder builder) {
        return CommandSource.suggestMatching(EXAMPLES, builder);
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}
package com.github.thedeathlycow.tdcdata.server.command.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public class HandArgumentType implements ArgumentType<Hand> {
    private static final Collection<String> EXAMPLES = Arrays.asList("mainhand", "offhand");
    private static final DynamicCommandExceptionType NOT_A_HAND_EXCEPTINON = new DynamicCommandExceptionType(
            (handName) -> {
                return Text.literal(String.format("%s is not a valid hand", handName));
            }
    );

    private HandArgumentType() {
    }

    public static HandArgumentType hand() {
        return new HandArgumentType();
    }

    public static Hand getHand(final CommandContext<?> context, final String name) {
        return context.getArgument(name, Hand.class);
    }

    @Override
    public Hand parse(StringReader stringReader) throws CommandSyntaxException {
        String hand = stringReader.readUnquotedString();
        if (hand.equals("mainhand")) {
            return Hand.MAIN_HAND;
        } else if (hand.equals("offhand")) {
            return Hand.OFF_HAND;
        } else {
            throw NOT_A_HAND_EXCEPTINON.create(hand);
        }
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

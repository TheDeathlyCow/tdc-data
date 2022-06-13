package com.github.thedeathlycow.tdcdata.server.command.argument;

import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.OperationArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.MathHelper;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;

/**
 * A scoreboard operator with only one operand.
 * <p>
 * This class is adapted from {@link net.minecraft.command.argument.OperationArgumentType}
 * for operations with only a single operand.
 */
public class UnaryOperationArgumentType implements ArgumentType<UnaryOperation> {

    private static final Collection<String> EXAMPLES = Arrays.asList("~", "!", "ln", "sqrt", "rand");
    private static final SimpleCommandExceptionType INVALID_OPERATION = new SimpleCommandExceptionType(new TranslatableText("arguments.operation.invalid"));
    private static final SimpleCommandExceptionType NOT_POSITIVE_INPUT = new SimpleCommandExceptionType(new LiteralText("Input score must be positive!"));

    public static final SimpleCommandExceptionType DIVISION_ZERO_EXCEPTION = new SimpleCommandExceptionType(new TranslatableText("arguments.operation.div0"));
    private static final double NATURAL_LOG_OF_2 = Math.log(2);

    public static UnaryOperationArgumentType unaryOperation() {
        return new UnaryOperationArgumentType();
    }

    public static UnaryOperation getUnaryOperation(CommandContext<ServerCommandSource> context, String name) {
        return context.getArgument(name, UnaryOperation.class);
    }

    @Override
    public UnaryOperation parse(StringReader reader) throws CommandSyntaxException {
        if (!reader.canRead()) {
            throw INVALID_OPERATION.create();
        } else {
            int i = reader.getCursor();

            while (reader.canRead() && reader.peek() != ' ') {
                reader.skip();
            }

            return getOperator(reader.getString().substring(i, reader.getCursor()));
        }
    }

    private static UnaryOperation.UnaryIntOperation getOperator(String operator) throws CommandSyntaxException {
        return switch (operator) {
            case "~" -> (a) -> ~a;
            case "!" -> (a) -> a == 0 ? 1 : 0;
            case "abs" -> Math::abs;
            case "sqrt" -> getDoubleFunction(Math::sqrt);
            case "ln" -> getDoubleFunction(Math::log);
            case "log2" -> getDoubleFunction((a) -> Math.log(a) / NATURAL_LOG_OF_2);
            case "log" -> getDoubleFunction(Math::log10);
            case "rand" -> (a) -> ThreadLocalRandom.current().nextInt();
            default -> throw INVALID_OPERATION.create();
        };
    }

    private static UnaryOperation.UnaryIntOperation getDoubleFunction(ScaledDoubleFunction function) {
        return getDoubleFunction(function, true);
    }

    private static UnaryOperation.UnaryIntOperation getDoubleFunction(ScaledDoubleFunction function, boolean mustBePositive) {
        return (a) -> {

            if (mustBePositive && a <= 0) {
                throw NOT_POSITIVE_INPUT.create();
            }

            final double result = function.apply(a);
            return (int) result;
        };
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return CommandSource.suggestMatching(ImmutableList.of("~", "!", "abs", "sqrt", "ln", "log2", "log", "rand"), builder);
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    @FunctionalInterface
    public interface ScaledDoubleFunction {
        double apply(double thetaRadians);
    }
}

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
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.MathHelper;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;

/**
 * A scoreboard operator with only one operand.
 *
 * This class is adapted from {@link net.minecraft.command.argument.OperationArgumentType}
 * for operations with only a single operand.
 */
public class UnaryOperationArgumentType implements ArgumentType<UnaryOperation> {

    private static final Collection<String> EXAMPLES = Arrays.asList("~", "!", "++", "rand", "sqrt", "sin");
    private static final SimpleCommandExceptionType INVALID_OPERATION = new SimpleCommandExceptionType(new TranslatableText("arguments.operation.invalid"));
    private static final SimpleCommandExceptionType BAD_BOUND = new SimpleCommandExceptionType(new LiteralText("Random bound must be positive!"));
    private static final int DOUBLE_FUNCTION_SCALE = 1000;

    @Override
    public UnaryOperation parse(StringReader reader) throws CommandSyntaxException {
        if (!reader.canRead()) {
            throw INVALID_OPERATION.create();
        } else {
            int i = reader.getCursor();

            while(reader.canRead() && reader.peek() != ' ') {
                reader.skip();
            }

            return getOperator(reader.getString().substring(i, reader.getCursor()));
        }
    }

    private static UnaryOperation getOperator(String operator) throws CommandSyntaxException {
        return switch (operator) {
            default -> getIntOperator(operator);
        };
    }

    private static UnaryOperation.UnaryIntOperation getIntOperator(String operator) throws CommandSyntaxException {
        return switch (operator) {
            case "-" -> (a) -> -a;
            case "--" -> (a) -> a--;
            case "++" -> (a) -> a++;
            case "~" -> (a) -> ~a;
            case "!" -> (a) -> a == 0 ? 1 : 0;
            case "rand" -> (a) -> {
                if (a > 0) {
                    return ThreadLocalRandom.current().nextInt(a);
                } else {
                    throw BAD_BOUND.create();
                }
            };
            case "sqrt" -> getDoubleFunction(Math::sqrt);
            case "sin" -> getDoubleFunction(Math::sin);
            case "cos" -> getDoubleFunction(Math::cos);
            case "tan" -> getDoubleFunction(Math::tan);
            case "asin" -> getDoubleFunction(Math::asin);
            case "acos" -> getDoubleFunction(Math::acos);
            case "atan" -> getDoubleFunction(Math::atan);
            case "sinh" -> getDoubleFunction(Math::sinh);
            case "cosh" -> getDoubleFunction(Math::cosh);
            case "tanh" -> getDoubleFunction(Math::tanh);
            default -> throw INVALID_OPERATION.create();
        };
    }

    private static UnaryOperation.UnaryIntOperation getDoubleFunction(ScaledDoubleFunction function) {
        return (thetaDegrees) -> {
            final double radians = Math.toRadians(thetaDegrees);
            final double result = function.apply(radians);
            return MathHelper.floor(DOUBLE_FUNCTION_SCALE * result);
        };
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return CommandSource.suggestMatching(ImmutableList.of("-", "--", "++", "~", "!", "rand", "sqrt", "sin", "cos", "tan", "asin", "acos", "atan", "sinh", "cosh", "tanh"), builder);
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

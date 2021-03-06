package com.github.thedeathlycow.tdcdata.mixin.command.argument;

import com.github.thedeathlycow.tdcdata.server.command.argument.UnaryOperationArgumentType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.command.argument.OperationArgumentType;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Arrays;
import java.util.stream.Stream;

@Mixin(OperationArgumentType.class)
public class CustomOperationMixin {

    private static final SimpleCommandExceptionType tdcdata$NON_POSITIVE_INPUT = new SimpleCommandExceptionType(Text.literal("Input score must be positive!"));

    @ModifyArg(
            method = "listSuggestions",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/command/CommandSource;suggestMatching([Ljava/lang/String;Lcom/mojang/brigadier/suggestion/SuggestionsBuilder;)Ljava/util/concurrent/CompletableFuture;"
            )
    )
    private String[] addCustomOperatorsToSuggestions(String[] candidates) {
        String[] bitwiseSuggestions = {"<<=", ">>=", ">>>=", "&=", "|=", "^=", "**=", "log_b"};
        return Stream.concat(Arrays.stream(candidates), Arrays.stream(bitwiseSuggestions))
                .toArray(String[]::new);
    }

    @Inject(
            method = "getIntOperator",
            at = @At(
                    value = "HEAD"
            ),
            cancellable = true
    )
    private static void parseCustomOperators(String operator, CallbackInfoReturnable<OperationArgumentType.IntOperator> cir) {

        OperationArgumentType.IntOperator parsedOperator = switch (operator) {
            case "<<=" -> (a, b) -> a << b;
            case ">>=" -> (a, b) -> (a >> b);
            case ">>>=" -> (a, b) -> (a >>> b);
            case "&=" -> (a, b) -> a & b;
            case "|=" -> (a, b) -> a | b;
            case "^=" -> (a, b) -> a ^ b;
            case "**=" -> (a, b) -> MathHelper.floor(Math.pow(a, b));
            case "log_b" -> (a, b) -> {
                if (a <= 0) {
                    throw tdcdata$NON_POSITIVE_INPUT.create();
                }
                if (b <= 1) {
                    throw UnaryOperationArgumentType.INVALID_BASE.create();
                }
                return (int) (Math.log(a) / Math.log(b));
            };
            default -> null;
        };

        if (parsedOperator != null) {
            cir.setReturnValue(parsedOperator);
        }
    }

}

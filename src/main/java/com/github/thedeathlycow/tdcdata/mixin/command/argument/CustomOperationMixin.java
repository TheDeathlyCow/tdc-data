package com.github.thedeathlycow.tdcdata.mixin.command.argument;

import net.minecraft.command.argument.OperationArgumentType;
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

    @ModifyArg(
            method = "listSuggestions",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/command/CommandSource;suggestMatching([Ljava/lang/String;Lcom/mojang/brigadier/suggestion/SuggestionsBuilder;)Ljava/util/concurrent/CompletableFuture;"
            )
    )
    private String[] addCustomOperatorsToSuggestions(String[] candidates) {
        String[] bitwiseSuggestions = {"<<=", ">>=", ">>>=", "&=", "|=", "^=", "exp"};
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
            case "exp" -> (a, b) -> MathHelper.floor(Math.pow(a, b));
            default -> null;
        };

        if (parsedOperator != null) {
            cir.setReturnValue(parsedOperator);
        }
    }

}

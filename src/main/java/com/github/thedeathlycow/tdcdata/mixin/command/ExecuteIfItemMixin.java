package com.github.thedeathlycow.tdcdata.mixin.command;

import com.github.thedeathlycow.tdcdata.server.command.ExecuteIfItemCommand;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.ItemSlotArgumentType;
import net.minecraft.command.argument.ItemStackArgumentType;
import net.minecraft.server.command.ExecuteCommand;
import net.minecraft.server.command.ItemCommand;
import net.minecraft.server.command.ServerCommandSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

@Mixin(ExecuteCommand.class)
public abstract class ExecuteIfItemMixin {

    @Inject(
            method = "addConditionArguments",
            at = @At(
                    value = "TAIL"
            )
    )
    private static void addItemCondition(CommandNode<ServerCommandSource> root, LiteralArgumentBuilder<ServerCommandSource> argumentBuilder, boolean positive, CallbackInfoReturnable<ArgumentBuilder<ServerCommandSource, ?>> cir) {

        var slotExecutionPoint = argument("sourceSlot", ItemSlotArgumentType.itemSlot()).then(
                AddConditionLogicInvoker.invoke(root, argument("item", ItemStackArgumentType.itemStack()), positive, (context) -> {
                    return ExecuteIfItemCommand.testItemCondition(context.getSource(), null, 0, null);
                })
        );

        argumentBuilder
                .then(
                        literal("entity").then(
                                argument("source", EntityArgumentType.entity()).then(
                                        slotExecutionPoint
                                )
                        )
                )
                .then(
                        literal("block").then(
                                argument("source", BlockPosArgumentType.blockPos()).then(
                                        slotExecutionPoint
                                )
                        )
                );
    }
}

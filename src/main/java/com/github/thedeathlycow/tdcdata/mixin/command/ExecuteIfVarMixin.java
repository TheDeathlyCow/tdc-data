package com.github.thedeathlycow.tdcdata.mixin.command;

import com.github.thedeathlycow.tdcdata.server.command.ExecuteIfItemCommand;
import com.github.thedeathlycow.tdcdata.server.command.VarCommand;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.ItemPredicateArgumentType;
import net.minecraft.command.argument.ItemSlotArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.ExecuteCommand;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Predicate;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

@Mixin(ExecuteCommand.class)
public abstract class ExecuteIfVarMixin {

    private static final String GET_FAIL = "Variable %s does not exist";

    @Invoker("addConditionLogic")
    public static ArgumentBuilder<ServerCommandSource, ?> invokeAddConditonLogic(CommandNode<ServerCommandSource> root, ArgumentBuilder<ServerCommandSource, ?> builder, boolean positive, ExecuteCommand.Condition condition) {
        throw new AssertionError();
    }

    @Inject(
            method = "addConditionArguments",
            at = @At(
                    value = "TAIL"
            )
    )
    private static void addItemCondition(CommandNode<ServerCommandSource> root, LiteralArgumentBuilder<ServerCommandSource> argumentBuilder, boolean positive, CommandRegistryAccess registryAccess, CallbackInfoReturnable<ArgumentBuilder<ServerCommandSource, ?>> cir) {

        var entityItemCondition = literal("entity").then(
                argument("target", EntityArgumentType.entity()).then(
                        argument("slot", ItemSlotArgumentType.itemSlot()).then(
                                invokeAddConditonLogic(root, argument("itemPredicate", ItemPredicateArgumentType.itemPredicate(registryAccess)), positive, (context) -> {
                                    Entity entity = EntityArgumentType.getEntity(context, "target");
                                    int slot = ItemSlotArgumentType.getItemSlot(context, "slot");
                                    Predicate<ItemStack> itemPredicate = ItemPredicateArgumentType.getItemStackPredicate(context, "itemPredicate");
                                    return ExecuteIfItemCommand.testEntityItemCondition(context.getSource(), entity, slot, itemPredicate);
                                })
                        )
                )
        );

        argumentBuilder.then(
                literal("tdcdata.var")
                        .then(argument("key", StringArgumentType.string())
                                .then(literal("contains")
                                        .then(literal("value")
                                                .then(invokeAddConditonLogic(root, argument("value", StringArgumentType.string()), positive, (context) -> {
                                                    String key = StringArgumentType.getString(context, "key");
                                                    if (!VarCommand.map.containsKey(key)) {
                                                        Text msg = Text.literal(String.format(GET_FAIL, key));
                                                        context.getSource().sendError(msg);
                                                        return false;
                                                    }

                                                    String keyValue = VarCommand.map.get(key);
                                                    String value = StringArgumentType.getString(context, "value");
                                                    return keyValue.contains(value);
                                                })))
                                        .then(literal("var")
                                                .then(invokeAddConditonLogic(root, argument("key2", StringArgumentType.string()), positive, context -> {
                                                    String key = StringArgumentType.getString(context, "key");
                                                    String key2 = StringArgumentType.getString(context, "key2");
                                                    if (!VarCommand.map.containsKey(key)) {
                                                        Text msg = Text.literal(String.format(GET_FAIL, key));
                                                        context.getSource().sendError(msg);
                                                        return false;
                                                    }

                                                    String keyValue = VarCommand.map.get(key);
                                                    if (!VarCommand.map.containsKey(key2)) {
                                                        Text msg = Text.literal(String.format(GET_FAIL, key2));
                                                        context.getSource().sendError(msg);
                                                        return false;
                                                    }

                                                    String keyValue2 = VarCommand.map.get(key2);
                                                    return keyValue.contains(keyValue2);
                                                }))))
                        .then(literal("equals")
                                .then(literal("value")
                                        .then(invokeAddConditonLogic(root, argument("value", StringArgumentType.string()), positive, (context) -> {
                                            String key = StringArgumentType.getString(context, "key");
                                            if (!VarCommand.map.containsKey(key)) {
                                                Text msg = Text.literal(String.format(GET_FAIL, key));
                                                context.getSource().sendError(msg);
                                                return false;
                                            }

                                            String keyValue = VarCommand.map.get(key);
                                            String value = StringArgumentType.getString(context, "value");
                                            return keyValue.equals(value);
                                        })))
                                .then(literal("var")
                                        .then(invokeAddConditonLogic(root, argument("key2", StringArgumentType.string()), positive, context -> {
                                            String key = StringArgumentType.getString(context, "key");
                                            String key2 = StringArgumentType.getString(context, "key2");
                                            if (!VarCommand.map.containsKey(key)) {
                                                Text msg = Text.literal(String.format(GET_FAIL, key));
                                                context.getSource().sendError(msg);
                                                return false;
                                            }

                                            String keyValue = VarCommand.map.get(key);
                                            if (!VarCommand.map.containsKey(key2)) {
                                                Text msg = Text.literal(String.format(GET_FAIL, key2));
                                                context.getSource().sendError(msg);
                                                return false;
                                            }

                                            String keyValue2 = VarCommand.map.get(key2);
                                            return keyValue.equals(keyValue2);
                                        }))))));
    }
}

package com.github.thedeathlycow.tdcdata.mixin.command;

import com.github.thedeathlycow.tdcdata.server.command.ExecuteIfItemCommand;
import com.github.thedeathlycow.tdcdata.server.command.argument.EntityPoseArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.ItemPredicateArgumentType;
import net.minecraft.command.argument.ItemSlotArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.ExecuteCommand;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Predicate;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

@Mixin(ExecuteCommand.class)
public abstract class ExecuteIfItemMixin {

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
    private static void addPoseCondition(CommandNode<ServerCommandSource> root, LiteralArgumentBuilder<ServerCommandSource> argumentBuilder, boolean positive, CommandRegistryAccess commandRegistryAccess, CallbackInfoReturnable<ArgumentBuilder<ServerCommandSource, ?>> cir) {
        var entityPoseCondition = literal("tdcdata.inpose").then(
                argument("target", EntityArgumentType.entity()).then(
                        invokeAddConditonLogic(root, argument("pose", EntityPoseArgumentType.entityPose()), positive, (context) -> {
                            Entity target = EntityArgumentType.getEntity(context, "target");
                            EntityPose pose = EntityPoseArgumentType.getEntityPose(context, "pose").getPose();
                            return target.isInPose(pose);
                        })
                )
        );

        argumentBuilder.then(
                entityPoseCondition
        );
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

        var blockItemCondition = literal("block").then(
                argument("pos", BlockPosArgumentType.blockPos()).then(
                        argument("slot", ItemSlotArgumentType.itemSlot()).then(
                                invokeAddConditonLogic(root, argument("itemPredicate", ItemPredicateArgumentType.itemPredicate(registryAccess)), positive, (context) -> {
                                    BlockPos pos = BlockPosArgumentType.getBlockPos(context, "pos");
                                    int slot = ItemSlotArgumentType.getItemSlot(context, "slot");
                                    Predicate<ItemStack> itemPredicate = ItemPredicateArgumentType.getItemStackPredicate(context, "itemPredicate");
                                    return ExecuteIfItemCommand.testBlockItemCondition(context.getSource(), pos, slot, itemPredicate);
                                })
                        )
                )
        );

        argumentBuilder.then(
                literal("tdcdata.item").then(
                        entityItemCondition
                ).then(
                        blockItemCondition
                )
        );
    }
}

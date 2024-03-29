package com.github.thedeathlycow.tdcdata.mixin.command;

import com.github.thedeathlycow.tdcdata.server.command.ExecuteStoreAttributeCommand;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.RegistryKeyArgumentType;
import net.minecraft.command.argument.UuidArgumentType;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.server.command.ExecuteCommand;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.registry.Registry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

@Mixin(ExecuteCommand.class)
public abstract class ExecuteStoreAttributeCommandMixin {

    @Inject(
            method = "addStoreArguments",
            at = @At("HEAD")
    )
    private static void addAttributeStoreArgument(LiteralCommandNode<ServerCommandSource> node, LiteralArgumentBuilder<ServerCommandSource> builder, boolean requestResult, CallbackInfoReturnable<ArgumentBuilder<ServerCommandSource, ?>> cir) {
        builder.then(literal("tdcdata.attribute").then(
                argument("target", EntityArgumentType.entity()).then(
                        argument("attribute", RegistryKeyArgumentType.registryKey(Registry.ATTRIBUTE_KEY))
                                .then(
                                        literal("base").then(
                                                argument("scale", DoubleArgumentType.doubleArg())
                                                        .redirect(node, context -> ExecuteStoreAttributeCommand.executeStoreAttribute(
                                                                context.getSource(),
                                                                EntityArgumentType.getEntity(context, "target"),
                                                                RegistryKeyArgumentType.getAttribute(context, "attribute"),
                                                                DoubleArgumentType.getDouble(context, "scale"),
                                                                requestResult,
                                                                null
                                                        ))
                                        )
                                )
                                .then(
                                        literal("modifier").then(
                                                argument("uuid", UuidArgumentType.uuid())
                                                        .then(
                                                                argument("name", StringArgumentType.string())
                                                                        .then(
                                                                                argument("scale", DoubleArgumentType.doubleArg())
                                                                                        .then(
                                                                                                literal("add").redirect(
                                                                                                        node, context -> ExecuteStoreAttributeCommand.executeStoreAttribute(
                                                                                                                context.getSource(),
                                                                                                                EntityArgumentType.getEntity(context, "target"),
                                                                                                                RegistryKeyArgumentType.getAttribute(context, "attribute"),
                                                                                                                DoubleArgumentType.getDouble(context, "scale"),
                                                                                                                requestResult,
                                                                                                                (value) -> new EntityAttributeModifier(
                                                                                                                        UuidArgumentType.getUuid(context, "uuid"),
                                                                                                                        StringArgumentType.getString(context, "name"),
                                                                                                                        value,
                                                                                                                        EntityAttributeModifier.Operation.ADDITION
                                                                                                                )
                                                                                                        )
                                                                                                )
                                                                                        )
                                                                                        .then(
                                                                                                literal("multiply").redirect(
                                                                                                        node, context -> ExecuteStoreAttributeCommand.executeStoreAttribute(
                                                                                                                context.getSource(),
                                                                                                                EntityArgumentType.getEntity(context, "target"),
                                                                                                                RegistryKeyArgumentType.getAttribute(context, "attribute"),
                                                                                                                DoubleArgumentType.getDouble(context, "scale"),
                                                                                                                requestResult,
                                                                                                                (value) -> new EntityAttributeModifier(
                                                                                                                        UuidArgumentType.getUuid(context, "uuid"),
                                                                                                                        StringArgumentType.getString(context, "name"),
                                                                                                                        value,
                                                                                                                        EntityAttributeModifier.Operation.MULTIPLY_TOTAL
                                                                                                                )
                                                                                                        )
                                                                                                )
                                                                                        )
                                                                                        .then(
                                                                                                literal("multiply_base").redirect(
                                                                                                        node, context -> ExecuteStoreAttributeCommand.executeStoreAttribute(
                                                                                                                context.getSource(),
                                                                                                                EntityArgumentType.getEntity(context, "target"),
                                                                                                                RegistryKeyArgumentType.getAttribute(context, "attribute"),
                                                                                                                DoubleArgumentType.getDouble(context, "scale"),
                                                                                                                requestResult,
                                                                                                                (value) -> new EntityAttributeModifier(
                                                                                                                        UuidArgumentType.getUuid(context, "uuid"),
                                                                                                                        StringArgumentType.getString(context, "name"),
                                                                                                                        value,
                                                                                                                        EntityAttributeModifier.Operation.MULTIPLY_BASE
                                                                                                                )
                                                                                                        )
                                                                                                )
                                                                                        )
                                                                        )

                                                        )
                                        )
                                )
                )
        ));
    }
}

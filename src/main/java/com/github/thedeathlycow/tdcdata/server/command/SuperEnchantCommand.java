package com.github.thedeathlycow.tdcdata.server.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import java.util.Collection;
import java.util.Iterator;

import net.minecraft.command.argument.EnchantmentArgumentType;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.Vec3ArgumentType;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.EnchantCommand;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class SuperEnchantCommand {


    private static final DynamicCommandExceptionType FAILED_ENTITY_EXCEPTION = new DynamicCommandExceptionType((entityName) -> {
        return Text.translatable("commands.enchant.failed.entity", new Object[]{entityName});
    });
    private static final DynamicCommandExceptionType FAILED_ITEMLESS_EXCEPTION = new DynamicCommandExceptionType((entityName) -> {
        return Text.translatable("commands.enchant.failed.itemless", new Object[]{entityName});
    });

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandManager.RegistrationEnvironment registryAccess) {

        dispatcher.register(
                (literal("superenchant").requires((src) -> src.hasPermissionLevel(2)))
                        .then(
                                argument("target", EntityArgumentType.entity())
                                        .then(
                                                argument("enchantment", EnchantmentArgumentType.enchantment())
                                                        .then(
                                                                argument("level", IntegerArgumentType.integer(0))
                                                                        .executes((context) -> {
                                                                                    return executeEnchant(context.getSource(),
                                                                                            EntityArgumentType.getEntity(context, "target"),
                                                                                            EnchantmentArgumentType.getEnchantment(context, "enchantment"),
                                                                                            IntegerArgumentType.getInteger(context, "level"));
                                                                                }
                                                                        )
                                                        )
                                        )
                        )
        );
    }

    private static int executeEnchant(ServerCommandSource source, Entity target, Enchantment enchantment, int level) throws CommandSyntaxException {
        if (target instanceof LivingEntity livingEntity) {
            ItemStack itemStack = livingEntity.getMainHandStack();
            if (!itemStack.isEmpty()) {
                itemStack.addEnchantment(enchantment, level);

                source.sendFeedback(
                        Text.translatable(
                                "commands.enchant.success.single",
                                enchantment.getName(level),
                                target.getDisplayName()
                        ),
                        true
                );

                return 1;
            } else {
                throw FAILED_ITEMLESS_EXCEPTION.create(livingEntity.getName().getString());
            }
        } else {
            throw FAILED_ENTITY_EXCEPTION.create(target.getName().getString());
        }
    }
}

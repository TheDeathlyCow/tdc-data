package com.github.thedeathlycow.tdcdata.server.command;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//


import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
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
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class SuperEnchantCommand {
    private static final DynamicCommandExceptionType NOT_LIVING_ENTITY_EXCEPTION = new DynamicCommandExceptionType(
            (targetName) -> {
                return Text.literal(String.format("%s is not a living entity", targetName));
            }
    );
    private static final DynamicCommandExceptionType ENTITY_ITEMLESS_EXCEPTION = new DynamicCommandExceptionType(
            (targetName) -> {
                return Text.literal(String.format("%s is currently itemless", targetName));
            }
    );

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandManager.RegistrationEnvironment registryAccess) {
        dispatcher.register(
                (literal("superenchant").requires((src) -> src.hasPermissionLevel(2)))
                        .then(argument("target", EntityArgumentType.entity())
                                .then(argument("enchantment", EnchantmentArgumentType.enchantment())
                                        .then(argument("level", IntegerArgumentType.integer(0))
                                                .executes((context) -> {
                                                    return executeEnchant(context.getSource(),
                                                            EntityArgumentType.getEntity(context, "target"),
                                                            EnchantmentArgumentType.getEnchantment(context, "enchantment"),
                                                            IntegerArgumentType.getInteger(context, "level"));
                                                })))));
    }

    private static int executeEnchant(ServerCommandSource source, Entity target, Enchantment enchantment, int level) throws CommandSyntaxException {
        if (target instanceof LivingEntity livingEntity) {
            ItemStack itemStack = livingEntity.getMainHandStack();
            if (!itemStack.isEmpty()) {
                itemStack.addEnchantment(enchantment, level);
                return 1;
            } else {
                throw ENTITY_ITEMLESS_EXCEPTION.create(livingEntity.getName().getString());
            }
        } else {
            throw NOT_LIVING_ENTITY_EXCEPTION.create(target.getName().getString());
        }
    }
}

package com.github.thedeathlycow.tdcdata.server.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.command.argument.EnchantmentArgumentType;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.EnchantCommand;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.util.Collection;
import java.util.Map;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class SuperEnchantCommand {

    private static final DynamicCommandExceptionType FAILED_INCOMPATIBLE_EXCEPTION = new DynamicCommandExceptionType(
            (itemName) -> {
                return Text.translatable("commands.enchant.failed.incompatible", itemName);
            });

    private static final DynamicCommandExceptionType FAILED_ENTITY_EXCEPTION = new DynamicCommandExceptionType(
            (targetName) -> {
                return Text.translatable("commands.enchant.failed.entity", targetName);
            }
    );


    private static final DynamicCommandExceptionType ENTITY_ITEMLESS_EXCEPTION = new DynamicCommandExceptionType(
            (targetName) -> {
                return Text.translatable("commands.enchant.failed.itemless", targetName);
            }
    );

    private static final SimpleCommandExceptionType FAILED_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.enchant.failed"));

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandManager.RegistrationEnvironment registryAccess) {

        var targets = argument("targets", EntityArgumentType.entities())
                .then(argument("enchantment", EnchantmentArgumentType.enchantment())
                        .then(argument("level", IntegerArgumentType.integer(0))
                                .executes((context) -> {
                                            return executeEnchant(
                                                    context.getSource(),
                                                    EntityArgumentType.getEntities(context, "targets"),
                                                    EnchantmentArgumentType.getEnchantment(context, "enchantment"),
                                                    IntegerArgumentType.getInteger(context, "level")
                                            );
                                        }
                                )
                        )
                );

        dispatcher.register(
                literal("tdcdata")
                        .then(
                                (literal("superenchant").requires((src) -> src.hasPermissionLevel(2)))
                                        .then(targets)
                        )
        );
    }

    private static int executeEnchant(
            ServerCommandSource source,
            Collection<? extends Entity> targets,
            Enchantment enchantment,
            int level
    ) throws CommandSyntaxException {
        int total = 0;

        for (var target : targets) {
            if (target instanceof LivingEntity livingEntity) {
                ItemStack itemStack = livingEntity.getMainHandStack();
                if (!itemStack.isEmpty()) {
                    Map<Enchantment, Integer> enchantments = EnchantmentHelper.get(itemStack);

                    if (enchantments.containsKey(enchantment)) {
                        throw FAILED_INCOMPATIBLE_EXCEPTION.create(itemStack.getItem().getName(itemStack).getString());
                    }

                    itemStack.addEnchantment(enchantment, level);
                    total++;
                } else {
                    throw ENTITY_ITEMLESS_EXCEPTION.create(livingEntity.getName().getString());
                }
            } else if (targets.size() == 1) {
                throw FAILED_ENTITY_EXCEPTION.create(target.getName().getString());
            }
        }

        if (total == 0) {
            throw FAILED_EXCEPTION.create();
        }

        if (targets.size() == 1) {
            source.sendFeedback(Text.translatable("commands.enchant.success.single", enchantment.getName(level), targets.iterator().next().getDisplayName()), true);
        } else {
            source.sendFeedback(Text.translatable("commands.enchant.success.multiple", enchantment.getName(level), targets.size()), true);
        }

        return total;
    }
}

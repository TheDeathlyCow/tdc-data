package com.github.thedeathlycow.tdcdata.server.command;

import com.github.thedeathlycow.tdcdata.DatapackExtensionsExceptions;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.minecraft.command.argument.EnchantmentArgumentType;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.util.Map;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class SuperEnchantCommand {

    private static final DynamicCommandExceptionType FAILED_INCOMPATIBLE_EXCEPTION = new DynamicCommandExceptionType((itemName) -> {
        return Text.translatable("commands.enchant.failed.incompatible", itemName);
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
                Map<Enchantment, Integer> enchantments = EnchantmentHelper.get(itemStack);

                if (enchantments.containsKey(enchantment)) {
                    throw FAILED_INCOMPATIBLE_EXCEPTION.create(itemStack.getItem().getName(itemStack).getString());
                }

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
                throw DatapackExtensionsExceptions.ENTITY_HAND_EMPTY.create(livingEntity.getName().getString());
            }
        } else {
            throw DatapackExtensionsExceptions.ENTITY_NOT_LIVING_EXCEPTION.create(target.getName().getString());
        }
    }
}

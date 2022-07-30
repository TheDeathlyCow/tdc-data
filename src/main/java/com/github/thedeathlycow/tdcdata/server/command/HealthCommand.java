package com.github.thedeathlycow.tdcdata.server.command;

import com.github.thedeathlycow.tdcdata.DatapackExtensionsExceptions;
import com.github.thedeathlycow.tdcdata.DatapackExtensionsTranslator;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class HealthCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandManager.RegistrationEnvironment registryAccess) {

        Command<ServerCommandSource> getCurrentLeaf = context -> {
            return getCurrent(context.getSource(), EntityArgumentType.getEntity(context, "target"));
        };

        var getCurrent = literal("current").executes(getCurrentLeaf);

        var getMax = literal("max")
                .executes(context -> {
                    return getMax(context.getSource(), EntityArgumentType.getEntity(context, "target"));
                });

        var getSubCommand = literal("get")
                .then(
                        argument("target", EntityArgumentType.entity())
                                .executes(getCurrentLeaf)
                                .then(getCurrent)
                                .then(getMax)
                );

        var removeSubCommand = literal("remove")
                .then(
                        argument("target", EntityArgumentType.entity())
                                .then(
                                        argument("amount", IntegerArgumentType.integer(0))
                                                .executes(context -> {
                                                    return adjust(
                                                            context.getSource(),
                                                            EntityArgumentType.getEntity(context, "target"),
                                                            -IntegerArgumentType.getInteger(context, "amount"),
                                                            true,
                                                            true
                                                    );
                                                })
                                                .then(
                                                        argument("clamp", BoolArgumentType.bool())
                                                                .executes(context -> {
                                                                    return adjust(
                                                                            context.getSource(),
                                                                            EntityArgumentType.getEntity(context, "target"),
                                                                            -IntegerArgumentType.getInteger(context, "amount"),
                                                                            BoolArgumentType.getBool(context, "clamp"),
                                                                            true
                                                                    );
                                                                })
                                                )
                                )
                );

        var addSubCommand = literal("add")
                .then(
                        argument("target", EntityArgumentType.entity())
                                .then(
                                        argument("amount", IntegerArgumentType.integer(0))
                                                .executes(context -> {
                                                    return adjust(
                                                            context.getSource(),
                                                            EntityArgumentType.getEntity(context, "target"),
                                                            IntegerArgumentType.getInteger(context, "amount"),
                                                            true,
                                                            false
                                                    );
                                                })
                                                .then(
                                                        argument("clamp", BoolArgumentType.bool())
                                                                .executes(context -> {
                                                                    return adjust(
                                                                            context.getSource(),
                                                                            EntityArgumentType.getEntity(context, "target"),
                                                                            IntegerArgumentType.getInteger(context, "amount"),
                                                                            BoolArgumentType.getBool(context, "clamp"),
                                                                            false
                                                                    );
                                                                })
                                                )
                                )
                );

        var setSubCommand = literal("set")
                .then(
                        argument("target", EntityArgumentType.entity())
                                .then(
                                        argument("amount", IntegerArgumentType.integer(0))
                                                .executes(context -> {
                                                    return set(context.getSource(),
                                                            EntityArgumentType.getEntity(context, "target"),
                                                            IntegerArgumentType.getInteger(context, "amount"),
                                                            true);
                                                })
                                                .then(
                                                        argument("clamp", BoolArgumentType.bool())
                                                                .executes(context -> {
                                                                    return set(
                                                                            context.getSource(),
                                                                            EntityArgumentType.getEntity(context, "target"),
                                                                            IntegerArgumentType.getInteger(context, "amount"),
                                                                            BoolArgumentType.getBool(context, "clamp")
                                                                    );
                                                                })
                                                )
                                )
                );

        dispatcher.register(
                (literal("health").requires((src) -> src.hasPermissionLevel(2)))
                        .then(getSubCommand)
                        .then(removeSubCommand)
                        .then(addSubCommand)
                        .then(setSubCommand)
        );
    }

    /**
     * Sets the health of each targeted entity. May clamp the amount
     * between 0 and {@link LivingEntity#getMaxHealth()} (inclusive).
     * <p>
     * Returns and displays in chat (to the source) the number of entities
     * affected by this command.
     *
     * @param source      The source of the command
     * @param target      The {@link Entity} affected by this command
     * @param amount      The of health to set on each target.
     * @param shouldClamp Whether the amount should be clamped
     * @return Returns the sum of the healths of each target after execution
     * @throws CommandSyntaxException Thrown if <code>target</code> is not a {@link LivingEntity}
     */
    private static int set(final ServerCommandSource source, Entity target, final int amount, final boolean shouldClamp) throws CommandSyntaxException {
        if (!(target instanceof LivingEntity livingTarget)) {
            throw DatapackExtensionsExceptions.ENTITY_NOT_LIVING_EXCEPTION.create(target.getDisplayName().getString());
        }

        int newHealth = shouldClamp ? MathHelper.clamp(amount, 0, MathHelper.floor(livingTarget.getMaxHealth())) : amount;

        livingTarget.setHealth(newHealth);

        Text msg = DatapackExtensionsTranslator.translateAsText("commands.health.set", target.getDisplayName().getString(), amount);
        source.sendFeedback(msg, true);
        return newHealth;
    }

    /**
     * Adds or removes health (aka <code>Health</code> in NBT data)
     * to a list of entities. May clamp the amount between 0 and {@link LivingEntity#getMaxHealth()}
     * (inclusive).
     * <p>
     * Returns and displays in chat the number of entities affected by this command.
     *
     * @param source      The source of the command
     * @param target      The targeted {@link LivingEntity} to adjust the health of.
     * @param amount      The amount to adjust by
     * @param shouldClamp Whether to clamp the final amount. Defaults to true.
     * @param isRemoving  Whether this command is removing or adding.
     * @return Returns the sum of the healths of each target after execution
     * @throws CommandSyntaxException Thrown if <code>target</code> is not a {@link LivingEntity}
     */
    private static int adjust(final ServerCommandSource source, Entity target, final int amount, final boolean shouldClamp, final boolean isRemoving) throws CommandSyntaxException {
        if (!(target instanceof LivingEntity livingTarget)) {
            throw DatapackExtensionsExceptions.ENTITY_NOT_LIVING_EXCEPTION.create(target.getDisplayName().getString());
        }

        int newHealth = MathHelper.floor(livingTarget.getHealth() + amount);
        if (shouldClamp) {
            newHealth = MathHelper.clamp(newHealth, 0, MathHelper.floor(livingTarget.getMaxHealth()));
        }

        livingTarget.setHealth(newHealth);

        // NOTE: The reason that we run livingTarget.getHealth() is because the setHealth() method may have modified the value we passed in.
        Text msg = DatapackExtensionsTranslator.translateAsText(isRemoving ? "commands.health.remove" : "commands.health.add", amount, target.getDisplayName().getString(), livingTarget.getHealth());
        source.sendFeedback(msg, true);
        return newHealth;
    }

    /**
     * Gets the current health (aka <code>Health</code> in NBT data)
     * of the targeted {@link LivingEntity}, and displays that amount in chat to the command source.
     *
     * @param source The source of the command
     * @param target The targeted {@link LivingEntity} of the command
     * @return Returns the amount of health the target has.
     * @throws CommandSyntaxException Thrown if <code>target</code> is not a {@link LivingEntity}
     */
    private static int getCurrent(final ServerCommandSource source, final Entity target) throws CommandSyntaxException {
        if (!(target instanceof LivingEntity livingTarget)) {
            throw DatapackExtensionsExceptions.ENTITY_NOT_LIVING_EXCEPTION.create(target.getDisplayName().getString());
        }

        int amount = MathHelper.floor(livingTarget.getHealth());

        Text msg = DatapackExtensionsTranslator.translateAsText("commands.health.get_current", target.getDisplayName().getString(), amount);
        source.sendFeedback(msg, true);
        return amount;
    }

    /**
     * Gets the maximum health (aka <code>Health</code> in NBT data)
     * that the targeted {@link LivingEntity} can have, and displays that amount in chat to the
     * command source.
     *
     * @param source The source of the command
     * @param target The targeted {@link LivingEntity} of the command
     * @return Returns the amount of maximum health the target can have.
     * @throws CommandSyntaxException Thrown if <code>target</code> is not a {@link LivingEntity}
     */
    private static int getMax(final ServerCommandSource source, final Entity target) throws CommandSyntaxException {
        if (!(target instanceof LivingEntity livingTarget)) {
            throw DatapackExtensionsExceptions.ENTITY_NOT_LIVING_EXCEPTION.create(target.getDisplayName().getString());
        }
        int amount = MathHelper.floor(livingTarget.getMaxHealth());

        Text msg = DatapackExtensionsTranslator.translateAsText("commands.health.get_max", target.getDisplayName().getString(), amount);
        source.sendFeedback(msg, true);
        return amount;
    }
}

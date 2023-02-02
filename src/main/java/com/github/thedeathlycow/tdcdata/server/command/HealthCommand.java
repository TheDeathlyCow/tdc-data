package com.github.thedeathlycow.tdcdata.server.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

import java.util.Collection;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class HealthCommand {

    private static final String ADD_SUCCESS_SINGLE = "Added %f health to ";
    private static final String REMOVE_SUCCESS_SINGLE = "Removed %f health from ";
    private static final String ADD_SUCCESS_MULTIPLE = "Added %f health to %d entities";
    private static final String REMOVE_SUCCESS_MULTIPLE = "Removed %f health from %d entities";
    private static final String GET_CURRENT_SUCCESS = " has %f health";
    private static final String GET_MAX_SUCCESS = " can have a maximum of %f health";

    private static final DynamicCommandExceptionType FAILED_ENTITY_EXCEPTION = new DynamicCommandExceptionType(
            (targetName) -> {
                return Text.translatable("commands.enchant.failed.entity", targetName);
            }
    );

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
                        argument("targets", EntityArgumentType.entities())
                                .then(
                                        argument("amount", FloatArgumentType.floatArg(0))
                                                .executes(context -> {
                                                    return adjust(
                                                            context.getSource(),
                                                            EntityArgumentType.getEntities(context, "targets"),
                                                            -FloatArgumentType.getFloat(context, "amount"),
                                                            true,
                                                            true
                                                    );
                                                })
                                                .then(
                                                        argument("clamp", BoolArgumentType.bool())
                                                                .executes(context -> {
                                                                    return adjust(
                                                                            context.getSource(),
                                                                            EntityArgumentType.getEntities(context, "targets"),
                                                                            -FloatArgumentType.getFloat(context, "amount"),
                                                                            BoolArgumentType.getBool(context, "clamp"),
                                                                            true
                                                                    );
                                                                })
                                                )
                                )
                );

        var addSubCommand = literal("add")
                .then(
                        argument("targets", EntityArgumentType.entities())
                                .then(
                                        argument("amount", FloatArgumentType.floatArg(0))
                                                .executes(context -> {
                                                    return adjust(
                                                            context.getSource(),
                                                            EntityArgumentType.getEntities(context, "targets"),
                                                            FloatArgumentType.getFloat(context, "amount"),
                                                            true,
                                                            false
                                                    );
                                                })
                                                .then(
                                                        argument("clamp", BoolArgumentType.bool())
                                                                .executes(context -> {
                                                                    return adjust(
                                                                            context.getSource(),
                                                                            EntityArgumentType.getEntities(context, "targets"),
                                                                            FloatArgumentType.getFloat(context, "amount"),
                                                                            BoolArgumentType.getBool(context, "clamp"),
                                                                            false
                                                                    );
                                                                })
                                                )
                                )
                );

        var setSubCommand = literal("set")
                .then(
                        argument("targets", EntityArgumentType.entities())
                                .then(
                                        argument("amount", FloatArgumentType.floatArg(0))
                                                .executes(context -> {
                                                    return set(context.getSource(),
                                                            EntityArgumentType.getEntities(context, "targets"),
                                                            FloatArgumentType.getFloat(context, "amount"),
                                                            true);
                                                })
                                                .then(
                                                        argument("clamp", BoolArgumentType.bool())
                                                                .executes(context -> {
                                                                    return set(
                                                                            context.getSource(),
                                                                            EntityArgumentType.getEntities(context, "targets"),
                                                                            FloatArgumentType.getFloat(context, "amount"),
                                                                            BoolArgumentType.getBool(context, "clamp")
                                                                    );
                                                                })
                                                )
                                )
                );

        dispatcher.register(
                literal("tdcdata")
                        .then(
                                (literal("health").requires((src) -> src.hasPermissionLevel(2)))
                                        .then(getSubCommand)
                                        .then(removeSubCommand)
                                        .then(addSubCommand)
                                        .then(setSubCommand)
                        )
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
     * @param targets     The {@link Entity}s affected by this command
     * @param amount      The of health to set on each target.
     * @param shouldClamp Whether the amount should be clamped
     * @return Returns the sum of the healths of each target after execution
     * @throws CommandSyntaxException Thrown if <code>target</code> is not a {@link LivingEntity}
     */
    private static int set(final ServerCommandSource source, Collection<? extends Entity> targets, final float amount, final boolean shouldClamp) throws CommandSyntaxException {
        int sum = 0;

        for (var target : targets) {
            if (target instanceof LivingEntity livingTarget) {
                float newHealth = shouldClamp ? MathHelper.clamp(amount, 0f, MathHelper.floor(livingTarget.getMaxHealth())) : amount;
                livingTarget.setHealth(newHealth);
                sum += MathHelper.floor(newHealth);
            } else if (targets.size() == 1) {
                throw FAILED_ENTITY_EXCEPTION.create(target.getDisplayName());
            }
        }

        MutableText msg = Text.literal("Set the health of ");
        if (targets.size() == 1) {
            msg.append(targets.iterator().next().getDisplayName())
                    .append(String.format(" to %f", amount));
        } else {
            msg.append(String.format("%d entities to %f", targets.size(), amount));
        }

        source.sendFeedback(msg, true);
        return sum;
    }

    /**
     * Adds or removes health (aka <code>Health</code> in NBT data)
     * to a list of entities. May clamp the amount between 0 and {@link LivingEntity#getMaxHealth()}
     * (inclusive).
     * <p>
     * Returns and displays in chat the number of entities affected by this command.
     *
     * @param source      The source of the command
     * @param targets     The targeted {@link Entity}s to adjust the health of.
     * @param amount      The amount to adjust by
     * @param shouldClamp Whether to clamp the final amount. Defaults to true.
     * @param isRemoving  Whether this command is removing or adding.
     * @return Returns the sum of the healths of each target after execution
     * @throws CommandSyntaxException Thrown if <code>target</code> is not a {@link LivingEntity}
     */
    private static int adjust(final ServerCommandSource source, Collection<? extends Entity> targets, final float amount, final boolean shouldClamp, final boolean isRemoving) throws CommandSyntaxException {

        int sum = 0;
        for (var target : targets) {

            if (target instanceof LivingEntity livingTarget) {
                float newHealth = livingTarget.getHealth() + amount;
                if (shouldClamp) {
                    newHealth = MathHelper.clamp(newHealth, 0f, MathHelper.floor(livingTarget.getMaxHealth()));
                }

                livingTarget.setHealth(newHealth);

                sum += MathHelper.floor(newHealth);
            } else if (targets.size() == 1) {
                throw FAILED_ENTITY_EXCEPTION.create(target.getDisplayName());
            }

        }


        MutableText msg = Text.empty();
        if (isRemoving) {
            if (targets.size() == 1) {
                buildSingleResponseMessage(msg, REMOVE_SUCCESS_SINGLE, targets.iterator().next(), -amount);
            } else {
                msg.append(String.format(REMOVE_SUCCESS_MULTIPLE, -amount, targets.size()));
            }
        } else {
            if (targets.size() == 1) {
                buildSingleResponseMessage(msg, ADD_SUCCESS_SINGLE, targets.iterator().next(), amount);
            } else {
                msg.append(String.format(ADD_SUCCESS_MULTIPLE, amount, targets.size()));
            }
        }
        source.sendFeedback(msg, true);

        return sum;
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
            throw FAILED_ENTITY_EXCEPTION.create(target.getDisplayName());
        }

        float amount = livingTarget.getHealth();

        Text msg = Text.empty()
                .append(target.getDisplayName())
                .append(String.format(GET_CURRENT_SUCCESS, amount));

        source.sendFeedback(msg, true);
        return MathHelper.floor(amount);
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
            throw FAILED_ENTITY_EXCEPTION.create(target.getDisplayName());
        }

        float amount = livingTarget.getMaxHealth();

        Text msg = Text.empty()
                .append(target.getDisplayName())
                .append(String.format(GET_MAX_SUCCESS, amount));

        source.sendFeedback(msg, true);
        return MathHelper.floor(amount);
    }

    private static void buildSingleResponseMessage(MutableText text, String message, Entity target, float amount) {
        if (target instanceof LivingEntity livingTarget) {
            text.append(String.format(message, amount))
                    .append(target.getDisplayName())
                    .append(String.format(" (now %f)", livingTarget.getHealth()));
        }
    }
}

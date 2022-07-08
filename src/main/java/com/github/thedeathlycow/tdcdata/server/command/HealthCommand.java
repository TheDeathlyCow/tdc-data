package com.github.thedeathlycow.tdcdata.server.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

import java.util.Collection;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class HealthCommand {

    private static final String ADD_SUCCESS_SINGLE = "Added %d health to %s (now %d)";
    private static final String ADD_FAIL_SINGLE = "%s is not living";
    private static final String ADD_SUCCESS_MULTIPLE = "Added %d health to %d targets";
    private static final String REMOVE_SUCCESS_SINGLE = "Removed %d health from %s (now %d)";
    private static final String REMOVE_FAIL_SINGLE = "%s is not living";
    private static final String REMOVE_SUCCESS_MULTIPLE = "Removed %d health from %d targets";
    private static final String SET_SUCCESS_SINGLE = "Set the health of %s to %d";
    private static final String SET_FAIL_SINGLE = "%s is not living";
    private static final String SET_SUCCESS_MULTIPLE = "Set the health of %d targets to %d";
    private static final String GET_CURRENT_SUCCESS = "%s has %d health";
    private static final String GET_CURRENT_FAIL = "%s is not living";
    private static final String GET_MAX_SUCCESS = "%s can have a maximum of %d health";
    private static final String GET_MAX_FAIL = "%s is not living";

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
                                        argument("amount", IntegerArgumentType.integer(0))
                                                .executes(context -> {
                                                    return adjust(
                                                            context.getSource(),
                                                            EntityArgumentType.getEntities(context, "targets"),
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
                                                                            EntityArgumentType.getEntities(context, "targets"),
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
                        argument("targets", EntityArgumentType.entities())
                                .then(
                                        argument("amount", IntegerArgumentType.integer(0))
                                                .executes(context -> {
                                                    return adjust(
                                                            context.getSource(),
                                                            EntityArgumentType.getEntities(context, "targets"),
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
                                                                            EntityArgumentType.getEntities(context, "targets"),
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
                        argument("targets", EntityArgumentType.entities())
                                .then(
                                        argument("amount", IntegerArgumentType.integer(0))
                                                .executes(context -> {
                                                    return set(context.getSource(),
                                                            EntityArgumentType.getEntities(context, "targets"),
                                                            IntegerArgumentType.getInteger(context, "amount"),
                                                            true);
                                                })
                                                .then(
                                                        argument("clamp", BoolArgumentType.bool())
                                                                .executes(context -> {
                                                                    return set(
                                                                            context.getSource(),
                                                                            EntityArgumentType.getEntities(context, "targets"),
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
     * @param targets     The collection of {@link Entity}s affected by this command
     * @param amount      The of health to set on each target.
     * @param shouldClamp Whether the amount should be clamped
     * @return Returns the sum of the healths of each target after execution
     */
    private static int set(final ServerCommandSource source, final Collection<? extends Entity> targets, final int amount, final boolean shouldClamp) {
        int sum = 0;
        for (Entity target : targets) {
            if (!(target instanceof LivingEntity livingTarget)) {
                Text msg = Text.literal(String.format(SET_FAIL_SINGLE, target.getDisplayName().getString()));
                source.sendError(msg);
                return sum;
            }

            int toSet = shouldClamp ? MathHelper.clamp(amount, 0, Math.round(livingTarget.getMaxHealth())) : amount;
            livingTarget.setHealth(toSet);
            sum += toSet;
        }

        Text msg;
        if (targets.size() == 1) {
            Entity target = targets.iterator().next();
            msg = Text.literal(String.format(SET_SUCCESS_SINGLE, target.getDisplayName().getString(), amount));
        } else {
            msg = Text.literal(String.format(SET_SUCCESS_MULTIPLE, targets.size(), amount));
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
     * @param targets     The targeted {@link LivingEntity}s to adjust the health of.
     * @param amount      The amount to adjust by
     * @param shouldClamp Whether to clamp the final amount. Defaults to true.
     * @param isRemoving  Whether this command is removing or adding.
     * @return Returns the sum of the healths of each target after execution
     */
    private static int adjust(final ServerCommandSource source, final Collection<? extends Entity> targets, final int amount, final boolean shouldClamp, final boolean isRemoving) {
        int sum = 0;
        for (Entity target : targets) {
            if (!(target instanceof LivingEntity livingTarget)) {
                Text msg = Text.literal(String.format(isRemoving ? REMOVE_FAIL_SINGLE : ADD_FAIL_SINGLE, target.getDisplayName().getString()));
                source.sendError(msg);
                return sum;
            }

            int adjustedAmount = Math.round(livingTarget.getHealth() + amount);
            if (shouldClamp) {
                adjustedAmount = MathHelper.clamp(adjustedAmount, 0, Math.round(livingTarget.getMaxHealth()));
            }
            livingTarget.setHealth(adjustedAmount);
            sum += adjustedAmount;
        }


        Text msg;
        if (targets.size() == 1) {
            Entity target = targets.iterator().next();
            String format = isRemoving ? REMOVE_SUCCESS_SINGLE : ADD_SUCCESS_SINGLE;
            int health = Math.round(((LivingEntity) target).getHealth());
            msg = Text.literal(String.format(format, MathHelper.abs(amount), target.getDisplayName().getString(), health));
        } else {
            String format = isRemoving ? REMOVE_SUCCESS_MULTIPLE : ADD_SUCCESS_MULTIPLE;
            msg = Text.literal(String.format(format, MathHelper.abs(amount), targets.size()));
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
     */
    private static int getCurrent(final ServerCommandSource source, final Entity target) {
        if (!(target instanceof LivingEntity)) {
            Text msg = Text.literal(String.format(GET_CURRENT_FAIL, target.getDisplayName().getString()));
            source.sendError(msg);
            return -1;
        }

        int amount = Math.round(((LivingEntity) target).getHealth());

        Text msg = Text.literal(String.format(GET_CURRENT_SUCCESS, target.getDisplayName().getString(), amount));
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
     */
    private static int getMax(final ServerCommandSource source, final Entity target) {
        if (!(target instanceof LivingEntity)) {
            Text msg = Text.literal(String.format(GET_MAX_FAIL, target.getDisplayName().getString()));
            source.sendError(msg);
            return -1;
        }

        int amount = Math.round(((LivingEntity) target).getMaxHealth());

        Text msg = Text.literal(String.format(GET_MAX_SUCCESS, target.getDisplayName().getString(), amount));
        source.sendFeedback(msg, true);
        return amount;
    }
}

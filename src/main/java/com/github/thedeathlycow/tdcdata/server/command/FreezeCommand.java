package com.github.thedeathlycow.tdcdata.server.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.MathHelper;

import java.util.Collection;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class FreezeCommand {

    private static final String ADD_SUCCESS_SINGLE = "Added %d frozen ticks to %s (now %d)";
    private static final String ADD_SUCCESS_MULTIPLE = "Added %d frozen ticks to %d targets";
    private static final String REMOVE_SUCCESS_SINGLE = "Removed %d frozen ticks from %s (now %d)";
    private static final String REMOVE_SUCCESS_MULTIPLE = "Removed %d frozen ticks from %d targets";
    private static final String SET_SUCCESS_SINGLE = "Set the frozen ticks of %s to %d";
    private static final String SET_SUCCESS_MULTIPLE = "Set the frozen ticks of %d targets to %d";
    private static final String GET_CURRENT_SUCCESS = "%s has %d frozen ticks";
    private static final String GET_MAX_SUCCESS = "%s can have a maximum of %d frozen ticks";
    private static final String GET_PROGRESS_SUCCESS = "%s is %d%% frozen";

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {

        Command<ServerCommandSource> getCurrentLeaf = context -> {
            return get(context.getSource(), EntityArgumentType.getEntity(context, "target"));
        };

        var getCurrent = literal("current").executes(getCurrentLeaf);

        var getMax = literal("max")
                .executes(context -> {
                    return getMax(context.getSource(), EntityArgumentType.getEntity(context, "target"));
                });

        var getProgress = literal("progress")
                .then(
                        argument("scale", FloatArgumentType.floatArg()).executes(context -> {
                                    return getProgress(
                                            context.getSource(),
                                            EntityArgumentType.getEntity(context, "target"),
                                            FloatArgumentType.getFloat(context, "scale")
                                    );
                                }
                        )
                ).executes(context -> {
                    return getProgress(
                            context.getSource(),
                            EntityArgumentType.getEntity(context, "target"),
                            100
                    );
                });

        var getSubCommand = literal("get")
                .then(
                        argument("target", EntityArgumentType.entity())
                                .executes(getCurrentLeaf)
                                .then(getCurrent)
                                .then(getMax)
                                .then(getProgress)
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
                (literal("freeze").requires((src) -> src.hasPermissionLevel(2)))
                        .then(getSubCommand)
                        .then(removeSubCommand)
                        .then(addSubCommand)
                        .then(setSubCommand)
        );
    }

    /**
     * Sets the frozen ticks (aka <code>TicksFrozen</code> in NBT data) of each
     * targeted entity. May clamp the amount between 0 and {@link Entity#getMinFreezeDamageTicks()}
     * (inclusive).
     * <p>
     * Returns and displays in chat (to the source) the number of entities
     * affected by this command.
     *
     * @param source      The source of the command
     * @param targets     The collection of {@link Entity}s affected by this command
     * @param amount      The of frozen ticks to set on each target.
     * @param shouldClamp Whether the amount should be clamped
     * @return Returns the number of entities affected by this command.
     */
    private static int set(final ServerCommandSource source, final Collection<? extends Entity> targets, final int amount, final boolean shouldClamp) {
        for (Entity target : targets) {
            int toSet = shouldClamp ? MathHelper.clamp(amount, 0, target.getMinFreezeDamageTicks()) : amount;
            target.setFrozenTicks(toSet);
        }

        Text msg;
        if (targets.size() == 1) {
            Entity target = targets.iterator().next();
            msg = new LiteralText(String.format(SET_SUCCESS_SINGLE, target.getDisplayName().asString(), amount));
        } else {
            msg = new LiteralText(String.format(SET_SUCCESS_MULTIPLE, targets.size(), amount));
        }
        source.sendFeedback(msg, true);

        return targets.size();
    }

    /**
     * Adds or removes a number of frozen ticks (aka <code>TicksFrozen</code> in NBT data)
     * to a list of entities. May clamp the amount between 0 and {@link Entity#getMinFreezeDamageTicks()}
     * (inclusive).
     * <p>
     * Returns and displays in chat the number of entities affected by this command.
     *
     * @param source      The source of the command
     * @param targets     The targeted {@link Entity}s to adjust the frozen ticks of.
     * @param amount      The amount to adjust by
     * @param shouldClamp Whether to clamp the final amount. Defaults to true.
     * @param isRemoving  Whether this command is removing or adding.
     * @return Returns the number of entities affected by this command.
     */
    private static int adjust(final ServerCommandSource source, final Collection<? extends Entity> targets, final int amount, final boolean shouldClamp, final boolean isRemoving) {

        for (Entity target : targets) {
            int adjustedAmount = target.getFrozenTicks() + amount;
            if (shouldClamp) {
                adjustedAmount = MathHelper.clamp(amount, 0, target.getMinFreezeDamageTicks());
            }
            target.setFrozenTicks(adjustedAmount);
        }


        Text msg;
        if (targets.size() == 1) {
            Entity target = targets.iterator().next();
            String format = isRemoving ? REMOVE_SUCCESS_SINGLE : ADD_SUCCESS_SINGLE;
            msg = new LiteralText(String.format(format, MathHelper.abs(amount), target.getDisplayName().asString(), target.getFrozenTicks()));
        } else {
            String format = isRemoving ? REMOVE_SUCCESS_MULTIPLE : ADD_SUCCESS_MULTIPLE;
            msg = new LiteralText(String.format(format, MathHelper.abs(amount), targets.size()));
        }
        source.sendFeedback(msg, true);

        return targets.size();
    }

    /**
     * Gets the current number of frozen ticks (aka <code>TicksFrozen</code> in NBT data)
     * of the targeted {@link Entity}, and displays that amount in chat to the command source.
     *
     * @param source The source of the command
     * @param target The targeted {@link Entity} of the command
     * @return Returns the amount of frozen ticks the target has.
     */
    private static int get(final ServerCommandSource source, final Entity target) {
        int amount = target.getFrozenTicks();
        Text msg = new LiteralText(String.format(GET_CURRENT_SUCCESS, target.getDisplayName().asString(), amount));
        source.sendFeedback(msg, true);
        return amount;
    }

    /**
     * Gets the maximum number of frozen ticks (aka <code>TicksFrozen</code> in NBT data)
     * that the targeted {@link Entity} can have, and displays that amount in chat to the
     * command source. Usually this is 140, but mods may be able to adjust this number.
     *
     * @param source The source of the command
     * @param target The targeted {@link Entity} of the command
     * @return Returns the amount of maximum number of frozen ticks the target can have.
     */
    private static int getMax(final ServerCommandSource source, final Entity target) {
        int amount = target.getMinFreezeDamageTicks();
        Text msg = new LiteralText(String.format(GET_MAX_SUCCESS, target.getDisplayName().asString(), amount));
        source.sendFeedback(msg, true);
        return amount;
    }

    /**
     * Gets the freezing tick progress of the targeted {@link Entity}, scales it
     * by the given scale, and displays this result in chat. Essentially a way
     * to invoke {@link Entity#getFreezingScale()} in game.
     * <p>
     * The result can be formally expressed as <code>floor({@link Entity#getFreezingScale()} * scale)</code>
     * </p>
     *
     * @param source The source of the command
     * @param target The targeted {@link Entity} of the command
     * @param scale  The scale by which to multiply the result of {@link Entity#getFreezingScale()} by.
     * @return Returns the amount of maximum number of frozen ticks the target can have.
     */
    private static int getProgress(final ServerCommandSource source, final Entity target, final float scale) {
        float progress = target.getFreezingScale();
        int amount = MathHelper.floor(progress * scale);
        Text msg = new LiteralText(String.format(GET_PROGRESS_SUCCESS, target.getDisplayName().asString(), amount));
        source.sendFeedback(msg, true);
        return amount;
    }
}

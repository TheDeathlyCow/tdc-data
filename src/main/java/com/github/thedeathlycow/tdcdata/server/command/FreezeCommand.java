package com.github.thedeathlycow.tdcdata.server.command;

import com.github.thedeathlycow.tdcdata.DatapackExtensionsTranslator;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

import java.util.Collection;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class FreezeCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandManager.RegistrationEnvironment registryAccess) {

        Command<ServerCommandSource> getCurrentLeaf = context -> {
            return getCurrent(context.getSource(), EntityArgumentType.getEntity(context, "target"));
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
     * @return Returns the sum of the freezing ticks of each target after execution
     */
    private static int set(final ServerCommandSource source, final Collection<? extends Entity> targets, final int amount, final boolean shouldClamp) {
        int sum = 0;
        for (Entity target : targets) {
            int toSet = shouldClamp ? MathHelper.clamp(amount, 0, target.getMinFreezeDamageTicks()) : amount;
            target.setFrozenTicks(toSet);
            sum += toSet;
        }

        Text msg;
        if (targets.size() == 1) {
            Entity target = targets.iterator().next();
            msg = DatapackExtensionsTranslator.translateAsText("commands.freeze.set_single", target.getDisplayName().getString(), amount);
        } else {
            msg = DatapackExtensionsTranslator.translateAsText("commands.freeze.set_multiple", targets.size(), amount);
        }
        source.sendFeedback(msg, true);

        return sum;
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
     * @return Returns the sum of the freezing ticks of each target after execution
     */
    private static int adjust(final ServerCommandSource source, final Collection<? extends Entity> targets, final int amount, final boolean shouldClamp, final boolean isRemoving) {
        int sum = 0;
        for (Entity target : targets) {
            int adjustedAmount = target.getFrozenTicks() + amount;
            if (shouldClamp) {
                adjustedAmount = MathHelper.clamp(adjustedAmount, 0, target.getMinFreezeDamageTicks());
            }
            target.setFrozenTicks(adjustedAmount);
            sum += adjustedAmount;
        }


        Text msg;
        if (targets.size() == 1) {
            Entity target = targets.iterator().next();
            String format = isRemoving ? "commands.freeze.remove_single" : "commands.freeze.add_single";
            msg = DatapackExtensionsTranslator.translateAsText(format, MathHelper.abs(amount), target.getDisplayName().getString(), target.getFrozenTicks());
        } else {
            String format = isRemoving ? "commands.freeze.remove_multiple" : "commands.freeze.add_multiple";
            msg = DatapackExtensionsTranslator.translateAsText(format, MathHelper.abs(amount), targets.size());
        }
        source.sendFeedback(msg, true);

        return sum;
    }

    /**
     * Gets the current number of frozen ticks (aka <code>TicksFrozen</code> in NBT data)
     * of the targeted {@link Entity}, and displays that amount in chat to the command source.
     *
     * @param source The source of the command
     * @param target The targeted {@link Entity} of the command
     * @return Returns the amount of frozen ticks the target has.
     */
    private static int getCurrent(final ServerCommandSource source, final Entity target) {
        int amount = target.getFrozenTicks();
        Text msg = DatapackExtensionsTranslator.translateAsText("commands.freeze.get_current", target.getDisplayName().getString(), amount);
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
        Text msg = DatapackExtensionsTranslator.translateAsText("commands.freeze.get_max", target.getDisplayName().getString(), amount);
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
        Text msg = DatapackExtensionsTranslator.translateAsText("commands.freeze.get_progress", target.getDisplayName().getString(), amount);
        source.sendFeedback(msg, true);
        return amount;
    }
}

package com.github.thedeathlycow.tdcdata.server.command;

import com.github.thedeathlycow.tdcdata.server.command.argument.HandArgumentType;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;

import java.util.Locale;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class SwingCommand {

    private static final String SWING_SUCCESS = "Swung %s's %s";

    private static final DynamicCommandExceptionType NOT_LIVING_ENTITY_EXCEPTION = new DynamicCommandExceptionType(
            (targetName) -> {
                return Text.literal(String.format("%s is not a living entity", targetName));
            }
    );

    private static final DynamicCommandExceptionType TARGET_IS_DEAD_EXCEPTION = new DynamicCommandExceptionType(
            (targetName) -> {
                return Text.literal(String.format("%s is not alive", targetName));
            }
    );

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandManager.RegistrationEnvironment registryAccess) {
        dispatcher.register(
                (literal("swing").requires((src) -> src.hasPermissionLevel(2)))
                        .then(argument("target", EntityArgumentType.entity())
                                .then(argument("hand", HandArgumentType.hand())
                                        .executes(context -> {
                                            return executeSwing(
                                                    context.getSource(),
                                                    EntityArgumentType.getEntity(context, "target"),
                                                    HandArgumentType.getHand(context, "hand")
                                            );
                                        })))
        );
    }


    private static int executeSwing(final ServerCommandSource source, Entity target, Hand hand) throws CommandSyntaxException {

        if (target instanceof LivingEntity livingTarget) {
            if (!target.isAlive()) {
                throw TARGET_IS_DEAD_EXCEPTION.create(target.getDisplayName().getString());
            }

            livingTarget.swingHand(hand, true);
            String handName = hand.name().toLowerCase().replace('_', ' ');
            Text msg = Text.literal(String.format(SWING_SUCCESS, target.getDisplayName().getString(), handName));
            source.sendFeedback(msg, true);

            return 1;
        } else {
            throw NOT_LIVING_ENTITY_EXCEPTION.create(target.getDisplayName().getString());
        }
    }
}

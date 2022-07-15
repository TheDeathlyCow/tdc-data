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

    private static final DynamicCommandExceptionType NOT_LIVING_PLAYER_EXCEPTION = new DynamicCommandExceptionType(
            (targetName) -> {
                return Text.literal(String.format("%s is not alive", targetName));
            }
    );

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandManager.RegistrationEnvironment registryAccess) {
        dispatcher.register(
                (literal("swing").requires((src) -> src.hasPermissionLevel(2)))
                        .then(argument("target", EntityArgumentType.player())
                                .then(argument("hand", HandArgumentType.hand())
                                        .executes(context -> {
                                            PlayerEntity player = EntityArgumentType.getPlayer(context, "target");
                                            Hand hand = HandArgumentType.getHand(context, "hand");

                                            if (!player.isLiving()) {
                                                throw NOT_LIVING_PLAYER_EXCEPTION.create(player.getDisplayName().getString());
                                            }

                                            player.swingHand(hand, true);
                                            String handName = hand.name().toLowerCase().replace('_', ' ');
                                            Text msg = Text.literal(String.format(SWING_SUCCESS, player.getDisplayName().getString(), handName));
                                            context.getSource().sendFeedback(msg, true);

                                            return 0;
                                        })))
        );
    }
}

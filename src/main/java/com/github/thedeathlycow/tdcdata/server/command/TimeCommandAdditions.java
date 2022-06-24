package com.github.thedeathlycow.tdcdata.server.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.time.Instant;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class TimeCommandAdditions {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandManager.RegistrationEnvironment registryAccess) {

        var querySysTime = literal("tdcdata.epoch")
                .executes((context) -> {
                    return executeQuery(context.getSource(), (int) (Instant.now().getEpochSecond() % Integer.MAX_VALUE));
                });

        dispatcher.register(
                literal("time").requires(source -> source.hasPermissionLevel(2))
                        .then(literal("query")
                                .then(querySysTime)
                        )
        );
    }

    private static int executeQuery(ServerCommandSource source, int time) {
        source.sendFeedback(Text.translatable("commands.time.query", time), false);
        return time;
    }
}

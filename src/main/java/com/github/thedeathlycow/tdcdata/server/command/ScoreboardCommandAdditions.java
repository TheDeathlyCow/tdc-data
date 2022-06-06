package com.github.thedeathlycow.tdcdata.server.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.command.argument.ScoreHolderArgumentType;
import net.minecraft.command.argument.ScoreboardObjectiveArgumentType;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ScoreboardPlayerScore;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ScoreboardCommand;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.TranslatableText;

import java.util.Collection;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class ScoreboardCommandAdditions {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        var notOperator = literal("tdcdata.not")
                .then(
                        argument("targets", ScoreHolderArgumentType.scoreHolders()).suggests(ScoreHolderArgumentType.SUGGESTION_PROVIDER).then(
                                argument("objective", ScoreboardObjectiveArgumentType.scoreboardObjective()).executes(
                                        (context) -> {
                                            return executeNot(
                                                    context.getSource(),
                                                    ScoreHolderArgumentType.getScoreboardScoreHolders(context, "targets"),
                                                    ScoreboardObjectiveArgumentType.getWritableObjective(context, "objective")
                                            );
                                        }
                                )
                        )
                );

        dispatcher.register(
                literal("scoreboard").requires(src -> src.hasPermissionLevel(2)).then(
                        literal("players").then(
                                notOperator
                        )
                )
        );
    }

    private static int executeNot(ServerCommandSource source, Collection<String> targets, ScoreboardObjective objective) {
        Scoreboard scoreboard = source.getServer().getScoreboard();
        int sum = 0;
        for (String target : targets) {
            ScoreboardPlayerScore score = scoreboard.getPlayerScore(target, objective);
            final int value = ~(score.getScore());
            sum += value;
            score.setScore(value);
        }

        if (targets.size() == 1) {
            source.sendFeedback(new TranslatableText("commands.scoreboard.players.operation.success.single", objective.toHoverableText(), targets.iterator().next(), sum), true);
        } else {
            source.sendFeedback(new TranslatableText("commands.scoreboard.players.operation.success.multiple", objective.toHoverableText(), targets.size()), true);
        }

        return sum;
    }
}

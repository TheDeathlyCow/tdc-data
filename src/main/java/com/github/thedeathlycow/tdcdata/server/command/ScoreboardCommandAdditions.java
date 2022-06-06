package com.github.thedeathlycow.tdcdata.server.command;

import com.github.thedeathlycow.tdcdata.server.command.argument.UnaryOperation;
import com.github.thedeathlycow.tdcdata.server.command.argument.UnaryOperationArgumentType;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.argument.ScoreHolderArgumentType;
import net.minecraft.command.argument.ScoreboardObjectiveArgumentType;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ScoreboardPlayerScore;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;

import java.util.Collection;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class ScoreboardCommandAdditions {

    private static final String RANDOM_SUCCESS = "Generated %s";

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {

        var randomOperation = literal("tdcdata.random")
                .executes(
                        context -> {
                            return executeRandom(context.getSource());
                        }
                );

        var unaryOperation = literal("tdcdata.unaryOperation")
                .then(
                        argument("operation", UnaryOperationArgumentType.unaryOperation()).then(
                                argument("targets", ScoreHolderArgumentType.scoreHolders()).suggests(ScoreHolderArgumentType.SUGGESTION_PROVIDER).then(
                                        argument("objective", ScoreboardObjectiveArgumentType.scoreboardObjective()).executes(
                                                (context) -> {
                                                    return executeUnaryOperation(
                                                            context.getSource(),
                                                            ScoreHolderArgumentType.getScoreboardScoreHolders(context, "targets"),
                                                            ScoreboardObjectiveArgumentType.getWritableObjective(context, "objective"),
                                                            UnaryOperationArgumentType.getUnaryOperation(context, "operation")
                                                    );
                                                }
                                        )
                                )
                        )
                );

        dispatcher.register(
                literal("scoreboard").requires(src -> src.hasPermissionLevel(2)).then(
                        literal("players")
                                .then(unaryOperation)
                                .then(randomOperation)
                )
        );
    }

    /**
     * Generates a random integer and displays it to
     * the command source.
     *
     * @param source Command source.
     * @return Returns the random number.
     */
    private static int executeRandom(ServerCommandSource source) {
        int result = source.getWorld().random.nextInt();
        source.sendFeedback(new LiteralText(String.format(RANDOM_SUCCESS, result)), true);
        return result;
    }

    /**
     * Permforms a unary operation on each target's score, and stores
     * the result back into the said score.
     *
     * @param source    The command source
     * @param targets   The targets to execute the operation on
     * @param objective The {@link ScoreboardObjective} to execute the operation on
     * @param operation The {@link UnaryOperation} to execute
     * @return Returns the sum of the result score of each target after execution.
     * @throws CommandSyntaxException Thrown if the operation has invalid operands.
     */
    private static int executeUnaryOperation(ServerCommandSource source, Collection<String> targets, ScoreboardObjective objective, UnaryOperation operation) throws CommandSyntaxException {
        Scoreboard scoreboard = source.getServer().getScoreboard();
        int sum = 0;
        for (String target : targets) {
            ScoreboardPlayerScore score = scoreboard.getPlayerScore(target, objective);
            operation.apply(score);
            sum += score.getScore();
        }

        if (targets.size() == 1) {
            source.sendFeedback(new TranslatableText("commands.scoreboard.players.operation.success.single", objective.toHoverableText(), targets.iterator().next(), sum), true);
        } else {
            source.sendFeedback(new TranslatableText("commands.scoreboard.players.operation.success.multiple", objective.toHoverableText(), targets.size()), true);
        }

        return sum;
    }
}

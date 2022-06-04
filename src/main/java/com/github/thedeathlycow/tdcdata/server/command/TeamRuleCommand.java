package com.github.thedeathlycow.tdcdata.server.command;

import com.github.thedeathlycow.tdcdata.scoreboard.RuledTeam;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import net.minecraft.command.argument.TeamArgumentType;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.command.TeamCommand;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class TeamRuleCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        var ruleSubCommand = literal("rule")
                .then(
                        argument("team", TeamArgumentType.team())
                                .then(
                                        literal("keepInventory")
                                                .then(
                                                        argument("keepInventory", BoolArgumentType.bool())
                                                                .executes(
                                                                        context -> {
                                                                            return keepInventory(
                                                                                    context.getSource(),
                                                                                    TeamArgumentType.getTeam(context, "team"),
                                                                                    BoolArgumentType.getBool(context, "keepInventory")
                                                                            );
                                                                        }
                                                                )
                                                )
                                )
                );

        dispatcher.register(
                (literal("team").requires((src) -> src.hasPermissionLevel(2)))
                        .then(ruleSubCommand)
        );
    }

    /**
     * Sets keep inventory for the given team to the given value,
     * and sends the result in chat to the command source.
     *
     * @param source The source of the command
     * @param team   The team to set the keep inventory rule for
     * @param value  The value to set for keep inventory
     * @return Returns 1 to indicate success
     */
    private static int keepInventory(ServerCommandSource source, Team team, boolean value) {
        ((RuledTeam) team).tdcdata$setKeepInventory(value);
        Text msg = new TranslatableText("commands.tdcdata.team.rule.keepinventory.success", team, value);
        source.sendFeedback(msg, true);
        return 1;
    }
}

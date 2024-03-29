package com.github.thedeathlycow.tdcdata.server.command;

import com.github.thedeathlycow.tdcdata.scoreboard.RuledTeam;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.command.argument.TeamArgumentType;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class TeamModifyCommandAdditions {

    private static final SimpleCommandExceptionType KEEP_INVENTORY_UNCHANGED_EXCEPTION = new SimpleCommandExceptionType(
            Text.literal("Nothing changed. Keep Inventory already has that value")
    );

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandManager.RegistrationEnvironment registryAccess) {
        var keepInventoryRule = argument("team", TeamArgumentType.team())
                .then(
                        literal("tdcdata.keepInventory")
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
                );

        dispatcher.register(
                (literal("team").requires((src) -> src.hasPermissionLevel(2)))
                        .then(
                                literal("modify")
                                        .then(keepInventoryRule)
                        )
        );
    }

    /**
     * Sets keep inventory for the given team to the given value,
     * and sends the result in chat to the command source.
     *
     * @param source The source of the command
     * @param team   The team to set the keep inventory rule for
     * @param value  The value to set for keep inventory
     * @return Returns a result 0
     * @throws CommandSyntaxException Thrown if the current value of keep inventory
     *                                is already equal to the given value.
     */
    private static int keepInventory(ServerCommandSource source, Team team, boolean value) throws CommandSyntaxException {

        RuledTeam ruledTeam = ((RuledTeam) team);

        if (ruledTeam.tdcdata$shouldKeepInventory() == value) {
            throw KEEP_INVENTORY_UNCHANGED_EXCEPTION.create();
        } else {
            ruledTeam.tdcdata$setKeepInventory(value);

            Text msg = Text.literal("Set Keep Inventory for team ")
                    .append(team.getDisplayName())
                    .append(String.format(" to %s", value));

            source.sendFeedback(msg, true);
        }

        return 0;
    }
}

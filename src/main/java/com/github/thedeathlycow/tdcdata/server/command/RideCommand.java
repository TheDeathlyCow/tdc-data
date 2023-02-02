package com.github.thedeathlycow.tdcdata.server.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;


public class RideCommand {

    private static final DynamicCommandExceptionType SAME_TARGET_EXCEPTION = new DynamicCommandExceptionType(
            (targetName) -> {
                return Text.empty()
                        .append((Text) targetName)
                        .append(Text.literal(" cannot ride itself"));
            }
    );

    private static final SimpleCommandExceptionType CANNOT_RIDE_PLAYERS_EXCEPTION = new SimpleCommandExceptionType(Text.literal("Players cannot be ridden"));

    private static final SimpleCommandExceptionType NOT_RIDING_ANYTHING_EXCEPTION = new SimpleCommandExceptionType(Text.literal("Target is not riding anything"));

    private static final DynamicCommandExceptionType CANNOT_RIDE_EXCEPTION = new DynamicCommandExceptionType(
            (target) -> {
                return Text.empty()
                        .append((Text) target)
                        .append(Text.literal(" is not rideable"));
            }
    );

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandManager.RegistrationEnvironment registryAccess) {
        var ride = argument("target", EntityArgumentType.entity())
                .then(
                        literal("mount")
                                .then(argument("vehicle", EntityArgumentType.entity())
                                        .executes(
                                                context -> {
                                                    return executeMount(
                                                            context.getSource(),
                                                            EntityArgumentType.getEntity(context, "target"),
                                                            EntityArgumentType.getEntity(context, "vehicle"),
                                                            true
                                                    );
                                                }
                                        )
                                        .then(
                                                argument("force", BoolArgumentType.bool())
                                                        .executes(
                                                                context -> {
                                                                    return executeMount(
                                                                            context.getSource(),
                                                                            EntityArgumentType.getEntity(context, "target"),
                                                                            EntityArgumentType.getEntity(context, "vehicle"),
                                                                            BoolArgumentType.getBool(context, "force")
                                                                    );
                                                                }
                                                        )
                                        )
                                )

                )
                .then(
                        literal("dismount")
                                .executes(context -> {
                                            return executeDismount(
                                                    context.getSource(),
                                                    EntityArgumentType.getEntity(context, "target")
                                            );
                                        }
                                )
                );

        dispatcher.register(
                literal("tdcdata")
                        .then(
                                (literal("ride").requires((src) -> src.hasPermissionLevel(2)))
                                        .then(ride)
                        )
        );
    }

    private static int executeMount(ServerCommandSource source, Entity target, Entity vehicle, boolean force) throws CommandSyntaxException {

        if (target.getId() == vehicle.getId()) {
            throw SAME_TARGET_EXCEPTION.create(target.getDisplayName());
        }

        if (vehicle.isPlayer()) {
            throw CANNOT_RIDE_PLAYERS_EXCEPTION.create();
        }

        if (target.startRiding(vehicle, force)) {
            Text msg = Text.empty()
                    .append(target.getDisplayName())
                    .append(Text.literal(" started riding "))
                    .append(vehicle.getDisplayName());
            source.sendFeedback(msg, true);
            return 0;
        } else {
            throw CANNOT_RIDE_EXCEPTION.create(vehicle.getDisplayName());
        }
    }

    private static int executeDismount(final ServerCommandSource source, Entity target) throws CommandSyntaxException {
        if (target.getVehicle() != null) {
            target.stopRiding();
            Text msg = Text.literal("Dismounted ") // new way of making text lol
                    .append(target.getDisplayName());
            source.sendFeedback(msg, true);
        } else {
            throw NOT_RIDING_ANYTHING_EXCEPTION.create();
        }
        return 0;
    }
}

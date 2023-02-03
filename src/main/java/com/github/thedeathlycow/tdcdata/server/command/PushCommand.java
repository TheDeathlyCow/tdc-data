package com.github.thedeathlycow.tdcdata.server.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.PosArgument;
import net.minecraft.command.argument.Vec3ArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class PushCommand {

    public static final SimpleCommandExceptionType ONLY_RELATIVE_COORDINATES_EXCEPTION = new SimpleCommandExceptionType(
            Text.literal("Can only have relative coordinates!")
    );

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandManager.RegistrationEnvironment registryAccess) {
        var motion = argument("target", EntityArgumentType.entity())
                .then(argument("force", Vec3ArgumentType.vec3(false))
                        .executes(context -> {
                                    return executeMove(context.getSource(),
                                            EntityArgumentType.getEntity(context, "target"),
                                            Vec3ArgumentType.getPosArgument(context, "force"));
                                }
                        )
                );

        dispatcher.register(
                literal("tdcdata")
                        .then(
                                (literal("push").requires((src) -> src.hasPermissionLevel(2)))
                                        .then(motion)
                        )
        );
    }

    private static int executeMove(final ServerCommandSource source, Entity target, PosArgument force) throws CommandSyntaxException {

        if (!force.isXRelative() || !force.isYRelative() || !force.isZRelative()) {
            throw ONLY_RELATIVE_COORDINATES_EXCEPTION.create();
        }

        Vec3d motionVector = force.toAbsolutePos(source).subtract(target.getPos());

        target.addVelocity(motionVector.x, motionVector.y, motionVector.z);

        if (target instanceof ServerPlayerEntity player) {
            player.networkHandler.sendPacket(new EntityVelocityUpdateS2CPacket(player.getId(), motionVector));
        }

        Text msg = Text.literal(String.format("Applied %s motion to ", motionVector))
                .append(target.getDisplayName());

        source.sendFeedback(msg, true);

        return (int) motionVector.length();
    }
}

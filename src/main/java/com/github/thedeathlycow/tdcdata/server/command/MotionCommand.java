package com.github.thedeathlycow.tdcdata.server.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.Vec3ArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class MotionCommand {

    private static final String MOVE_SUCCESS = "Animated move for %s with force %s";

    private static final DynamicCommandExceptionType NOT_LIVING_ENTITY_EXCEPTION = new DynamicCommandExceptionType(
            (targetName) -> {
                return Text.literal(String.format("%s is not a living entity", targetName));
            }
    );

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandManager.RegistrationEnvironment registryAccess) {
        dispatcher.register(
                (literal("motion").requires((src) -> src.hasPermissionLevel(2)))
                        .then(argument("target", EntityArgumentType.entity())
                                .then(argument("offset", Vec3ArgumentType.vec3())
                                        .executes(context -> {
                                            return executeMove(context.getSource(),
                                                    EntityArgumentType.getEntity(context, "target"),
                                                    Vec3ArgumentType.getVec3(context, "offset"));
                                        })))
        );
    }

    private static int executeMove(final ServerCommandSource source, Entity target, Vec3d amount) throws CommandSyntaxException {
        Vec3d relative = source.getPosition().subtract(amount);
        relative = relative.negate();
        target.addVelocity(relative.x, relative.y, relative.z);

        //if (source.getPlayer() != null) {
        //    ServerPlayerEntity player = source.getPlayer();
        //    player.networkHandler.sendPacket(new EntityVelocityUpdateS2CPacket(target.getId(), new Vec3d(0, 0.5, 0)));
        //}

        if (target instanceof ServerPlayerEntity player) {
            player.networkHandler.sendPacket(new EntityVelocityUpdateS2CPacket(target.getId(), relative));
        }

        Text msg = Text.literal(String.format(MOVE_SUCCESS, target.getDisplayName().getString(), relative));
        source.sendFeedback(msg, true);

        return 1;
    }
}

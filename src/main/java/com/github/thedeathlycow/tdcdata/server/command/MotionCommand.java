package com.github.thedeathlycow.tdcdata.server.command;

import com.github.thedeathlycow.tdcdata.DatapackExtensionsTranslator;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.argument.EntityArgumentType;
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

public class MotionCommand {
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

        if (target instanceof ServerPlayerEntity player) {
            player.networkHandler.sendPacket(new EntityVelocityUpdateS2CPacket(target.getId(), relative));
        }

        Text msg = DatapackExtensionsTranslator.translateAsText("commands.motion.move", target.getDisplayName().getString(), relative);
        source.sendFeedback(msg, true);

        return 1;
    }
}

package com.github.thedeathlycow.tdcdata.server.command;

import com.github.thedeathlycow.tdcdata.server.command.argument.HandArgumentType;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.minecraft.client.util.math.Vector3d;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.passive.PolarBearEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.HealthUpdateS2CPacket;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;

import java.util.Objects;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class PlayAnimationCommand {

    private static final String SWING_SUCCESS = "Animated swing of %s's %s";
    private static final String HURT_SUCCESS = "Animated hurt for %s";
    private static final String JUMP_SUCCESS = "Animated jump for %s with intensity %s";
    private static final String WARN_SUCCESS = "Animated warn for %s with state %s";

    private static final DynamicCommandExceptionType NOT_LIVING_ENTITY_EXCEPTION = new DynamicCommandExceptionType(
            (targetName) -> {
                return Text.literal(String.format("%s is not a living entity", targetName));
            }
    );

    private static final DynamicCommandExceptionType NOT_POLAR_BEAR_ENITY_EXCEPTION = new DynamicCommandExceptionType(
            (targetName) -> {
                return Text.literal(String.format("%s is not a polar bear", targetName));
            }
    );

    private static final DynamicCommandExceptionType TARGET_IS_DEAD_EXCEPTION = new DynamicCommandExceptionType(
            (targetName) -> {
                return Text.literal(String.format("%s is not alive", targetName));
            }
    );

    private static final DynamicCommandExceptionType TARGET_IS_NOT_DAMAGEABLE_EXCEPTION = new DynamicCommandExceptionType(
            (targetName) -> {
                return Text.literal(String.format("%s is not damageable", targetName));
            }
    );

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandManager.RegistrationEnvironment registryAccess) {
        dispatcher.register(
                (literal("playanimation").requires((src) -> src.hasPermissionLevel(2)))
                        .then(argument("target", EntityArgumentType.entity())
                                .then(literal("swing")
                                        .then(argument("hand", HandArgumentType.hand())
                                                .executes(context -> {
                                                    return executeSwing(
                                                            context.getSource(),
                                                            EntityArgumentType.getEntity(context, "target"),
                                                            HandArgumentType.getHand(context, "hand")
                                                    );
                                                })))
                                .then(literal("hurt")
                                        .executes(context -> {
                                            return executeHurt(
                                                    context.getSource(),
                                                    EntityArgumentType.getEntity(context, "target")
                                            );
                                        }))
                                .then(literal("jump")
                                        .then(argument("intensity", FloatArgumentType.floatArg())
                                                .executes(context -> {
                                                    return executeJump(
                                                            context.getSource(),
                                                            EntityArgumentType.getEntity(context, "target"),
                                                            FloatArgumentType.getFloat(context, "intensity")
                                                    );
                                                }))
                                        .executes(context -> {
                                            return executeJump(
                                                    context.getSource(),
                                                    EntityArgumentType.getEntity(context, "target"),
                                                    0.5f
                                            );
                                        }))
                                .then(literal("warn")
                                        .then(argument("state", BoolArgumentType.bool())
                                                .executes(context -> {
                                                    return executeWarn(
                                                            context.getSource(),
                                                            EntityArgumentType.getEntity(context, "target"),
                                                            BoolArgumentType.getBool(context, "state")
                                                    );
                                                })))));
    }


    private static int executeSwing(final ServerCommandSource source, Entity target, Hand hand) throws CommandSyntaxException {

        if (target instanceof LivingEntity livingTarget) {
            if (!target.isAlive()) {
                throw TARGET_IS_DEAD_EXCEPTION.create(target.getDisplayName().getString());
            }

            livingTarget.swingHand(hand, true);
            String handName = hand.name().toLowerCase().replace('_', ' ');
            Text msg = Text.literal(String.format(SWING_SUCCESS, target.getDisplayName().getString(), handName));
            source.sendFeedback(msg, true);

            return 1;
        } else {
            throw NOT_LIVING_ENTITY_EXCEPTION.create(target.getDisplayName().getString());
        }
    }

    private static int executeHurt(final ServerCommandSource source, Entity target) throws CommandSyntaxException {

        if (target instanceof LivingEntity livingTarget) {
            if (!target.isAlive()) {
                throw TARGET_IS_DEAD_EXCEPTION.create(target.getDisplayName().getString());
            }

            if (!livingTarget.canTakeDamage()) {
                throw TARGET_IS_NOT_DAMAGEABLE_EXCEPTION.create(target.getDisplayName().getString());
            }

            livingTarget.damage(DamageSource.GENERIC, 0);


            //livingTarget.animateDamage();

            //if (target instanceof ServerPlayerEntity player) {
            //    // Send a damage entity packet to the player
            //    player.networkHandler.sendPacket(new HealthUpdateS2CPacket(player.getHealth(), player.getHungerManager().getFoodLevel(), player.getHungerManager().getSaturationLevel()));
            //    player.networkHandler.onPlayerInteractEntity(PlayerInteractEntityC2SPacket.attack(target, false));
            //    player.networkHandler.
            //}

            Text msg = Text.literal(String.format(HURT_SUCCESS, target.getDisplayName().getString()));
            source.sendFeedback(msg, true);

            return 1;
        } else {
            throw NOT_LIVING_ENTITY_EXCEPTION.create(target.getDisplayName().getString());
        }
    }

    private static int executeJump(final ServerCommandSource source, Entity target, float intesity) {

        target.addVelocity(0, intesity, 0);

        //if (source.getPlayer() != null) {
        //    ServerPlayerEntity player = source.getPlayer();
        //    player.networkHandler.sendPacket(new EntityVelocityUpdateS2CPacket(target.getId(), new Vec3d(0, 0.5, 0)));
        //}

        if (target instanceof ServerPlayerEntity player) {
            player.networkHandler.sendPacket(new EntityVelocityUpdateS2CPacket(target.getId(), new Vec3d(0, intesity, 0)));
        }

        Text msg = Text.literal(String.format(JUMP_SUCCESS, target.getDisplayName().getString(), intesity));
        source.sendFeedback(msg, true);

        return 1;
    }

    private static int executeWarn(final ServerCommandSource source, Entity target, boolean state) throws CommandSyntaxException {

        if (target instanceof PolarBearEntity polarBear) {
            if (!target.isAlive()) {
                throw TARGET_IS_DEAD_EXCEPTION.create(target.getDisplayName().getString());
            }

            polarBear.setWarning(state);

            Text msg = Text.literal(String.format(WARN_SUCCESS, target.getDisplayName().getString(), state));
            source.sendFeedback(msg, true);

            return 1;
        } else {
            throw NOT_POLAR_BEAR_ENITY_EXCEPTION.create(target.getDisplayName().getString());
        }
    }
}

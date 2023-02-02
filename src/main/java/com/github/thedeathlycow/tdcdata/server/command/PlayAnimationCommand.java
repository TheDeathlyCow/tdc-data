package com.github.thedeathlycow.tdcdata.server.command;

import com.github.thedeathlycow.tdcdata.server.command.argument.HandArgumentType;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.client.util.math.Vector3d;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.PolarBearEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
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
    private static final String WARN_SUCCESS = "Animated warn for %s with state %s";

    private static final DynamicCommandExceptionType FAILED_ENTITY_EXCEPTION = new DynamicCommandExceptionType(
            (targetName) -> {
                return Text.translatable("commands.enchant.failed.entity", targetName);
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

    private static final DynamicCommandExceptionType SAME_TARGET_EXCEPTION = new DynamicCommandExceptionType(
            (targetName) -> {
                return Text.literal(String.format("%s cannot ride itself", targetName));
            }
    );

    private static final SimpleCommandExceptionType CANNOT_JUMP_EXCEPTION = new SimpleCommandExceptionType(Text.literal("Target cannot jump"));

    private static final SimpleCommandExceptionType NOT_RIDING_ANYTHING_EXCEPTION = new SimpleCommandExceptionType(Text.literal("Target is not riding anything"));

    private static final DynamicCommandExceptionType CANNOT_RIDE_EXCEPTION = new DynamicCommandExceptionType(
            (target) -> {
                Text targetName = Text.of(target.toString());
                if (target instanceof Text targetText) {
                    targetName = targetText;
                }

                return Text.empty()
                        .append(targetName)
                        .append(Text.literal(" is not rideable"));
            }
    );

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandManager.RegistrationEnvironment registryAccess) {

        var swingAnimation = literal("swing")
                .then(argument("hand", HandArgumentType.hand())
                        .executes(context -> {
                                    return executeSwing(
                                            context.getSource(),
                                            EntityArgumentType.getEntity(context, "target"),
                                            HandArgumentType.getHand(context, "hand")
                                    );
                                }
                        )
                );

        var hurtAnimation = literal("hurt")
                .executes(context -> {
                            return executeHurt(
                                    context.getSource(),
                                    EntityArgumentType.getEntity(context, "target")
                            );
                        }
                );

        var rideAnimation = literal("ride")
                .then(literal("mount")
                        .then(argument("vehicle", EntityArgumentType.entity())
                                .executes(
                                        context -> {
                                            return executeMount(
                                                    context.getSource(),
                                                    EntityArgumentType.getEntity(context, "target"),
                                                    EntityArgumentType.getEntity(context, "vehicle"),
                                                    false
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
                .then(literal("dismount")
                        .executes(context -> {
                                    return executeDismount(
                                            context.getSource(),
                                            EntityArgumentType.getEntity(context, "target")
                                    );
                                }
                        )
                );

        var jumpAnimation = literal("jump")
                .executes(context -> {
                            return executeJump(
                                    context.getSource(),
                                    EntityArgumentType.getEntity(context, "target")
                            );
                        }
                );

        var warnAnimation = literal("warn")
                .then(argument("state", BoolArgumentType.bool())
                        .executes(context -> {
                                    return executeWarn(
                                            context.getSource(),
                                            EntityArgumentType.getEntity(context, "target"),
                                            BoolArgumentType.getBool(context, "state")
                                    );
                                }
                        )
                )
                .executes(
                        context -> {
                            return executeWarn(
                                    context.getSource(),
                                    EntityArgumentType.getEntity(context, "target"),
                                    true
                            );
                        }
                );

        dispatcher.register(
                literal("tdcdata")
                        .then(
                                (literal("playanimation").requires((src) -> src.hasPermissionLevel(2)))
                                        .then(
                                                argument("target", EntityArgumentType.entity())
                                                        .then(swingAnimation)
                                                        .then(hurtAnimation)
                                                        .then(rideAnimation)
                                                        .then(jumpAnimation)
                                                        .then(warnAnimation)
                                        )
                        )
        );
    }

    private static int executeSwing(final ServerCommandSource source, Entity target, Hand hand) throws CommandSyntaxException {

        if (target instanceof LivingEntity livingTarget) {
            if (!target.isAlive()) {
                throw TARGET_IS_DEAD_EXCEPTION.create(target.getDisplayName().getString());
            }

            livingTarget.swingHand(hand, true);

            Text msg = Text.literal("Swung ")
                    .append(target.getDisplayName());
            source.sendFeedback(msg, true);

            return 1;
        } else {
            throw FAILED_ENTITY_EXCEPTION.create(target.getDisplayName().getString());
        }
    }

    private static int executeHurt(final ServerCommandSource source, Entity target) throws CommandSyntaxException {

        if (target instanceof LivingEntity livingTarget) {
            if (!target.isAlive()) {
                throw TARGET_IS_DEAD_EXCEPTION.create(target.getDisplayName().getString());
            }

            livingTarget.getWorld().sendEntityStatus(livingTarget, EntityStatuses.DAMAGE_FROM_GENERIC_SOURCE);

            Text msg = Text.literal(String.format(HURT_SUCCESS, target.getDisplayName().getString()));
            source.sendFeedback(msg, true);

            return 1;
        } else {
            throw FAILED_ENTITY_EXCEPTION.create(target.getDisplayName().getString());
        }
    }

    private static int executeJump(final ServerCommandSource source, Entity target) throws CommandSyntaxException {

        if (target instanceof MobEntity mob) {
            mob.getJumpControl().setActive();
            Text msg = Text.empty()
                    .append(target.getDisplayName())
                    .append(Text.literal(" started jumping"));
            source.sendFeedback(msg, true);
        } else {
            throw CANNOT_JUMP_EXCEPTION.create();
        }
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

    private static int executeMount(final ServerCommandSource source, Entity target, Entity vehicle, boolean force) throws CommandSyntaxException {

        if (target.getId() == vehicle.getId()) {
            throw SAME_TARGET_EXCEPTION.create(target.getDisplayName().getString());
        }
        if (target.startRiding(vehicle, force)) {
            Text msg = Text.literal("") // new way of making text lol
                    .append(target.getDisplayName())
                    .append(Text.literal(" started riding "))
                    .append(vehicle.getDisplayName());
            source.sendFeedback(msg, true);
            return 1;
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
        return 1;
    }
}

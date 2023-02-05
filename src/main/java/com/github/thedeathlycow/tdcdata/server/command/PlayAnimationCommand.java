package com.github.thedeathlycow.tdcdata.server.command;

import com.github.thedeathlycow.tdcdata.mixin.invokers.LivingEntityInvoker;
import com.github.thedeathlycow.tdcdata.server.command.argument.EntityPoseArgumentType;
import com.github.thedeathlycow.tdcdata.server.command.argument.HandArgumentType;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.PolarBearEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class PlayAnimationCommand {

    private static final DynamicCommandExceptionType FAILED_ENTITY_EXCEPTION = new DynamicCommandExceptionType(
            (targetName) -> {
                return Text.translatable("commands.enchant.failed.entity", targetName);
            }
    );


    private static final DynamicCommandExceptionType NOT_POLAR_BEAR_ENITY_EXCEPTION = new DynamicCommandExceptionType(
            (targetName) -> {
                return Text.empty()
                        .append((Text) targetName)
                        .append(Text.literal(" is not a polar bear"));
            }
    );

    private static final DynamicCommandExceptionType TARGET_IS_DEAD_EXCEPTION = new DynamicCommandExceptionType(
            (targetName) -> {
                return Text.empty()
                        .append((Text) targetName)
                        .append(Text.literal(" is not alive"));
            }
    );

    private static final SimpleCommandExceptionType CANNOT_JUMP_EXCEPTION = new SimpleCommandExceptionType(Text.literal("Target cannot jump"));

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

        var poseAnimation = literal("setpose")
                .then(argument("pose", EntityPoseArgumentType.entityPose())
                        .executes(context -> {
                            return executeSetPose(
                                    context.getSource(),
                                    EntityArgumentType.getEntity(context, "target"),
                                    EntityPoseArgumentType.getEntityPose(context, "pose")
                            );
                        })
                );

        dispatcher.register(
                literal("tdcdata")
                        .then(
                                (literal("playanimation").requires((src) -> src.hasPermissionLevel(2)))
                                        .then(
                                                argument("target", EntityArgumentType.entity())
                                                        .then(swingAnimation)
                                                        .then(hurtAnimation)
                                                        .then(jumpAnimation)
                                                        .then(warnAnimation)
                                                        .then(poseAnimation)
                                        )
                        )
        );
    }

    private static int executeSetPose(ServerCommandSource source, Entity target, EntityPoseArgumentType.EntityPoseId poseId) {
        EntityPose pose = poseId.getPose();

        target.setPose(pose);

        source.sendFeedback(
                Text.literal("Updated pose of ")
                        .append(target.getName()),
                true
        );

        return pose.ordinal();
    }

    private static int executeSwing(final ServerCommandSource source, Entity target, Hand hand) throws CommandSyntaxException {

        if (target instanceof LivingEntity livingTarget) {
            if (!target.isAlive()) {
                throw TARGET_IS_DEAD_EXCEPTION.create(target.getDisplayName());
            }

            livingTarget.swingHand(hand, true);

            Text msg = Text.literal("Swung ")
                    .append(target.getDisplayName());
            source.sendFeedback(msg, true);

            return hand.ordinal();
        } else {
            throw FAILED_ENTITY_EXCEPTION.create(target.getDisplayName().getString());
        }
    }

    private static int executeHurt(final ServerCommandSource source, Entity target) throws CommandSyntaxException {
        if (!target.isAlive()) {
            throw TARGET_IS_DEAD_EXCEPTION.create(target.getDisplayName());
        }

        int value = 0;
        target.getWorld().sendEntityStatus(target, EntityStatuses.DAMAGE_FROM_GENERIC_SOURCE);
        if (target instanceof LivingEntityInvoker invoker) {
            invoker.tdcdata$invokePlayHurtSound(DamageSource.GENERIC);
            value = 1;
        }

        Text msg = Text.literal("Played hurt animation for ")
                .append(target.getDisplayName());
        source.sendFeedback(msg, true);

        return value;
    }

    private static int executeJump(final ServerCommandSource source, Entity target) throws CommandSyntaxException {

        if (target instanceof MobEntity mob) {
            mob.getJumpControl().setActive();
            Text msg = Text.literal("Played jump animation for ")
                    .append(target.getDisplayName());
            source.sendFeedback(msg, true);
        } else {
            throw CANNOT_JUMP_EXCEPTION.create();
        }
        return 0;
    }

    private static int executeWarn(final ServerCommandSource source, Entity target, boolean state) throws CommandSyntaxException {

        if (target instanceof PolarBearEntity polarBear) {
            if (!target.isAlive()) {
                throw TARGET_IS_DEAD_EXCEPTION.create(target.getDisplayName());
            }

            polarBear.setWarning(state);

            Text msg = Text.literal("Set warning animation state for ")
                    .append(target.getDisplayName())
                    .append(Text.literal(String.format(" to %s", state)));
            source.sendFeedback(msg, true);

            return state ? 1 : 0;
        } else {
            throw NOT_POLAR_BEAR_ENITY_EXCEPTION.create(target.getDisplayName());
        }
    }


}

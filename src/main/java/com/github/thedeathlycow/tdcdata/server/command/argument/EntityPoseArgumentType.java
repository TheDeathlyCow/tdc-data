package com.github.thedeathlycow.tdcdata.server.command.argument;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.serialization.Codec;
import net.minecraft.command.argument.BlockMirrorArgumentType;
import net.minecraft.command.argument.EnumArgumentType;
import net.minecraft.entity.EntityPose;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.StringIdentifiable;

public class EntityPoseArgumentType extends EnumArgumentType<EntityPoseArgumentType.EntityPoseId> {

    private static final Codec<EntityPoseId> CODEC = StringIdentifiable.createCodec(EntityPoseId::values);

    private EntityPoseArgumentType() {
        super(CODEC, EntityPoseId::values);
    }

    public static  EnumArgumentType<EntityPoseId> entityPose() {
        return new EntityPoseArgumentType();
    }

    public static EntityPoseId getEntityPose(CommandContext<ServerCommandSource> context, String id) {
        return context.getArgument(id, EntityPoseId.class);
    }

    /**
     * A hack to make {@link EntityPose} be string-identifiable without using a potentially conflicting mixin
     */
    public enum EntityPoseId implements StringIdentifiable {
        STANDING("standing", EntityPose.STANDING),
        FALL_FLYING("fall_flying", EntityPose.FALL_FLYING),
        SLEEPING("sleeping", EntityPose.SLEEPING),
        SWIMMING("swimming", EntityPose.SWIMMING),
        SPIN_ATTACK("spin_attack", EntityPose.SPIN_ATTACK),
        CROUCHING("crouching", EntityPose.CROUCHING),
        LONG_JUMPING("long_jumping", EntityPose.LONG_JUMPING),
        DYING("dying", EntityPose.DYING),
        CROAKING("croaking", EntityPose.CROAKING),
        USING_TONGUE("using_tongue", EntityPose.USING_TONGUE),
        ROARING("roaring", EntityPose.ROARING),
        SNIFFING("sniffing", EntityPose.SNIFFING),
        EMERGING("emerging", EntityPose.EMERGING),
        DIGGING("digging", EntityPose.DIGGING);

        private final String id;
        private final EntityPose pose;

        EntityPoseId(String id, EntityPose pose) {
            this.id = id;
            this.pose = pose;
        }

        @Override
        public String asString() {
            return this.id;
        }

        public EntityPose getPose() {
            return this.pose;
        }
    }
}

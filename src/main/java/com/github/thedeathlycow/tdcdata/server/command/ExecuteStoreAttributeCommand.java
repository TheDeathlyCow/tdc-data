package com.github.thedeathlycow.tdcdata.server.command;

import com.mojang.brigadier.ResultConsumer;
import com.mojang.brigadier.exceptions.Dynamic3CommandExceptionType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.function.BinaryOperator;
import java.util.function.Function;

public class ExecuteStoreAttributeCommand {
    private static final BinaryOperator<ResultConsumer<ServerCommandSource>> BINARY_RESULT_CONSUMER = (resultConsumer, resultConsumer2) -> (context, success, result) -> {
        resultConsumer.onCommandComplete(context, success, result);
        resultConsumer2.onCommandComplete(context, success, result);
    };

    private static final Dynamic3CommandExceptionType MODIFIER_ALREADY_PRESENT_EXCEPTION = new Dynamic3CommandExceptionType((entityName, attributeName, uuid) -> {
        return Text.translatable("commands.attribute.failed.modifier_already_present", uuid, attributeName, entityName);
    });

    public static ServerCommandSource executeStoreAttribute(
            ServerCommandSource source,
            Entity target,
            EntityAttribute attribute,
            double scale,
            boolean requestResult,
            @Nullable Function<Double, EntityAttributeModifier> modifierFactory
    ) {
        return source.mergeConsumers((context, success, result) -> {
            double value = (requestResult ? result : (success ? 1 : 0)) * scale;
            if (target instanceof LivingEntity livingEntity) {
                EntityAttributeInstance inst = livingEntity.getAttributeInstance(attribute);
                if (inst == null) {
                    source.sendError(
                            Text.translatable(
                                    "commands.attribute.failed.no_attribute",
                                    livingEntity.getName(),
                                    Text.translatable(attribute.getTranslationKey())
                            )
                    );
                    return;
                }

                if (modifierFactory != null) {

                    var modifier = modifierFactory.apply(value);

                    if (inst.hasModifier(modifier)) {
                        source.sendError(
                                Text.translatable(
                                        "commands.attribute.failed.modifier_already_present",
                                        modifier.getId(),
                                        Text.translatable(attribute.getTranslationKey()),
                                        livingEntity.getName()
                                )
                        );
                    } else {
                        inst.addPersistentModifier(modifier);
                    }
                } else {
                    inst.setBaseValue(value);
                }
            } else {
                source.sendError(Text.translatable("commands.attribute.failed.entity", target.getName()));
            }
        }, BINARY_RESULT_CONSUMER);
    }
}

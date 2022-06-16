package com.github.thedeathlycow.tdcdata.server.command;

import com.mojang.brigadier.ResultConsumer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.TranslatableText;

import java.util.function.BinaryOperator;

public class ExecuteStoreAttributeCommand {
    private static final BinaryOperator<ResultConsumer<ServerCommandSource>> BINARY_RESULT_CONSUMER = (resultConsumer, resultConsumer2) -> (context, success, result) -> {
        resultConsumer.onCommandComplete(context, success, result);
        resultConsumer2.onCommandComplete(context, success, result);
    };
    public static ServerCommandSource executeStoreAttribute(ServerCommandSource source, Entity target, EntityAttribute attribute, double scale, boolean requestResult){
        return source.mergeConsumers((context, success, result) -> {
            double value = (requestResult ?  result : (success ? 1 : 0)) * scale;
            if (target instanceof LivingEntity livingEntity){
                EntityAttributeInstance inst = livingEntity.getAttributeInstance(attribute);
                if (inst != null) {
                    inst.setBaseValue(value);
                } else {
                    source.sendError(new TranslatableText("commands.attribute.failed.no_attribute", livingEntity.getName(), new TranslatableText(attribute.getTranslationKey())));
                }
            }
            else {
                source.sendError(new TranslatableText("commands.attribute.failed.entity", target.getName()));
            }
        }, BINARY_RESULT_CONSUMER);
    }
}

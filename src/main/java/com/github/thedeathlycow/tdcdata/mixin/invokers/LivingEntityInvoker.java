package com.github.thedeathlycow.tdcdata.mixin.invokers;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(LivingEntity.class)
public interface LivingEntityInvoker {

    @Invoker("playHurtSound")
    void tdcdata$invokePlayHurtSound(DamageSource source);

}

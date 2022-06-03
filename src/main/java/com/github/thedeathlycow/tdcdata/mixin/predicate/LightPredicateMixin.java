package com.github.thedeathlycow.tdcdata.mixin.predicate;

import com.github.thedeathlycow.tdcdata.predicate.LightTypePredicate;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.predicate.LightPredicate;
import net.minecraft.predicate.NumberRange;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LightType;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(LightPredicate.class)
public abstract class LightPredicateMixin implements LightTypePredicate {

    @Shadow
    @Final
    private NumberRange.IntRange range;

    @Shadow public abstract boolean test(ServerWorld world, BlockPos pos);

    @Nullable
    private LightType tdcdata$lightType;

    private boolean tdcdata$includeSkydarkening = true;

    @Inject(
            method = "test",
            at = @At(
                    value = "RETURN"
            ),
            cancellable = true
    )
    private void testType(ServerWorld world, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        LightPredicate instance =  (LightPredicate) (Object) this;
        if (this.tdcdata$lightType != null && (instance != LightPredicate.ANY) && world.canSetBlock(pos)) {
            cir.setReturnValue(LightTypePredicate.super.tdcdata$test(world, pos, this.range));
        }
    }

    @Inject(
            method = "fromJson",
            at = @At(
                    value = "RETURN"
            )
    )
    private static void readTypeFromJson(JsonElement json, CallbackInfoReturnable<LightPredicate> cir) {
        LightPredicate predicate = cir.getReturnValue();
        LightTypePredicate typePredicate = (LightTypePredicate) predicate;
        if (predicate != LightPredicate.ANY) {
            JsonObject lightJson = JsonHelper.asObject(json, "light");

            if (lightJson.has("type")) {
                JsonElement typeElement = lightJson.get("type");
                LightType type = LightTypePredicate.getTypeFromJson(typeElement);
                boolean includeSkydarkening = LightTypePredicate.getIncludeSkyDarknessFromJson(typeElement);
                typePredicate.tdcdata$setLightType(type);
                typePredicate.tdcdata$setShouldIncludeSkyDarkness(includeSkydarkening);
            } else {
                typePredicate.tdcdata$setLightType(null);
                typePredicate.tdcdata$setShouldIncludeSkyDarkness(true);
            }
        }
    }

    @Inject(
            method = "toJson",
            at = @At("TAIL"),
            locals = LocalCapture.CAPTURE_FAILEXCEPTION
    )
    private void addTypeToJson(CallbackInfoReturnable<JsonElement> cir, JsonObject jsonObject) {
        if (this.tdcdata$lightType != null) {
            jsonObject.add("type", LightTypePredicate.toJson(this));
        }
    }

    @Override
    public @Nullable LightType tdcdata$getLightType() {
        return this.tdcdata$lightType;
    }

    @Override
    public void tdcdata$setLightType(@Nullable LightType lightType) {
        this.tdcdata$lightType = lightType;
    }

    @Override
    public boolean tdcdata$shouldIncludeSkyDarkness() {
        return this.tdcdata$includeSkydarkening;
    }

    @Override
    public void tdcdata$setShouldIncludeSkyDarkness(boolean shouldIncludeSkyDarkness) {
        this.tdcdata$includeSkydarkening = shouldIncludeSkyDarkness;
    }
}

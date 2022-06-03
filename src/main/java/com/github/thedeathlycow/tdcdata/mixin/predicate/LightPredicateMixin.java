package com.github.thedeathlycow.tdcdata.mixin.predicate;

import com.github.thedeathlycow.tdcdata.TdcData;
import com.github.thedeathlycow.tdcdata.predicate.LightTypePredicate;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.predicate.LightPredicate;
import net.minecraft.predicate.NumberRange;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LightType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(LightPredicate.class)
public class LightPredicateMixin implements LightTypePredicate {

    @Shadow @Final private NumberRange.IntRange range;
    @Nullable
    private LightType lightType;

    private boolean includeSkydarkening = true;

    @Inject(
            method = "test",
            at = @At(
                    value = "TAIL",
                    shift = At.Shift.BEFORE
            ),
            cancellable = true
    )
    private void testType(ServerWorld world, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (this.lightType != null) {
            TdcData.LOGGER.info("testing light type");
            int lightLevel = world.getLightLevel(this.lightType, pos);
            if (this.includeSkydarkening && this.lightType == LightType.SKY) {
                lightLevel -= world.getAmbientDarkness();
            }
            cir.setReturnValue(this.range.test(lightLevel));
        }
    }

    @Inject(
            method = "fromJson",
            at = @At(
                    value = "RETURN",
                    shift = At.Shift.AFTER
            )
    )
    private static void readTypeFromJson(JsonElement json, CallbackInfoReturnable<LightPredicate> cir) {
        LightPredicate predicate = cir.getReturnValue();
        if (predicate != LightPredicate.ANY) {
            LightTypePredicate typePredicate = (LightTypePredicate) predicate;
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

            TdcData.LOGGER.info("read light type from json");
        } else {
            TdcData.LOGGER.info("NOT read light type from json");
        }
    }

    @Inject(
            method = "toJson",
            at = @At("TAIL"),
            locals = LocalCapture.CAPTURE_FAILEXCEPTION
    )
    private void addTypeToJson(CallbackInfoReturnable<JsonElement> cir, JsonObject jsonObject) {
        if (this.lightType != null) {
            jsonObject.add("type", LightTypePredicate.toJson(this));
            TdcData.LOGGER.info("wrote light type to json");
        }
    }

    @Override
    public @Nullable LightType tdcdata$getLightType() {
        return this.lightType;
    }

    @Override
    public void tdcdata$setLightType(@Nullable LightType lightType) {
        this.lightType = lightType;
    }

    @Override
    public boolean tdcdata$shouldIncludeSkyDarkness() {
        return this.includeSkydarkening;
    }

    @Override
    public void tdcdata$setShouldIncludeSkyDarkness(boolean shouldIncludeSkyDarkness) {
        this.includeSkydarkening = shouldIncludeSkyDarkness;
    }
}

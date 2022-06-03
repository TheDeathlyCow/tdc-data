package com.github.thedeathlycow.tdcdata.predicate;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.world.LightType;
import org.jetbrains.annotations.Nullable;

public interface LightTypePredicate {

    @Nullable
    LightType tdcdata$getLightType();

    void tdcdata$setLightType(@Nullable LightType lightType);

    boolean tdcdata$shouldIncludeSkyDarkness();

    void tdcdata$setShouldIncludeSkyDarkness(boolean shouldIncludeSkyDarkness);


    static LightType getTypeFromJson(JsonElement element) {
        String typeName;
        if (element.isJsonPrimitive()) {
            typeName = element.getAsString();
        } else {
            JsonObject typeJson = element.getAsJsonObject();
            typeName = typeJson.get("type").getAsString();
        }
        return LightType.valueOf(typeName.toUpperCase());
    }

    static boolean getIncludeSkyDarknessFromJson(JsonElement element) {
        if (element.isJsonPrimitive()) {
            return true;
        } else {
            JsonObject typeJson = element.getAsJsonObject();
            return typeJson.get("include_sky_darkness").getAsBoolean();
        }
    }

    static JsonElement toJson(LightTypePredicate typePredicate) {
        JsonObject typeObject = new JsonObject();
        LightType type = typePredicate.tdcdata$getLightType();
        if (type != null) {
            typeObject.add("type", new JsonPrimitive(type.toString().toLowerCase()));
            typeObject.add("include_sky_darkness", new JsonPrimitive(typePredicate.tdcdata$shouldIncludeSkyDarkness()));
        }
        return typeObject;
    }
}

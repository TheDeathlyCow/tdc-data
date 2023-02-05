package com.github.thedeathlycow.tdcdata.predicate;

import com.google.common.collect.ImmutableSet;
import com.google.gson.*;
import net.minecraft.block.SculkSensorBlock;
import net.minecraft.predicate.NumberRange;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public class HandPredicate {

    public static final HandPredicate ANY = new HandPredicate();

    @Nullable
    private final Hand hand;

    public HandPredicate() {
        this(null);
    }

    public HandPredicate(@Nullable Hand hand) {
        this.hand = hand;
    }

    public boolean test(Hand hand) {
        if (this == ANY) {
            return true;
        } else if (this.hand == null) {
            return true;
        } else {
            return this.hand == hand;
        }
    }

    public static HandPredicate fromJson(@Nullable JsonElement jsonElement) {
        if (jsonElement == null || jsonElement.isJsonNull()) {
            return ANY;
        }

        Hand hand = null;
        if (jsonElement.isJsonPrimitive()) {
            hand = Hand.valueOf(jsonElement.getAsString().toUpperCase());
        } else if (jsonElement.isJsonObject()) {
            var json = jsonElement.getAsJsonObject();
            if (json.has("hand")) {
                hand = Hand.valueOf(json.get("hand").getAsString());
            }
        }

        return new HandPredicate(hand);
    }

    public JsonElement toJson() {
        if (this == ANY) {
            return JsonNull.INSTANCE;
        }
        JsonObject json = new JsonObject();

        if (hand != null) {
            json.addProperty("hand", hand.toString());
        }

        return json;
    }
}

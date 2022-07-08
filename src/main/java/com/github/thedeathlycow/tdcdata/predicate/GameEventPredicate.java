package com.github.thedeathlycow.tdcdata.predicate;

import com.google.common.collect.ImmutableSet;
import com.google.gson.*;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashSet;
import java.util.Set;

public class GameEventPredicate {

    public static final GameEventPredicate ANY = new GameEventPredicate();

    @Nullable
    private final Set<GameEvent> events;
    @Nullable
    private final TagKey<GameEvent> tag;

    public GameEventPredicate() {
        this.events = null;
        this.tag = null;
    }

    public GameEventPredicate(@Nullable Set<GameEvent> events, @Nullable TagKey<GameEvent> tag) {
        this.events = events;
        this.tag = tag;
    }

    public boolean test(GameEvent event) {
        if (this == ANY) {
            return true;
        } else if (this.events != null && !this.events.contains(event)) {
            return false;
        } else {
            return this.tag == null || event.isIn(this.tag);
        }
    }

    public static GameEventPredicate fromJson(@Nullable JsonElement jsonElement) {
        if (jsonElement == null || jsonElement.isJsonNull()) {
            return ANY;
        }
        Set<GameEvent> events = null;
        TagKey<GameEvent> tag = null;
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        if (jsonObject.has("events")) {
            ImmutableSet.Builder<GameEvent> builder = ImmutableSet.builder();
            JsonArray array = jsonObject.getAsJsonArray("events");
            for (JsonElement elem : array) {
                Identifier id = new Identifier(elem.getAsString());
                GameEvent event = Registry.GAME_EVENT.getOrEmpty(id).orElseThrow(() -> {
                    return new JsonSyntaxException("Unknown game event id '" + id + "'");
                });
                builder.add(event);
            }
            events = builder.build();
        }

        if (jsonObject.has("tag")) {
            Identifier id = new Identifier(JsonHelper.getString(jsonObject, "tag"));
            tag = TagKey.of(Registry.GAME_EVENT_KEY, id);
        }

        return new GameEventPredicate(events, tag);
    }

    public JsonElement toJson() {
        if (this == ANY) {
            return JsonNull.INSTANCE;
        }
        JsonObject json = new JsonObject();

        if (this.events != null && this.events.size() > 0) {
            JsonArray eventsArray = new JsonArray();
            for (GameEvent event : this.events) {
                eventsArray.add(Registry.GAME_EVENT.getId(event).toString());
            }
            json.add("events", eventsArray);
        }

        if (this.tag != null) {
            json.addProperty("tag", this.tag.id().toString());
        }

        return json;
    }
}

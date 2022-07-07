package com.github.thedeathlycow.tdcdata.predicate;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
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
        } else if (this.events != null && this.events.contains(event)) {
            return true;
        } else {
            return this.tag != null && event.isIn(this.tag);
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
            events = new LinkedHashSet<>();
            JsonArray array = jsonObject.getAsJsonArray("events");
            for (JsonElement elem : array) {
                GameEvent event = Registry.GAME_EVENT.get(new Identifier(elem.getAsString()));
                events.add(event);
            }
        }

        if (jsonObject.has("tag")) {
            tag = TagKey.of(Registry.GAME_EVENT_KEY, new Identifier(jsonObject.get("tag").getAsString()));
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

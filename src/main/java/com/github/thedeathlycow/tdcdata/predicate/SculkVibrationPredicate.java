package com.github.thedeathlycow.tdcdata.predicate;

import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.event.listener.SculkSensorListener;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class SculkVibrationPredicate {

    @Nullable
    private final TagKey<GameEvent> eventTag;
    private final Set<GameEvent> events;
    private final int frequency;
    @Nullable
    private final SculkSensorListener listener;

    private SculkVibrationPredicate(Builder builder) {
        this.eventTag = builder.eventTag;
        this.events = builder.events;
        this.frequency = builder.frequency;
        this.listener = builder.listener;
    }

    public boolean test(GameEvent event, int frequency, SculkSensorListener listener) {

        if (!this.events.contains(event)) {
            return false;
        } else if (!(this.eventTag != null && !event.isIn(this.eventTag))) {
            return false;
        } else if (this.frequency != frequency) {
            return false;
        } else if ((this.listener != null && !listener.equals(this.listener))) {
            return false;
        }

        return true;
    }

    public static class Builder {

        @Nullable
        private TagKey<GameEvent> eventTag = null;
        private Set<GameEvent> events = Set.of();
        private int frequency = 0;
        @Nullable
        private SculkSensorListener listener = null;

        public Builder withEventTag(TagKey<GameEvent> eventTag) {
            this.eventTag = eventTag;
            return this;
        }

        public Builder withEvents(Set<GameEvent> events) {
            this.events = events;
            return this;
        }

        public Builder withFrequency(int frequency) {
            this.frequency = frequency;
            return this;
        }

        public Builder withListener(SculkSensorListener listener) {
            this.listener = listener;
            return this;
        }

        public SculkVibrationPredicate build() {
            return new SculkVibrationPredicate(this);
        }
    }
}

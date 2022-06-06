package com.github.thedeathlycow.tdcdata.server.command.argument;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.scoreboard.ScoreboardPlayerScore;

@FunctionalInterface
public interface UnaryOperation {

    void apply(ScoreboardPlayerScore a) throws CommandSyntaxException;

    @FunctionalInterface
    interface UnaryIntOperation extends UnaryOperation {

        int apply(int a) throws CommandSyntaxException;

        default void apply(ScoreboardPlayerScore score) throws CommandSyntaxException {
            score.setScore(this.apply(score.getScore()));
        }
    }
}

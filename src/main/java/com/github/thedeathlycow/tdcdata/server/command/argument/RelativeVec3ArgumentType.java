package com.github.thedeathlycow.tdcdata.server.command.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.command.argument.DefaultPosArgument;
import net.minecraft.command.argument.LookingPosArgument;
import net.minecraft.command.argument.PosArgument;
import net.minecraft.command.argument.Vec3ArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public class RelativeVec3ArgumentType implements ArgumentType<PosArgument> {
    private static final Collection<String> EXAMPLES = Arrays.asList("0 0 0", "^ ^ ^", "^1 ^ ^-5", "0.1 -0.5 .9");
    public static final SimpleCommandExceptionType INCOMPLETE_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("argument.pos3d.incomplete"));
    public static final SimpleCommandExceptionType MIXED_COORDINATE_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("argument.pos.mixed"));

    public static RelativeVec3ArgumentType vec3() {
        return new RelativeVec3ArgumentType();
    }

    public static Vec3d get(CommandContext<ServerCommandSource> context, String name) {
        return context.getArgument(name, PosArgument.class)
                .toAbsolutePos(context.getSource());
    }


    public PosArgument parse(StringReader stringReader) throws CommandSyntaxException {
        return stringReader.canRead()
                && stringReader.peek() == '^'
                ? LookingPosArgument.parse(stringReader)
                : RawVec3Argument.parse(stringReader);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return ArgumentType.super.listSuggestions(context, builder);
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}

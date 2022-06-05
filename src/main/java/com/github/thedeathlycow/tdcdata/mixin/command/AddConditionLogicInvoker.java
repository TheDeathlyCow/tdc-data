package com.github.thedeathlycow.tdcdata.mixin.command;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.server.command.ExecuteCommand;
import net.minecraft.server.command.ServerCommandSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ExecuteCommand.class)
public class AddConditionLogicInvoker {

    @Invoker("addConditionLogic")
    public static ArgumentBuilder<ServerCommandSource, ?> invoke(CommandNode<ServerCommandSource> root, ArgumentBuilder<ServerCommandSource, ?> builder, boolean positive, ExecuteCommand.Condition condition) {
        throw new AssertionError();
    }

}

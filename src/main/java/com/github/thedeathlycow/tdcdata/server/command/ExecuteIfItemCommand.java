package com.github.thedeathlycow.tdcdata.server.command;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.minecraft.block.ChestBlock;
import net.minecraft.command.argument.ItemSlotArgumentType;
import net.minecraft.command.argument.ItemStackArgument;
import net.minecraft.command.argument.ItemStackArgumentType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.ExecuteCommand;
import net.minecraft.server.command.ItemCommand;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.TranslatableText;

public class ExecuteIfItemCommand {

    private static final DynamicCommandExceptionType NO_SUCH_SLOT_TARGET_EXCEPTION = new DynamicCommandExceptionType((slot) -> {
        return new TranslatableText("commands.item.target.no_such_slot", slot);
    });

    public static boolean testItemCondition(ServerCommandSource source, Inventory inventory, int itemSlot, ItemStackArgument testItem) throws CommandSyntaxException {
        if (itemSlot >= 0 && itemSlot < inventory.size()) {
            ItemStack stack = inventory.getStack(itemSlot);
            return testItem.test(stack);
        } else {
            throw NO_SUCH_SLOT_TARGET_EXCEPTION.create(itemSlot);
        }
    }
}

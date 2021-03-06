package com.github.thedeathlycow.tdcdata.server.command;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic3CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.util.function.Predicate;

public class ExecuteIfItemCommand {

    private static final DynamicCommandExceptionType NO_SUCH_SLOT_TARGET_EXCEPTION = new DynamicCommandExceptionType((slot) -> {
        return Text.translatable("commands.item.target.no_such_slot", slot);
    });

    private static final Dynamic3CommandExceptionType NOT_A_CONTAINER_TARGET_EXCEPTION = new Dynamic3CommandExceptionType((x, y, z) -> {
        return Text.translatable("commands.item.target.not_a_container", x, y, z);
    });

    public static boolean testEntityItemCondition(ServerCommandSource source, Entity entity, int itemSlot, Predicate<ItemStack> itemPredicate) throws CommandSyntaxException {
        StackReference stack = entity.getStackReference(itemSlot);
        if (stack != StackReference.EMPTY) {
            return itemPredicate.test(stack.get());
        } else {
            throw NO_SUCH_SLOT_TARGET_EXCEPTION.create(itemSlot);
        }
    }

    public static boolean testBlockItemCondition(ServerCommandSource source, BlockPos pos, int itemSlot, Predicate<ItemStack> itemPredicate) throws CommandSyntaxException {
        ServerWorld world = source.getWorld();
        Inventory inventory = getInventoryAtPos(world, pos);

        boolean isSlotInInventory = itemSlot >= 0 && itemSlot < inventory.size();
        if (!isSlotInInventory) {
            throw NO_SUCH_SLOT_TARGET_EXCEPTION.create(itemSlot);
        }

        ItemStack stack = inventory.getStack(itemSlot);
        return itemPredicate.test(stack);
    }

    private static Inventory getInventoryAtPos(ServerWorld world, BlockPos pos) throws CommandSyntaxException {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if ((blockEntity instanceof Inventory inventory)) {
            return inventory;
        } else {
            throw NOT_A_CONTAINER_TARGET_EXCEPTION.create(pos.getX(), pos.getY(), pos.getZ());
        }
    }
}

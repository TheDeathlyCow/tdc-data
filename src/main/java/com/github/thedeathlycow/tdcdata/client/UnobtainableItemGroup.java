package com.github.thedeathlycow.tdcdata.client;

import com.github.thedeathlycow.tdcdata.DatapackUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.minecraft.block.LightBlock;
import net.minecraft.block.StructureBlock;
import net.minecraft.block.enums.StructureBlockMode;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
public class UnobtainableItemGroup {

    public static ItemGroup create() {
        return FabricItemGroupBuilder.create(
                        new Identifier(DatapackUtils.MODID, "unobtainable_group")
                )
                .icon(() -> new ItemStack(Items.BARRIER))
                .appendItems(stacks -> {
                    stacks.add(Items.BARRIER.getDefaultStack());
                    stacks.add(Items.DEBUG_STICK.getDefaultStack());
                    stacks.add(Items.COMMAND_BLOCK.getDefaultStack());
                    stacks.add(Items.REPEATING_COMMAND_BLOCK.getDefaultStack());
                    stacks.add(Items.CHAIN_COMMAND_BLOCK.getDefaultStack());
                    stacks.add(Items.COMMAND_BLOCK_MINECART.getDefaultStack());
                    stacks.add(Items.STRUCTURE_BLOCK.getDefaultStack());
                    stacks.add(getStructureDataBlock());
                    stacks.add(Items.JIGSAW.getDefaultStack());
                    stacks.add(Items.STRUCTURE_VOID.getDefaultStack());
                    stacks.add(Items.KNOWLEDGE_BOOK.getDefaultStack());
                    stacks.add(Items.SCULK_SENSOR.getDefaultStack());
                    stacks.add(Items.BUNDLE.getDefaultStack());
                    stacks.add(Items.DRAGON_EGG.getDefaultStack());
                    stacks.add(Items.SPAWNER.getDefaultStack());
                    stacks.addAll(getLightBlocks());
                })
                .build();
    }

    private static ItemStack getStructureDataBlock() {
        ItemStack stack = Items.STRUCTURE_BLOCK.getDefaultStack();
        NbtCompound nbt = new NbtCompound();
        nbt.putString(StructureBlock.MODE.getName(), StructureBlockMode.DATA.asString());
        stack.setSubNbt("BlockEntityTag", nbt);
        return stack;
    }

    private static List<ItemStack> getLightBlocks() {
        List<ItemStack> lights = new ArrayList<>();
        for (int i = 0; i <= 15; i++) {
            ItemStack stack = Items.LIGHT.getDefaultStack();
            NbtCompound nbtCompound = new NbtCompound();
            nbtCompound.putString(LightBlock.LEVEL_15.getName(), String.valueOf(i));
            stack.setSubNbt("BlockStateTag", nbtCompound);
            lights.add(stack);
        }
        return lights;
    }
}

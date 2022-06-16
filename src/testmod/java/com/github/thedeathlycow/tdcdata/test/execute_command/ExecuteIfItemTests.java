package com.github.thedeathlycow.tdcdata.test.execute_command;

import net.minecraft.block.Blocks;
import net.minecraft.command.argument.ItemSlotArgumentType;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ItemCommand;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;

public class ExecuteIfItemTests {

    @GameTest(structureName = "tdcdata-test:execute.ifitem.diamond_helmet_head_slot")
    public void playerWithItemInSlotPassesCheck(TestContext context) {
        VillagerEntity villager = createVillagerWithArmor(context);
        BlockPos buttonPos = new BlockPos(0, 2, 0);
        context.pushButton(buttonPos);
        context.expectBlockAtEnd(Blocks.LIME_WOOL, buttonPos);
    }

    @GameTest(structureName = "tdcdata-test:execute.ifitem.diamond_helmet_head_slot")
    public void playerWithoutItemInSlotFailsCheck(TestContext context) {
        VillagerEntity villager = createVillagerWithoutArmor(context);
        BlockPos buttonPos = new BlockPos(0, 2, 0);
        context.pushButton(buttonPos);
        context.expectBlockAtEnd(Blocks.STONE_BUTTON, buttonPos);
    }

    private static VillagerEntity createVillagerWithArmor(TestContext context) {
        VillagerEntity villager = context.spawnEntity(EntityType.VILLAGER, 0, 2, 0);
        ItemStack stack = new ItemStack(Items.DIAMOND_HELMET);
        final int headSlot = EquipmentSlot.HEAD.getOffsetEntitySlotId(100);
        StackReference stackReference = villager.getStackReference(headSlot);
        stackReference.set(stack);
        return villager;
    }

    private static VillagerEntity createVillagerWithoutArmor(TestContext context) {
        return context.spawnEntity(EntityType.VILLAGER, 0, 2, 0);
    }
}

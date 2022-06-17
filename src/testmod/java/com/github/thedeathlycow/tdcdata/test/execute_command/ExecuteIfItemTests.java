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

    private static final BlockPos BUTTON_POS = new BlockPos(2, 3, 2);

    //* IF tests

    @GameTest(templateName = "tdcdata-test:execute.if.item.test_head_slot")
    public void itemInHeadSlotPassesIfCheck(TestContext context) {
        VillagerEntity villager = createVillagerWithArmor(context);
        context.pushButton(BUTTON_POS);
        context.expectBlockAtEnd(Blocks.LIME_WOOL, BUTTON_POS);
    }

    @GameTest(templateName = "tdcdata-test:execute.if.item.test_head_slot")
    public void noItemInHeadSlotFailsIfCheck(TestContext context) {
        VillagerEntity villager = createVillagerWithoutArmor(context);
        context.pushButton(BUTTON_POS);
        context.expectBlockAtEnd(Blocks.STONE_BUTTON, BUTTON_POS);
    }

    @GameTest(templateName = "tdcdata-test:execute.if.item.test_villager0_slot")
    public void itemInHeadFailsVillager0IfCheck(TestContext context) {
        VillagerEntity villager = createVillagerWithArmor(context);
        context.pushButton(BUTTON_POS);
        context.expectBlockAtEnd(Blocks.STONE_BUTTON, BUTTON_POS);
    }

    @GameTest(templateName = "tdcdata-test:execute.if.item.test_villager0_slot")
    public void noItemInHeadFailsVillager0IfCheck(TestContext context) {
        VillagerEntity villager = createVillagerWithoutArmor(context);
        context.pushButton(BUTTON_POS);
        context.expectBlockAtEnd(Blocks.STONE_BUTTON, BUTTON_POS);
    }

    //* UNLESS tests

    @GameTest(templateName = "tdcdata-test:execute.unless.item.test_head_slot")
    public void itemInHeadSlotFailsUnlessCheck(TestContext context) {
        VillagerEntity villager = createVillagerWithArmor(context);
        context.pushButton(BUTTON_POS);
        context.expectBlockAtEnd(Blocks.STONE_BUTTON, BUTTON_POS);
    }

    @GameTest(templateName = "tdcdata-test:execute.unless.item.test_head_slot")
    public void noItemInHeadSlotPassesUnlessCheck(TestContext context) {
        VillagerEntity villager = createVillagerWithoutArmor(context);
        context.pushButton(BUTTON_POS);
        context.expectBlockAtEnd(Blocks.LIME_WOOL, BUTTON_POS);
    }

    @GameTest(templateName = "tdcdata-test:execute.unless.item.test_villager0_slot")
    public void itemInHeadPassesVillager0UnlessCheck(TestContext context) {
        VillagerEntity villager = createVillagerWithArmor(context);
        context.pushButton(BUTTON_POS);
        context.expectBlockAtEnd(Blocks.LIME_WOOL, BUTTON_POS);
    }

    @GameTest(templateName = "tdcdata-test:execute.unless.item.test_villager0_slot")
    public void noItemInHeadPassesVillager0UnlessCheck(TestContext context) {
        VillagerEntity villager = createVillagerWithoutArmor(context);
        context.pushButton(BUTTON_POS);
        context.expectBlockAtEnd(Blocks.LIME_WOOL, BUTTON_POS);
    }

    private static VillagerEntity createVillagerWithArmor(TestContext context) {
        VillagerEntity villager = context.spawnEntity(EntityType.VILLAGER, BUTTON_POS);
        ItemStack stack = new ItemStack(Items.DIAMOND_HELMET);
        final int headSlot = EquipmentSlot.HEAD.getOffsetEntitySlotId(100);
        StackReference stackReference = villager.getStackReference(headSlot);
        stackReference.set(stack);
        return villager;
    }

    private static VillagerEntity createVillagerWithoutArmor(TestContext context) {
        return context.spawnEntity(EntityType.VILLAGER, BUTTON_POS);
    }
}

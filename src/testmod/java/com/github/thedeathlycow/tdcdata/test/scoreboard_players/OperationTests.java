package com.github.thedeathlycow.tdcdata.test.scoreboard_players;

import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;

public class OperationTests {

    private static final BlockPos BUTTON_POS = new BlockPos(2, 3, 2);

    @GameTest(structureName = "tdcdata-test:scoreboard.players.operation.exponent")
    public void exponentGeneratesCorrectValue(TestContext context) {
        context.pushButton(BUTTON_POS);
        context.expectBlockAtEnd(Blocks.LIME_WOOL, BUTTON_POS);
    }

    @GameTest(structureName = "tdcdata-test:scoreboard.players.operation.logb")
    public void logBaseGeneratesCorrectValue(TestContext context) {
        context.pushButton(BUTTON_POS);
        context.expectBlockAtEnd(Blocks.LIME_WOOL, BUTTON_POS);
    }

    @GameTest(structureName = "tdcdata-test:scoreboard.players.operation.logb.negative")
    public void logBaseWithNegativeInputDoesNotChangeInput(TestContext context) {
        context.pushButton(BUTTON_POS);
        context.expectBlockAtEnd(Blocks.LIME_WOOL, BUTTON_POS);
    }

    @GameTest(structureName = "tdcdata-test:scoreboard.players.operation.rshift")
    public void rShiftGeneratesCorrectValue(TestContext context) {
        context.pushButton(BUTTON_POS);
        context.expectBlockAtEnd(Blocks.LIME_WOOL, BUTTON_POS);
    }

    @GameTest(structureName = "tdcdata-test:scoreboard.players.operation.rshift.negative")
    public void rShiftNegativeGeneratesCorrectValue(TestContext context) {
        context.pushButton(BUTTON_POS);
        context.expectBlockAtEnd(Blocks.LIME_WOOL, BUTTON_POS);
    }

    @GameTest(structureName = "tdcdata-test:scoreboard.players.operation.u_rshift.negative")
    public void unsignedRShiftNegativeGeneratesCorrectValue(TestContext context) {
        context.pushButton(BUTTON_POS);
        context.expectBlockAtEnd(Blocks.LIME_WOOL, BUTTON_POS);
    }
}

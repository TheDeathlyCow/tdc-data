package com.github.thedeathlycow.tdcdata.test.scoreboard_players;

import net.minecraft.block.Blocks;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;

public class FunctionTests {

    private static final BlockPos BUTTON_POS = new BlockPos(2, 3, 2);

    //* Log tests

    @GameTest(templateName = "tdcdata-test:scoreboard.players.function.ln.negative")
    public void lnNegativeDoesNotChangeInput(TestContext context) {
        context.pushButton(BUTTON_POS);
        context.expectBlockAtEnd(Blocks.LIME_WOOL, BUTTON_POS);
    }

    @GameTest(templateName = "tdcdata-test:scoreboard.players.function.ln")
    public void lnPositiveGeneratesCorrectAnswer(TestContext context) {
        context.pushButton(BUTTON_POS);
        context.expectBlockAtEnd(Blocks.LIME_WOOL, BUTTON_POS);
    }

    @GameTest(templateName = "tdcdata-test:scoreboard.players.function.ln.zero")
    public void lnZeroDoesNotChangeInput(TestContext context) {
        context.pushButton(BUTTON_POS);
        context.expectBlockAtEnd(Blocks.LIME_WOOL, BUTTON_POS);
    }

    //* Square Root Tests

    @GameTest(templateName = "tdcdata-test:scoreboard.players.function.sqrt.negative")
    public void sqrtNegativeDoesNotChangeInput(TestContext context) {
        context.pushButton(BUTTON_POS);
        context.expectBlockAtEnd(Blocks.LIME_WOOL, BUTTON_POS);
    }

    @GameTest(templateName = "tdcdata-test:scoreboard.players.function.sqrt")
    public void sqrtPositiveGeneratesCorrectAnswer(TestContext context) {
        context.pushButton(BUTTON_POS);
        context.expectBlockAtEnd(Blocks.LIME_WOOL, BUTTON_POS);
    }

    @GameTest(templateName = "tdcdata-test:scoreboard.players.function.sqrt.zero")
    public void sqrtZeroDoesNotChangeInput(TestContext context) {
        context.pushButton(BUTTON_POS);
        context.expectBlockAtEnd(Blocks.LIME_WOOL, BUTTON_POS);
    }
}

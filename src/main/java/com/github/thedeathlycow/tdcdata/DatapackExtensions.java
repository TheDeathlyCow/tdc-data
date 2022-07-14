package com.github.thedeathlycow.tdcdata;

import com.github.thedeathlycow.tdcdata.server.command.*;
import com.github.thedeathlycow.tdcdata.advancement.TdcDataAdvancementTriggers;
import com.github.thedeathlycow.tdcdata.server.command.FreezeCommand;
import com.github.thedeathlycow.tdcdata.server.command.ScoreboardCommandAdditions;
import com.github.thedeathlycow.tdcdata.server.command.TeamModifyCommandAdditions;
import com.github.thedeathlycow.tdcdata.server.command.TimeCommandAdditions;
import com.github.thedeathlycow.tdcdata.server.command.argument.HandArgumentType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.player.*;
import net.minecraft.command.argument.serialize.ConstantArgumentSerializer;
import net.minecraft.item.ItemStack;
import net.minecraft.scoreboard.ScoreboardCriterion;
import net.minecraft.stat.Stat;
import net.minecraft.stat.StatFormatter;
import net.minecraft.stat.StatType;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.registry.Registry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class DatapackExtensions implements ModInitializer {

    public static final String MODID = "tdcdata";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

    public static final Identifier STAT_LEFT_CLICK = new Identifier(MODID, "mouse_left_click");
    public static final Identifier STAT_RIGHT_CLICK = new Identifier(MODID, "mouse_right_click");

    @Override
    public void onInitialize() {
        registerMouseClicks();
        ArgumentTypeRegistry.registerArgumentType(new Identifier(MODID, "hand"), HandArgumentType.class, ConstantArgumentSerializer.of(HandArgumentType::hand));

        CommandRegistrationCallback.EVENT.register(
                (dispatcher, dedicated, registryAccess) -> {
                    SwingCommand.register(dispatcher, registryAccess);
                    FreezeCommand.register(dispatcher, registryAccess);
                    HealthCommand.register(dispatcher, registryAccess);
                    TeamModifyCommandAdditions.register(dispatcher, registryAccess);
                    ScoreboardCommandAdditions.register(dispatcher, registryAccess);
                    TimeCommandAdditions.register(dispatcher, registryAccess);
                }
        );
        TdcDataAdvancementTriggers.registerTriggers();
        LOGGER.info("Datapack Extensions initialized!");
    }

    private void registerMouseClicks() {

        Registry.register(Registry.CUSTOM_STAT, STAT_LEFT_CLICK, STAT_LEFT_CLICK);
        Registry.register(Registry.CUSTOM_STAT, STAT_RIGHT_CLICK, STAT_RIGHT_CLICK);

        Stats.CUSTOM.getOrCreateStat(STAT_LEFT_CLICK, StatFormatter.DEFAULT);
        Stats.CUSTOM.getOrCreateStat(STAT_RIGHT_CLICK, StatFormatter.DEFAULT);

        // Right click detection
        UseItemCallback.EVENT.register((player, world, hand) -> {
            player.incrementStat(STAT_RIGHT_CLICK);

            //player.getStackInHand(hand)
            //player.swingHand(player.getActiveHand());
            return TypedActionResult.pass(ItemStack.EMPTY);
        });
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            player.incrementStat(STAT_RIGHT_CLICK);

            return ActionResult.PASS;
        });
        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            player.incrementStat(STAT_RIGHT_CLICK);

            return ActionResult.PASS;
        });

        // Left click detection
        AttackEntityCallback.EVENT.register(((player, world, hand, entity, hitResult) -> {
            player.incrementStat(STAT_LEFT_CLICK);

            return ActionResult.PASS;
        }));
        AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
            player.incrementStat(STAT_LEFT_CLICK);

            return ActionResult.PASS;
        });
    }
}

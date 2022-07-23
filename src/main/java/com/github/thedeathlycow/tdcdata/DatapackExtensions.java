package com.github.thedeathlycow.tdcdata;

import com.github.thedeathlycow.tdcdata.advancement.TdcDataAdvancementTriggers;
import com.github.thedeathlycow.tdcdata.scoreboard.stat.TdcDataCustomStats;
import com.github.thedeathlycow.tdcdata.server.command.*;
import com.github.thedeathlycow.tdcdata.server.command.ScoreboardCommandAdditions;
import com.github.thedeathlycow.tdcdata.server.command.argument.HandArgumentType;
import com.github.thedeathlycow.tdcdata.server.command.argument.NbtTypesArgumentType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.player.*;
import net.minecraft.command.argument.serialize.ConstantArgumentSerializer;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class DatapackExtensions implements ModInitializer {

    public static final String MODID = "tdcdata";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

    @Override
    public void onInitialize() {
        ArgumentTypeRegistry.registerArgumentType(new Identifier(MODID, "hand"), HandArgumentType.class, ConstantArgumentSerializer.of(HandArgumentType::hand));
        ArgumentTypeRegistry.registerArgumentType(new Identifier(MODID, "nbt_type"), NbtTypesArgumentType.class, ConstantArgumentSerializer.of(NbtTypesArgumentType::types));

        TdcDataCustomStats.registerCustomStats();

        CommandRegistrationCallback.EVENT.register(
                (dispatcher, dedicated, registryAccess) -> {
                    VarCommand.register(dispatcher, registryAccess);
                    AnimateCommand.register(dispatcher, registryAccess);
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
}

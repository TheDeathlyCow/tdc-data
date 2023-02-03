package com.github.thedeathlycow.tdcdata;

import com.github.thedeathlycow.tdcdata.advancement.TdcDataAdvancementTriggers;
import com.github.thedeathlycow.tdcdata.data.item.attribute.ItemAttributeLoader;
import com.github.thedeathlycow.tdcdata.scoreboard.stat.TdcDataCustomStats;
import com.github.thedeathlycow.tdcdata.server.command.*;
import com.github.thedeathlycow.tdcdata.server.command.argument.HandArgumentType;
import com.github.thedeathlycow.tdcdata.server.command.argument.NbtTypesArgumentType;
import com.github.thedeathlycow.tdcdata.server.command.argument.UnaryOperationArgumentType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.command.argument.serialize.ConstantArgumentSerializer;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class DatapackExtensions implements ModInitializer {

    public static final String MODID = "tdcdata";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

    @Override
    public void onInitialize() {
        ArgumentTypeRegistry.registerArgumentType(id("hand"), HandArgumentType.class, ConstantArgumentSerializer.of(HandArgumentType::hand));
        ArgumentTypeRegistry.registerArgumentType(id("nbt_type"), NbtTypesArgumentType.class, ConstantArgumentSerializer.of(NbtTypesArgumentType::types));
        ArgumentTypeRegistry.registerArgumentType(id("unary_operation"), UnaryOperationArgumentType.class, ConstantArgumentSerializer.of(UnaryOperationArgumentType::unaryOperation));

        TdcDataCustomStats.registerCustomStats();

        CommandRegistrationCallback.EVENT.register(
                (dispatcher, dedicated, registryAccess) -> {
                    MotionCommand.register(dispatcher, registryAccess);
                    SuperEnchantCommand.register(dispatcher, registryAccess);
                    PlayAnimationCommand.register(dispatcher, registryAccess);
                    RideCommand.register(dispatcher, registryAccess);
                    FreezeCommand.register(dispatcher, registryAccess);
                    HealthCommand.register(dispatcher, registryAccess);
                    TeamModifyCommandAdditions.register(dispatcher, registryAccess);
                    ScoreboardCommandAdditions.register(dispatcher, registryAccess);
                    TimeCommandAdditions.register(dispatcher, registryAccess);
                }
        );
        TdcDataAdvancementTriggers.registerTriggers();

        ResourceManagerHelper serverManager = ResourceManagerHelper.get(ResourceType.SERVER_DATA);

        serverManager.registerReloadListener(ItemAttributeLoader.INSTANCE);


        LOGGER.info("Datapack Extensions initialized!");
    }

    public static Identifier id(String path) {
        return new Identifier(MODID, path);
    }
}

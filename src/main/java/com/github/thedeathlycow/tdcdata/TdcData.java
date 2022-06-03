package com.github.thedeathlycow.tdcdata;

import com.github.thedeathlycow.tdcdata.server.command.FreezeCommand;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class TdcData implements ModInitializer {

    public static final String MODID = "tdcdata";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

    @Override
    public void onInitialize() {

        CommandRegistrationCallback.EVENT.register(
                (dispatcher, dedicated) -> {
                    FreezeCommand.register(dispatcher);
                }
        );

        LOGGER.info("TheDeathlyCow's Datapack Utils initialized!");
    }
}

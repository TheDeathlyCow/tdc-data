package com.github.thedeathlycow.tdcdata;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class TdcData implements ModInitializer {

    public static final String MODID = "tdcdata";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

    @Override
    public void onInitialize() {
        LOGGER.info("TheDeathlyCow's Datapack Utils initialized!");
    }
}

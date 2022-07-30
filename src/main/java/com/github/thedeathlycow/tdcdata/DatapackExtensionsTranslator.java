package com.github.thedeathlycow.tdcdata;

import com.google.gson.JsonParser;
import net.minecraft.resource.Resource;
import net.minecraft.text.Text;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.MissingFormatArgumentException;

public class DatapackExtensionsTranslator {
    private static final Map<String, String> translations = new HashMap<>();

    public static void loadTranslations(Resource resource) {
        try {
            JsonParser.parseReader(resource.getReader()).getAsJsonObject().entrySet().forEach(entry -> {
                translations.put(entry.getKey(), entry.getValue().getAsString());
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String translate(String key, Object... args) {
        try {
            return String.format(translations.getOrDefault(key, key), args);
        }
        catch (MissingFormatArgumentException e) {
            return translations.getOrDefault(key, key);
        }
    }

    public static Text translateAsText(String key, Object... args) {
        return Text.literal(translate(key, args));
    }
}

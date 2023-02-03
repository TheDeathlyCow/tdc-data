package com.github.thedeathlycow.tdcdata.data.attribute.item;


import com.github.thedeathlycow.tdcdata.DatapackExtensions;
import com.google.common.collect.Multimap;
import com.google.gson.JsonParseException;
import net.fabricmc.fabric.api.item.v1.ModifyItemAttributeModifiersCallback;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.item.ItemStack;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ItemAttributeLoader implements SimpleSynchronousResourceReloadListener, ModifyItemAttributeModifiersCallback {

    public static final ItemAttributeLoader INSTANCE = new ItemAttributeLoader(DatapackExtensions.id("item_attribute_modifiers"));

    private final Identifier identifier;

    private final Map<Identifier, ItemBaseAttributeModifier> values = new HashMap<>();

    public ItemAttributeLoader(Identifier identifier) {
        this.identifier = identifier;

        ModifyItemAttributeModifiersCallback.EVENT.register(this);
    }

    @Override
    public Identifier getFabricId() {
        return this.identifier;
    }

    @Override
    public void reload(ResourceManager manager) {
        Map<Identifier, ItemBaseAttributeModifier> newValues = new HashMap<>();

        for (var entry : manager.findResources("tdcdata/item_attribute_modifiers", id -> id.getPath().endsWith(".json")).entrySet()) {
            try (BufferedReader reader = entry.getValue().getReader()) {

                ItemBaseAttributeModifier modifier = ItemBaseAttributeModifier.Serializer.GSON.fromJson(
                        reader,
                        ItemBaseAttributeModifier.class
                );

                newValues.put(entry.getKey(), modifier);
            } catch (IOException | JsonParseException e) {
                DatapackExtensions.LOGGER.error("An error occurred while loading item attribute modifier {}: {}", entry.getKey(), e);
            }
        }

        this.values.clear();
        this.values.putAll(newValues);

        int numModifiers = this.values.size();
        DatapackExtensions.LOGGER.info("Loaded {} item attribute modifier{}", numModifiers, numModifiers == 1 ? "" : "s");

    }

    @Override
    public void modifyAttributeModifiers(ItemStack stack, EquipmentSlot slot, Multimap<EntityAttribute, EntityAttributeModifier> attributeModifiers) {
        for (var value : this.values.values()) {
            value.apply(stack, slot, attributeModifiers);
        }
    }
}

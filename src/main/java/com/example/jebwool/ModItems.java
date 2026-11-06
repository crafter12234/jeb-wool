package com.example.jebwool;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;

/** Registration for items that are not blocks. */
public class ModItems {
    /** The cushion stacks to 16, like the vanilla version. */
    public static final Item JEB_CUSHION = register("jeb_cushion");

    private static Item register(String name) {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM,
                Identifier.fromNamespaceAndPath(JebWoolMod.MOD_ID, name));
        Item item = new JebCushionItem(new Item.Properties().setId(key).stacksTo(16));
        return Registry.register(BuiltInRegistries.ITEM, key, item);
    }

    /** Empty method: needed to force the class to load. */
    public static void init() {
    }
}

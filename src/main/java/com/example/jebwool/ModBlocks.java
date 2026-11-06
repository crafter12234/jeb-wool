package com.example.jebwool;

import java.util.function.Function;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CarpetBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

/** Registration for all mod blocks and block items. */
public class ModBlocks {

    /**
     * Wool properties. In 26.3, vanilla Blocks.WHITE_WOOL was removed,
     * so we set hardness, sound, and flammability manually.
     */
    private static BlockBehaviour.Properties woolProps() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_PURPLE)
                .strength(0.8F)
                .sound(SoundType.WOOL)
                .ignitedByLava();
    }

    public static final Block JEB_WOOL = register("jeb_wool", Block::new, woolProps(), 64);

    public static final Block JEB_CARPET = register("jeb_carpet", CarpetBlock::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_PURPLE)
                    .strength(0.1F)
                    .sound(SoundType.WOOL)
                    .ignitedByLava(), 64);

    public static final Block JEB_WOOL_SLAB = register("jeb_wool_slab", SlabBlock::new, woolProps(), 64);

    public static final Block JEB_WOOL_STAIRS = register("jeb_wool_stairs",
            p -> new StairBlock(JEB_WOOL.defaultBlockState(), p), woolProps(), 64);

    /** Bed: stacks to 1, like a vanilla bed. */
    public static final Block JEB_BED = register("jeb_bed", JebBedBlock::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_PURPLE)
                    .sound(SoundType.WOOD)
                    .strength(0.2F)
                    .noOcclusion()
                    .ignitedByLava(), 1);

    /**
     * Registers a block and its corresponding item under the same name.
     *
     * @param name      name without a namespace, for example "jeb_wool"
     * @param factory   block constructor
     * @param props     block properties
     * @param stackSize maximum stack size for the item
     */
    private static <T extends Block> T register(String name, Function<BlockBehaviour.Properties, T> factory,
                                                BlockBehaviour.Properties props, int stackSize) {
        Identifier id = Identifier.fromNamespaceAndPath(JebWoolMod.MOD_ID, name);
        ResourceKey<Block> blockKey = ResourceKey.create(Registries.BLOCK, id);
        ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, id);

        // From 1.21.2 onward, blocks and items must always receive an ID via setId.
        T block = factory.apply(props.setId(blockKey));
        Registry.register(BuiltInRegistries.BLOCK, blockKey, block);

        Item.Properties itemProps = new Item.Properties().setId(itemKey).useBlockDescriptionPrefix();
        if (stackSize != 64) itemProps.stacksTo(stackSize);
        Registry.register(BuiltInRegistries.ITEM, itemKey, new BlockItem(block, itemProps));
        return block;
    }

    /** Empty method: needed to force this class to load and register blocks. */
    public static void init() {
    }
}

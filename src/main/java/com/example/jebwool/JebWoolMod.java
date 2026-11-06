package com.example.jebwool;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

/**
 * Main mod class for "jeb_ Wool".
 *
 * What the mod does:
 *  - if you kill or shear a sheep named "jeb_", it drops 1-3 blocks of jeb_ wool;
 *  - this wool is used to craft a carpet, slab, stairs, bed, and cushion;
 *  - all of these items smoothly cycle through all 16 wool colors, like the jeb_ sheep itself.
 */
public class JebWoolMod implements ModInitializer {
    /** Mod identifier. It must match fabric.mod.json and the asset/data folder names. */
    public static final String MOD_ID = "jeb_wool";

    /** Name of the sheep that drops the special wool (case-sensitive). */
    public static final String JEB_NAME = "jeb_";

    @Override
    public void onInitialize() {
        // Touching these classes forces them to load and register their content.
        ModBlocks.init();
        ModEntities.init();
        ModItems.init();

        registerCreativeTab();
        registerFlammability();
        registerSheepDrops();
    }

    /** Adds the mod's items to the creative mode tab "Colored Blocks". */
    private static void registerCreativeTab() {
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.COLORED_BLOCKS).register(output -> {
            output.accept(ModBlocks.JEB_WOOL);
            output.accept(ModBlocks.JEB_CARPET);
            output.accept(ModBlocks.JEB_WOOL_SLAB);
            output.accept(ModBlocks.JEB_WOOL_STAIRS);
            output.accept(ModBlocks.JEB_BED);
            output.accept(ModItems.JEB_CUSHION);
        });
    }

    /** Wool burns the same way as vanilla wool (values: ignition chance / burn speed). */
    private static void registerFlammability() {
        FlammableBlockRegistry fire = FlammableBlockRegistry.getDefaultInstance();
        for (Block block : new Block[]{ModBlocks.JEB_WOOL, ModBlocks.JEB_WOOL_SLAB, ModBlocks.JEB_WOOL_STAIRS}) {
            fire.add(block, 30, 60);
        }
        fire.add(ModBlocks.JEB_CARPET, 60, 20);
    }

    /** Wool drops: when a sheep is killed or sheared. */
    private static void registerSheepDrops() {
        // 1) Killing. Drops only from an adult sheep that has not yet been sheared.
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
            if (entity instanceof Sheep sheep
                    && entity.level() instanceof ServerLevel level
                    && isJeb(sheep) && !sheep.isSheared() && !sheep.isBaby()) {
                dropWool(level, sheep);
            }
        });

        // 2) Shearing: right-click a jeb_ sheep with shears.
        UseEntityCallback.EVENT.register((player, world, hand, entity, hit) -> {
            if (player.isSpectator() || !(entity instanceof Sheep sheep) || !isJeb(sheep)) {
                return InteractionResult.PASS; // not our case — let the game handle it normally
            }
            ItemStack stack = player.getItemInHand(hand);
            if (!stack.is(Items.SHEARS) || !sheep.readyForShearing()) {
                return InteractionResult.PASS;
            }
            if (world instanceof ServerLevel level) {
                level.playSound(null, sheep.getX(), sheep.getY(), sheep.getZ(),
                        SoundEvents.SHEEP_SHEAR, SoundSource.PLAYERS, 1.0F, 1.0F);
                sheep.setSheared(true);   // the sheep becomes sheared
                dropWool(level, sheep);   // give our custom wool instead of vanilla wool
                stack.hurtAndBreak(1, player,
                        hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
            }
            return InteractionResult.SUCCESS;
        });
    }

    /** Drops 1 to 3 blocks of jeb_ wool at the sheep's position. */
    private static void dropWool(ServerLevel level, Sheep sheep) {
        int count = 1 + level.getRandom().nextInt(3);
        sheep.spawnAtLocation(level, new ItemStack(ModBlocks.JEB_WOOL.asItem(), count));
    }

    /** Checks that the sheep is named exactly "jeb_" (for example via a name tag). */
    public static boolean isJeb(Sheep sheep) {
        return sheep.hasCustomName() && JEB_NAME.equals(sheep.getCustomName().getString());
    }
}

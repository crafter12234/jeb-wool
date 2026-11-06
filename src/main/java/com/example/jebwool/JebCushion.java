package com.example.jebwool;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.Cushion;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * jeb_ cushion. This is the vanilla cushion from 26.3 (a sit-able entity),
 * so sitting, physics, and persistence work the same as vanilla objects.
 * The difference is that it drops as our custom item and is rendered in a shimmering color cycle.
 */
public class JebCushion extends Cushion {

    /** The parent class expects EntityType<Cushion>, so we cast the type accordingly. */
    @SuppressWarnings("unchecked")
    public JebCushion(EntityType<JebCushion> type, Level level) {
        super((EntityType<Cushion>) (EntityType<?>) type, level);
    }

    /** What drops when the cushion is destroyed. */
    @Override
    public void dropItem(ServerLevel level, Entity breaker) {
        this.playSound(SoundEvents.WOOL_BREAK, 1.0F, 1.0F);
        // In creative mode, no item drops.
        if (breaker instanceof Player player && player.hasInfiniteMaterials()) {
            return;
        }
        this.spawnAtLocation(level, new ItemStack(ModItems.JEB_CUSHION));
    }

    /** The item the player receives when middle-clicking the cushion. */
    @Override
    public ItemStack getPickResult() {
        return new ItemStack(ModItems.JEB_CUSHION);
    }
}

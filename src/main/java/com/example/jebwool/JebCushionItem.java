package com.example.jebwool;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** Cushion item: on right-click, it places a JebCushion entity on the top face of a block. */
public class JebCushionItem extends Item {
    public JebCushionItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        // Place only on the top face of the block.
        if (context.getClickedFace() != Direction.UP) {
            return InteractionResult.FAIL;
        }
        Level level = context.getLevel();
        if (!(level instanceof ServerLevel server)) {
            return InteractionResult.SUCCESS; // on the client, only the hand animation is visible
        }

        BlockPos base = context.getClickedPos();
        Vec3 click = context.getClickLocation();
        // Horizontally centered on the block; vertically aligned to the block surface height (accounting for slabs).
        double x = base.getX() + 0.5;
        double z = base.getZ() + 0.5;
        double y = click.y;

        JebCushion cushion = new JebCushion(ModEntities.JEB_CUSHION, server);
        cushion.setPos(x, y, z);
        cushion.setYRot(context.getHorizontalDirection().toYRot()); // rotation based on the player's facing direction
        server.addFreshEntity(cushion);

        server.playSound(null, x, y, z, SoundEvents.WOOL_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
        context.getItemInHand().consume(1, context.getPlayer()); // in creative mode, it is not consumed
        return InteractionResult.SUCCESS;
    }
}

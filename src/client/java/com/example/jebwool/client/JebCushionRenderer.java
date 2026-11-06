package com.example.jebwool.client;

import net.minecraft.client.renderer.entity.CushionRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.CushionRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.decoration.Cushion;
import net.minecraft.world.item.DyeColor;

/**
 * Renderer for the jeb_ cushion. It uses the vanilla cushion renderer and swaps in the next vanilla color texture
 * every 25 ticks (matching the jeb_ sheep behavior).
 * The game cannot animate entity textures, so the color changes in steps here.
 */
public class JebCushionRenderer extends CushionRenderer {
    /** How many ticks each color remains visible (20 ticks = 1 second). */
    private static final float TICKS_PER_COLOR = 25.0F;

    public JebCushionRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void extractRenderState(Cushion entity, CushionRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick); // vanilla state: facing direction, etc.

        // Color index 0..15 in the order of vanilla dye colors.
        int index = Math.floorMod((int) Math.floor((entity.tickCount + partialTick) / TICKS_PER_COLOR), 16);
        DyeColor color = DyeColor.byId(index);

        // Replace the texture with the vanilla cushion texture for the desired color.
        state.texture = Identifier.withDefaultNamespace(
                "textures/entity/cushion/" + color.getSerializedName() + "_cushion.png");
    }
}

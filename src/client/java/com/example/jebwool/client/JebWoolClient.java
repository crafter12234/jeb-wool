package com.example.jebwool.client;

import com.example.jebwool.ModEntities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

/** Client-side mod logic: tells the game how to render the jeb_ cushion. */
public class JebWoolClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(ModEntities.JEB_CUSHION, JebCushionRenderer::new);
    }
}

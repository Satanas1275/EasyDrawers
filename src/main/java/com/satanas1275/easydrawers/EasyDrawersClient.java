package com.satanas1275.easydrawers;

import com.satanas1275.easydrawers.client.render.DrawerBlockEntityRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;

public class EasyDrawersClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BlockEntityRendererRegistry.register(EasyDrawers.DRAWER_BLOCK_ENTITY, DrawerBlockEntityRenderer::new);
    }
}

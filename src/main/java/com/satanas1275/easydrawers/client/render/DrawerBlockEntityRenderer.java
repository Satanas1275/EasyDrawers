package com.satanas1275.easydrawers.client.render;

import com.satanas1275.easydrawers.block.DrawerBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class DrawerBlockEntityRenderer implements BlockEntityRenderer<DrawerBlockEntity, DrawerRenderState> {
    private final ItemModelResolver itemModelResolver;

    public DrawerBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {
        this.itemModelResolver = ctx.itemModelResolver();
    }

    @Override
    public DrawerRenderState createRenderState() {
        return new DrawerRenderState();
    }

    @Override
    public void extractRenderState(DrawerBlockEntity be, DrawerRenderState state, float partialTick, Vec3 cameraPos, ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderState.extractBase(be, state, crumblingOverlay);
        state.storedItem = be.getStoredItem();
        state.count = be.getCount();
        state.facing = be.getBlockState().getValue(HorizontalDirectionalBlock.FACING);
    }

    @Override
    public void submit(DrawerRenderState state, PoseStack poseStack, SubmitNodeCollector nodeCollector, CameraRenderState cameraRenderState) {
        if (state.storedItem.isEmpty()) return;

        Vector3f facingVec = new Vector3f(
            state.facing.getStepX(),
            state.facing.getStepY(),
            state.facing.getStepZ()
        );
        Quaternionf rot = new Quaternionf().rotationTo(new Vector3f(0, 0, 1), facingVec);

        int light = 15728880;

        poseStack.pushPose();
        poseStack.translate(0.5f, 0.65f, 0.5f);
        poseStack.mulPose(rot);
        poseStack.translate(0, 0, 0.5f + 0.005f);
        poseStack.scale(0.5f, 0.5f, 0.5f);

        ItemStackRenderState itemState = new ItemStackRenderState();
        itemModelResolver.updateForTopItem(itemState, state.storedItem, ItemDisplayContext.FIXED, null, null, 0);
        itemState.submit(poseStack, nodeCollector, light, OverlayTexture.NO_OVERLAY, 0);
        poseStack.popPose();

        // Render count text on block face (no billboard)
        if (state.count >= 1) {
            poseStack.pushPose();
            poseStack.translate(0.5f, 0.36f, 0.5f);
            poseStack.mulPose(new Quaternionf().rotationTo(new Vector3f(0, 0, 1), facingVec));
            poseStack.translate(0, 0, 0.505f);

            // Cancel submitNameTag's internal billboard rotation
            poseStack.mulPose(new Quaternionf(cameraRenderState.orientation).conjugate());

            // submitNameTag adds offset.y+0.5 internally; pass -0.5 to cancel
            Vec3 offset = new Vec3(0, -0.5, 0);
            Component text = Component.literal(String.valueOf(state.count));
            nodeCollector.submitNameTag(poseStack, offset, 0, text, false, light, 0.0, cameraRenderState);
            poseStack.popPose();
        }
    }
}

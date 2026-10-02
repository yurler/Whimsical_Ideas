package com.whimsical.ideas;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.whimsical.ideas.client.SkinFetcher;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

public class PlushieRenderer implements BlockEntityRenderer<PlushieBlockEntity> {

    private final PlushieModel model;

    public PlushieRenderer(BlockEntityRendererProvider.Context context) {
        this.model = new PlushieModel(context.bakeLayer(ModModelLayers.PLUSHIE_LAYER));
    }

    @Override
    public void render(PlushieBlockEntity entity, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {

        PlushieData data = entity.getData();
        ResourceLocation texture;

        if (data.hasSkinData()) {
            texture = SkinFetcher.registerFromBytes(
                    data.getSkinData(),
                    "nbt:" + data.getOwnerName() + ":" + data.getUploadFile()
            );
        } else if ("upload".equals(data.getSkinSource()) && !data.getUploadFile().isEmpty()) {
            texture = SkinFetcher.getLocalSkin(data.getUploadFile());
        } else {
            texture = SkinFetcher.getSkin(data.getOwnerName());
        }

        BlockState state = entity.getBlockState();
        int rotation = state.hasProperty(PlushieBlock.ROTATION)
                ? state.getValue(PlushieBlock.ROTATION)
                : 0;

        poseStack.pushPose();
        poseStack.translate(0.5, 1.5, 0.5);
        poseStack.mulPose(Axis.YP.rotationDegrees(rotation * 45.0F));
        poseStack.scale(-1.0F, -1.0F, 1.0F);

        VertexConsumer buffer = bufferSource.getBuffer(
                RenderType.entityCutoutNoCull(texture)
        );

        this.model.renderToBuffer(poseStack, buffer, packedLight, packedOverlay,
                1.0F, 1.0F, 1.0F, 1.0F);

        poseStack.popPose();
    }
}
package com.whimsical.ideas;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.whimsical.ideas.client.SkinFetcher;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class PlushieItemRenderer extends BlockEntityWithoutLevelRenderer {

    private static PlushieItemRenderer INSTANCE;

    private final PlushieModel model;

    public static PlushieItemRenderer getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new PlushieItemRenderer();
        }
        return INSTANCE;
    }

    private PlushieItemRenderer() {
        super(
                Minecraft.getInstance().getBlockEntityRenderDispatcher(),
                Minecraft.getInstance().getEntityModels()
        );
        this.model = new PlushieModel(
                Minecraft.getInstance().getEntityModels()
                        .bakeLayer(ModModelLayers.PLUSHIE_LAYER)
        );
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext context,
                             PoseStack poseStack, MultiBufferSource bufferSource,
                             int packedLight, int packedOverlay) {

        PlushieData data = PlushieData.fromItemStack(stack);
        ResourceLocation texture;

        if (data != null && data.hasSkinData()) {
            texture = SkinFetcher.registerFromBytes(
                    data.getSkinData(),
                    "nbt:" + data.getOwnerName() + ":" + data.getUploadFile()
            );
        } else if (data != null && "upload".equals(data.getSkinSource()) && !data.getUploadFile().isEmpty()) {
            texture = SkinFetcher.getLocalSkin(data.getUploadFile());
        } else if (data != null) {
            texture = SkinFetcher.getSkin(data.getOwnerName());
        } else {
            texture = SkinFetcher.FALLBACK;
        }

        poseStack.pushPose();

        switch (context) {
            case GUI -> {
                poseStack.translate(0.5F, 1.25F, 0.5F);
                poseStack.scale(-0.9F, -0.9F, 0.9F);
                poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
            }
            case GROUND -> {
                poseStack.translate(0.5F, 0.75F, 0.5F);
                poseStack.scale(-0.5F, -0.5F, 0.5F);
                poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
            }
            case FIXED -> {
                poseStack.translate(0.5F, 1.5F, 0.5F);
                poseStack.scale(-1.2F, -1.2F, 1.2F);
                poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
            }
            case FIRST_PERSON_LEFT_HAND, FIRST_PERSON_RIGHT_HAND -> {
                poseStack.translate(0.6F, 1.1F, 0.3F);
                poseStack.scale(-0.5F, -0.5F, 0.5F);
                poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
            }
            case THIRD_PERSON_LEFT_HAND, THIRD_PERSON_RIGHT_HAND -> {
                poseStack.translate(0.5F, 1.0F, 0.3F);
                poseStack.scale(-0.45F, -0.45F, 0.45F);
                poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
            }
            case HEAD -> {
                poseStack.translate(0.5F, 1.25F, 0.5F);
                poseStack.scale(-0.7F, -0.7F, 0.7F);
            }
            default -> {
                poseStack.translate(0.5F, 1.25F, 0.5F);
                poseStack.scale(-0.7F, -0.7F, 0.7F);
            }
        }

        VertexConsumer buffer = bufferSource.getBuffer(
                RenderType.entityCutoutNoCull(texture)
        );

        this.model.renderToBuffer(poseStack, buffer, packedLight, packedOverlay,
                1.0F, 1.0F, 1.0F, 1.0F);

        poseStack.popPose();
    }
}
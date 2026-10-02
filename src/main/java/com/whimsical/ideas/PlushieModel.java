package com.whimsical.ideas;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.RenderType;

public class PlushieModel extends Model {

    private final ModelPart root;

    public PlushieModel(ModelPart root) {
        super(RenderType::entityCutoutNoCull);
        this.root = root;
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition part = mesh.getRoot();

        // --- 头部（底层 + 叠加层）---
        // 尺寸 8×8×8，UV 用标准玩家布局
        part.addOrReplaceChild("head", CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F)
                        .texOffs(32, 0)
                        .addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.25F)),
                PartPose.offset(0.0F, 16.0F, 0.0F));

        // --- 身体 ---
        // 尺寸 6×8×4，UV 用标准玩家布局的身体区域
        part.addOrReplaceChild("body", CubeListBuilder.create()
                        .texOffs(16, 16)
                        .addBox(-3.0F, 0.0F, -2.0F, 6.0F, 8.0F, 4.0F),
                PartPose.offset(0.0F, 15.0F, 0.0F));

        // --- 右手臂 ---
        // 尺寸 2×9×2.5，UV 用标准玩家右臂区域
        part.addOrReplaceChild("right_arm", CubeListBuilder.create()
                        .texOffs(40, 16)
                        .addBox(1.8F, -0.47F, -1.25F, 2.0F, 9.0F, 2.5F),
                PartPose.offsetAndRotation(0.5F, 16.5F, 0.0F,
                        0.0F, 0.0F, -0.3927F));

        // --- 左手臂 ---
        // 尺寸 2×9×2.5，UV 用标准玩家左臂区域
        part.addOrReplaceChild("left_arm", CubeListBuilder.create()
                        .texOffs(32, 48)
                        .addBox(-3.8F, -0.47F, -1.25F, 2.0F, 9.0F, 2.5F),
                PartPose.offsetAndRotation(-0.5F, 16.5F, 0.0F,
                        0.0F, 0.0F, 0.3927F));

        // --- 右腿 ---
        // 尺寸 3×3×7，UV 用标准玩家右腿区域
        part.addOrReplaceChild("right_leg", CubeListBuilder.create()
                        .texOffs(0, 16)
                        .addBox(-1.5F, 0.0F, -7.0F, 3.0F, 3.0F, 7.0F),
                PartPose.offsetAndRotation(1.5F, 21.5F, 0.5F,
                        0.0F, -0.4F, 0.0F));

        // --- 左腿 ---
        // 尺寸 3×3×7，UV 用标准玩家左腿区域
        part.addOrReplaceChild("left_leg", CubeListBuilder.create()
                        .texOffs(16, 48)
                        .addBox(-1.5F, 0.0F, -7.0F, 3.0F, 3.0F, 7.0F),
                PartPose.offsetAndRotation(-1.5F, 21.5F, 0.5F,
                        0.0F, 0.4F, 0.0F));

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer,
                               int packedLight, int packedOverlay,
                               float red, float green, float blue, float alpha) {
        this.root.render(poseStack, buffer, packedLight, packedOverlay,
                red, green, blue, alpha);
    }
}
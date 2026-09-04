package io.github.dogeiscut.inventory_spirits.content.inventory_spirit;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.dogeiscut.inventory_spirits.InventorySpirits;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;

public class InventorySpiritModel<T extends InventorySpiritEntity> extends EntityModel<T> {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(InventorySpirits.ID, "inventory_spirit"), "main");
	private final ModelPart rotator;
	private final ModelPart outer_cube;
	private final ModelPart inner_cube;

	public InventorySpiritModel(ModelPart root) {
		this.rotator = root.getChild("rotator");
		this.outer_cube = root.getChild("outer_cube");
		this.inner_cube = this.outer_cube.getChild("inner_cube");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition rotator = partdefinition.addOrReplaceChild("rotator", CubeListBuilder.create().texOffs(-16, 12).addBox(-8.0F, 0.0F, -8.0F, 16.0F, 0.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 6.5F, 0.0F));

		PartDefinition outer_cube = partdefinition.addOrReplaceChild("outer_cube", CubeListBuilder.create().texOffs(0, 0).addBox(-3.0F, -3.0F, -3.0F, 6.0F, 6.0F, 6.0F, new CubeDeformation(-6.0F)), PartPose.offset(0.0F, 6.5F, 0.0F));

		PartDefinition inner_cube = outer_cube.addOrReplaceChild("inner_cube", CubeListBuilder.create().texOffs(24, 0).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 64, 64);
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
		rotator.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
		outer_cube.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
	}

	@Override
	public void setupAnim(T t, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		this.rotator.yRot = ageInTicks * 0.4f;
		this.outer_cube.xRot = ageInTicks * 0.067690f;
		this.outer_cube.yRot = ageInTicks * 0.04782f;
		this.outer_cube.zRot = ageInTicks * 0.086945f;
		this.inner_cube.yRot = ageInTicks * -0.042341f;
		this.inner_cube.xRot = ageInTicks * -0.07567f;
		this.inner_cube.zRot = ageInTicks * -0.0679096f;
	}
}
package io.github.dogeiscut.inventory_spirits.content.inventory_spirit;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.dogeiscut.inventory_spirits.InventorySpirits;
import io.github.dogeiscut.inventory_spirits.InventorySpiritsClient;
import io.github.dogeiscut.inventory_spirits.registry.IsRenderTypes;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class InventorySpiritRenderer extends EntityRenderer<InventorySpiritEntity> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(InventorySpirits.ID, "textures/entity/inventory_spirit/inventory_spirit.png");
    private final InventorySpiritModel model;

    public InventorySpiritRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new InventorySpiritModel(context.bakeLayer(InventorySpiritsClient.INVENTORY_SPIRIT_LAYER));
    }

    @Override
    public void render(InventorySpiritEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);

        VertexConsumer vertexConsumer = buffer.getBuffer(IsRenderTypes.unshadedEmissive(TEXTURE));

        this.model.setupAnim(entity, 0.0f, 0.0f, entity.tickCount + partialTick, 0.0f, 0.0f);
        this.model.renderToBuffer(
                poseStack,
                vertexConsumer,
                LightTexture.FULL_BRIGHT,
                OverlayTexture.NO_OVERLAY
        );
    }

    @Override
    public ResourceLocation getTextureLocation(InventorySpiritEntity inventorySpiritEntity) {
        return TEXTURE;
    }
}

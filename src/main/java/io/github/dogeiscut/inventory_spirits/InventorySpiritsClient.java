package io.github.dogeiscut.inventory_spirits;

import io.github.dogeiscut.inventory_spirits.content.inventory_spirit.InventorySpiritDustParticle;
import io.github.dogeiscut.inventory_spirits.content.inventory_spirit.InventorySpiritModel;
import io.github.dogeiscut.inventory_spirits.content.inventory_spirit.InventorySpiritRenderer;
import io.github.dogeiscut.inventory_spirits.registry.ISEntities;
import io.github.dogeiscut.inventory_spirits.registry.ISParticles;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;

@EventBusSubscriber(modid = InventorySpirits.ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class InventorySpiritsClient {
    public static final ModelLayerLocation INVENTORY_SPIRIT_LAYER = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(InventorySpirits.ID, "inventory_spirit"), "main");

    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(INVENTORY_SPIRIT_LAYER, InventorySpiritModel::createBodyLayer);
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ISEntities.INVENTORY_SPIRIT.get(), InventorySpiritRenderer::new);
    }

    @SubscribeEvent
    public static void registerParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ISParticles.INVENTORY_SPIRIT_DUST.get(), InventorySpiritDustParticle.Provider::new);
    }
}

package io.github.dogeiscut.inventory_spirits.compat.jade;

import io.github.dogeiscut.inventory_spirits.InventorySpirits;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

import java.util.Optional;

public class InventorySpiritComponentProvider implements IEntityComponentProvider {
    public static final InventorySpiritComponentProvider INSTANCE = new InventorySpiritComponentProvider();
    public static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(InventorySpirits.ID, "spirit_contents");

    private InventorySpiritComponentProvider() {
    }

    @Override
    public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
        Optional<InventorySpiritDataProvider.Data> data = InventorySpiritDataProvider.INSTANCE.decodeFromData(accessor);
        data.ifPresent(d -> {
            if (d.storedItemCount() > 0) {
                tooltip.add(Component.translatable("jade.inventory_spirits.item_count", d.storedItemCount()));
            }
            if (d.totalExperience() > 0) {
                tooltip.add(Component.translatable("jade.inventory_spirits.experience", d.totalExperience()));
            }
        });
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }
}
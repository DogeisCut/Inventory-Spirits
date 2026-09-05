package io.github.dogeiscut.inventory_spirits.compat.jade;

import io.github.dogeiscut.inventory_spirits.InventorySpirits;
import io.github.dogeiscut.inventory_spirits.content.inventory_spirit.InventorySpiritEntity;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.StreamServerDataProvider;

public class InventorySpiritDataProvider implements StreamServerDataProvider<EntityAccessor, InventorySpiritDataProvider.Data> {
    public static final InventorySpiritDataProvider INSTANCE = new InventorySpiritDataProvider();
    public static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(InventorySpirits.ID, "spirit_contents");

    private InventorySpiritDataProvider() {
    }

    @Override
    public @Nullable Data streamData(EntityAccessor accessor) {
        if (!(accessor.getEntity() instanceof InventorySpiritEntity spirit)) return null;
        return new Data(spirit.getStoredItems().size(), spirit.getTotalExperience());
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, Data> streamCodec() {
        return Data.STREAM_CODEC;
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }

    public record Data(int storedItemCount, int totalExperience) {
        public static final StreamCodec<RegistryFriendlyByteBuf, Data> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, Data::storedItemCount,
                ByteBufCodecs.VAR_INT, Data::totalExperience,
                Data::new
        );
    }
}
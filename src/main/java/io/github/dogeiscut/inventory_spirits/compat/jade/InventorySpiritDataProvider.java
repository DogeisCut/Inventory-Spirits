package io.github.dogeiscut.inventory_spirits.compat.jade;

import io.github.dogeiscut.inventory_spirits.InventorySpirits;
import io.github.dogeiscut.inventory_spirits.content.inventory_spirit.InventorySpiritEntity;
import io.github.dogeiscut.inventory_spirits.content.inventory_spirit.StoredItemRecord;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.StreamServerDataProvider;

import java.util.ArrayList;
import java.util.List;

public class InventorySpiritDataProvider implements StreamServerDataProvider<EntityAccessor, InventorySpiritDataProvider.Data> {
    public static final InventorySpiritDataProvider INSTANCE = new InventorySpiritDataProvider();
    public static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(InventorySpirits.ID, "spirit_contents");

    private InventorySpiritDataProvider() {
    }

    @Override
    public @Nullable Data streamData(EntityAccessor accessor) {
        if (!(accessor.getEntity() instanceof InventorySpiritEntity spirit)) return null;

        List<ItemStack> items = new ArrayList<>(spirit.getStoredItems().size());
        for (StoredItemRecord record : spirit.getStoredItems()) {
            items.add(record.stack());
        }

        return new Data(items, spirit.getTotalExperience());
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, Data> streamCodec() {
        return Data.STREAM_CODEC;
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }

    public record Data(List<ItemStack> items, int totalExperience) {
        public static final StreamCodec<RegistryFriendlyByteBuf, Data> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.collection(ArrayList::new, ItemStack.STREAM_CODEC), Data::items,
                ByteBufCodecs.VAR_INT, Data::totalExperience,
                Data::new
        );
    }
}
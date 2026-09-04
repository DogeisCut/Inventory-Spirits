package io.github.dogeiscut.inventory_spirits.content.inventory_spirit;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public record StoredItemRecord(ItemStack stack, String category, int originalSlot, String subType) {
    public StoredItemRecord(ItemStack stack, String category, int originalSlot, String subType) {
        this.stack = stack.copy();
        this.category = category;
        this.originalSlot = originalSlot;
        this.subType = subType;
    }

    public CompoundTag save(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        tag.put("Item", this.stack.save(registries));
        tag.putString("Category", this.category);
        tag.putInt("OriginalSlot", this.originalSlot);
        tag.putString("SubType", this.subType);
        return tag;
    }

    public static StoredItemRecord load(CompoundTag tag, HolderLookup.Provider registries) {
        ItemStack stack = ItemStack.parseOptional(registries, tag.getCompound("Item"));
        String category = tag.getString("Category");
        int slot = tag.getInt("OriginalSlot");
        String subType = tag.getString("SubType");
        return new StoredItemRecord(stack, category, slot, subType);
    }
}

package io.github.dogeiscut.inventory_spirits.content.inventory_spirit;

import io.github.dogeiscut.inventory_spirits.InventorySpirits;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public record StoredItemRecord(ItemStack stack, Category category, int originalSlot, String subType) {
    public StoredItemRecord(ItemStack stack, Category category, int originalSlot, String subType) {
        this.stack = stack.copy();
        this.category = category;
        this.originalSlot = originalSlot;
        this.subType = subType;
    }

    public static StoredItemRecord load(CompoundTag tag, HolderLookup.Provider registries) {
        ItemStack stack = ItemStack.parseOptional(registries, tag.getCompound("Item"));
        Category category = Category.fromNbt(tag.getString("Category"));
        int slot = tag.getInt("OriginalSlot");
        String subType = tag.getString("SubType");
        return new StoredItemRecord(stack, category, slot, subType);
    }

    public CompoundTag save(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        tag.put("Item", this.stack.save(registries));
        tag.putString("Category", this.category.getSerializedName());
        tag.putInt("OriginalSlot", this.originalSlot);
        tag.putString("SubType", this.subType);
        return tag;
    }

    public enum Category implements StringRepresentable {
        INVENTORY("inventory"),
        CURIOS("curios"),
        COSMETIC_ARMOR("cosmetic_armor");

        private static final Map<String, Category> BY_NAME =
                Arrays.stream(values()).collect(Collectors.toMap(Category::getSerializedName, c -> c));

        private final String serializedName;

        Category(String serializedName) {
            this.serializedName = serializedName;
        }

        static Category fromNbt(String value) {
            Category category = BY_NAME.get(value);
            if (category == null) {
                InventorySpirits.LOGGER.warn("Unknown stored-item category '{}', defaulting to INVENTORY", value);
                return INVENTORY;
            }
            return category;
        }

        @Override
        public String getSerializedName() {
            return serializedName;
        }
    }
}

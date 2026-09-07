package io.github.dogeiscut.inventory_spirits.util;

import io.github.dogeiscut.inventory_spirits.content.inventory_spirit.StoredItemRecord;
import io.github.dogeiscut.inventory_spirits.registry.ISConfig;
import lain.mods.cos.api.CosArmorAPI;
import lain.mods.cos.api.inventory.CAStacksBase;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

public final class InventoryRestoreHelper {

    private InventoryRestoreHelper() {
    }

    public static void restoreItem(Player player, StoredItemRecord record) {
        ItemStack stack = record.stack();

        switch (record.category()) {
            case INVENTORY -> {
                NonNullList<ItemStack> inv = switch (record.subType()) {
                    case "armor" -> player.getInventory().armor;
                    case "offhand" -> player.getInventory().offhand;
                    default -> player.getInventory().items;
                };
                restoreToSlot(player, SlotAccessor.of(inv), record.originalSlot(), stack);
            }
            case CURIOS -> {
                if (ModList.get().isLoaded("curios")) {
                    ICurioStacksHandler stacksHandler = CuriosApi.getCuriosInventory(player)
                            .map(handler -> handler.getCurios().get(record.subType()))
                            .orElse(null);
                    if (stacksHandler != null) {
                        restoreToSlot(player, SlotAccessor.of(stacksHandler.getStacks()), record.originalSlot(), stack);
                    } else {
                        safeGiveOrDrop(player, stack);
                    }
                } else {
                    safeGiveOrDrop(player, stack);
                }
            }
            case COSMETIC_ARMOR -> {
                if (ModList.get().isLoaded("cosmeticarmorreworked")) {
                    CAStacksBase cosArmor = CosArmorAPI.getCAStacks(player.getUUID());
                    restoreToSlot(player, SlotAccessor.of(cosArmor), record.originalSlot(), stack);
                } else {
                    safeGiveOrDrop(player, stack);
                }
            }
            default -> safeGiveOrDrop(player, stack);
        }
    }

    private static void restoreToSlot(Player player, SlotAccessor slots, int slot, ItemStack stack) {
        if (slot < 0 || slot >= slots.size()) {
            safeGiveOrDrop(player, stack);
            return;
        }

        ItemStack current = slots.get(slot);
        if (current.isEmpty()) {
            slots.set(slot, stack.copy());
            return;
        }

//        if (ISConfig.kickItemsFromOriginalSlot) {
//            ItemStack itemToKick = current.copy();
//            slots.set(slot, stack.copy());
//            safeGiveOrDrop(player, itemToKick);
//        } else {
            safeGiveOrDrop(player, stack);
//        }
    }

    public static void safeGiveOrDrop(Player player, ItemStack stack) {
        ItemStack remainder = stack.copy();

        // This has the funny side effect of just erasing items in creative.
        // But, I mean, that's how it works with picking up items normally in creative mode so...

        // Just figured that was worth noting because it initially confused me before I realised
        // it was intentional behavior.
        boolean addedAll = player.getInventory().add(remainder);

        if (!addedAll && !remainder.isEmpty()) {
            player.drop(remainder, false);
        }
    }

    private interface SlotAccessor {
        int size();

        ItemStack get(int slot);

        void set(int slot, ItemStack stack);

        static SlotAccessor of(NonNullList<ItemStack> list) {
            return new SlotAccessor() {
                public int size() {
                    return list.size();
                }

                public ItemStack get(int slot) {
                    return list.get(slot);
                }

                public void set(int slot, ItemStack stack) {
                    list.set(slot, stack);
                }
            };
        }

        static SlotAccessor of(IItemHandlerModifiable handler) {
            return new SlotAccessor() {
                public int size() {
                    return handler.getSlots();
                }

                public ItemStack get(int slot) {
                    return handler.getStackInSlot(slot);
                }

                public void set(int slot, ItemStack stack) {
                    handler.setStackInSlot(slot, stack);
                }
            };
        }
    }
}
package io.github.dogeiscut.inventory_spirits.registry;

import io.github.dogeiscut.inventory_spirits.InventorySpirits;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

// The full config option list from TODO.md's "Next Release" section is planned for a later
// update. For 1.0.0 this just covers the one behavior that was previously hardcoded in
// InventorySpiritEntity.
@EventBusSubscriber(modid = InventorySpirits.ID, bus = EventBusSubscriber.Bus.MOD)
public class ISConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.BooleanValue KICK_ITEMS_FROM_ORIGINAL_SLOT = BUILDER
            .comment(
                    "Whether restoring an item to its original inventory slot should kick out whatever is currently occupying that slot.",
                    "false (default): if the original slot is occupied, the item is placed anywhere else that fits (or dropped if the inventory is full).",
                    "true: the item always returns to its original slot, and whatever was there is placed elsewhere (or dropped) instead."
            )
            .define("kickItemsFromOriginalSlot", false);

    public static final ModConfigSpec SPEC = BUILDER.build();

    public static boolean kickItemsFromOriginalSlot;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        kickItemsFromOriginalSlot = KICK_ITEMS_FROM_ORIGINAL_SLOT.get();
    }
}

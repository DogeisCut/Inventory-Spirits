package io.github.dogeiscut.inventory_spirits.registry;

import io.github.dogeiscut.inventory_spirits.InventorySpirits;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ISSoundEvents {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(Registries.SOUND_EVENT, InventorySpirits.ID);

    // Each of these needs a matching entry in sounds.json (see the template)
    // pointing at a sounds/<name>.ogg file, plus a subtitle key in en_us.json.
    public static final DeferredHolder<SoundEvent, SoundEvent> SPIRIT_SPAWN = register("spirit_spawn");
    public static final DeferredHolder<SoundEvent, SoundEvent> SPIRIT_HIT = register("spirit_hit");
    public static final DeferredHolder<SoundEvent, SoundEvent> SPIRIT_BREAK = register("spirit_break");
    public static final DeferredHolder<SoundEvent, SoundEvent> SPIRIT_COLLECT = register("spirit_collect");
    public static final DeferredHolder<SoundEvent, SoundEvent> SPIRIT_AMBIENT = register("spirit_ambient");

    private static DeferredHolder<SoundEvent, SoundEvent> register(String name) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(InventorySpirits.ID, name);
        // createVariableRangeEvent = normal (non-fixed-range) sound, same as almost
        // everything vanilla uses SoundEvents.* for. Swap to createFixedRangeEvent(id, range)
        // only if you want a sound that doesn't fall off with distance/doesn't scale with subtitle range.
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(id));
    }

    public static void register(IEventBus modEventBus) {
        SOUND_EVENTS.register(modEventBus);
    }
}
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

    public static final DeferredHolder<SoundEvent, SoundEvent> SPIRIT_SPAWN = register("spirit_spawn");
    public static final DeferredHolder<SoundEvent, SoundEvent> SPIRIT_HIT = register("spirit_hit");
    public static final DeferredHolder<SoundEvent, SoundEvent> SPIRIT_BREAK = register("spirit_break");
    public static final DeferredHolder<SoundEvent, SoundEvent> SPIRIT_COLLECT = register("spirit_collect");
    public static final DeferredHolder<SoundEvent, SoundEvent> SPIRIT_AMBIENT = register("spirit_ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> SPIRIT_REJECT = register("spirit_reject");

    private static DeferredHolder<SoundEvent, SoundEvent> register(String name) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(InventorySpirits.ID, name);
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(id));
    }

    public static void register(IEventBus modEventBus) {
        SOUND_EVENTS.register(modEventBus);
    }
}
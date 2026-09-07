package io.github.dogeiscut.inventory_spirits.registry;

import io.github.dogeiscut.inventory_spirits.InventorySpirits;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ISParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLES = DeferredRegister.create(Registries.PARTICLE_TYPE, InventorySpirits.ID);

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> INVENTORY_SPIRIT_DUST =
            PARTICLES.register("inventory_spirit_dust", () -> new SimpleParticleType(false));

    public static void register(IEventBus modEventBus) {
        PARTICLES.register(modEventBus);
    }
}

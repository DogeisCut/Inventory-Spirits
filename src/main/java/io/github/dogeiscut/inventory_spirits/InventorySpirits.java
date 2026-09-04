package io.github.dogeiscut.inventory_spirits;

import com.mojang.logging.LogUtils;
import io.github.dogeiscut.inventory_spirits.registry.ISConfig;
import io.github.dogeiscut.inventory_spirits.registry.ISEntities;
import io.github.dogeiscut.inventory_spirits.registry.ISParticles;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;

@Mod(InventorySpirits.ID)
public class InventorySpirits {
    public static final String ID = "inventory_spirits";

    public static final Logger LOGGER = LogUtils.getLogger();
    public InventorySpirits(IEventBus modEventBus, ModContainer modContainer) {

        ISEntities.register(modEventBus);
        ISParticles.register(modEventBus);

        modContainer.registerConfig(ModConfig.Type.COMMON, ISConfig.SPEC);
    }
}

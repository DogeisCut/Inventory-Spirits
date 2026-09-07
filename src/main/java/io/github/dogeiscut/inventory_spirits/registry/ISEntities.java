package io.github.dogeiscut.inventory_spirits.registry;

import io.github.dogeiscut.inventory_spirits.InventorySpirits;
import io.github.dogeiscut.inventory_spirits.content.inventory_spirit.InventorySpiritEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ISEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(Registries.ENTITY_TYPE, InventorySpirits.ID);

    public static final Supplier<EntityType<InventorySpiritEntity>> INVENTORY_SPIRIT = ENTITY_TYPES.register(
        "inventory_spirit",
        () -> EntityType.Builder.of(
            InventorySpiritEntity::new,
            MobCategory.MISC
        )
        .sized(0.6f, 0.6f)
        .eyeHeight(0.5f)
        // TODO (Next Release): Config option
        .fireImmune()
        .clientTrackingRange(16)
        .updateInterval(3)
        .build("inventory_spirit")
    );

    public static void register(IEventBus modEventBus) {
        ENTITY_TYPES.register(modEventBus);
    }
}
package io.github.dogeiscut.inventory_spirits.compat.jade;

import io.github.dogeiscut.inventory_spirits.content.inventory_spirit.InventorySpiritEntity;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin
public class InventorySpiritsJadePlugin implements IWailaPlugin {

    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerEntityDataProvider(InventorySpiritDataProvider.INSTANCE, InventorySpiritEntity.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerEntityComponent(InventorySpiritComponentProvider.INSTANCE, InventorySpiritEntity.class);
    }
}
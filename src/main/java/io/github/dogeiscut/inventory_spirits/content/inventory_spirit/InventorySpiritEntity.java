package io.github.dogeiscut.inventory_spirits.content.inventory_spirit;

import io.github.dogeiscut.inventory_spirits.registry.ISParticles;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.ItemStackHandler;

public class InventorySpiritEntity extends Entity {

    private final ItemStackHandler inventory = new ItemStackHandler(41) {
        @Override
        protected void onContentsChanged(int slot) {
            super.onContentsChanged(slot);
        }
    };

    public InventorySpiritEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    public ItemStackHandler getInventory() {
        return this.inventory;
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (!this.level().isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.CONSUME;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {

    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compoundTag) {
        if (compoundTag.contains("Inventory")) {
            this.inventory.deserializeNBT(this.registryAccess(), compoundTag.getCompound("Inventory"));
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compoundTag) {
        compoundTag.put("Inventory", this.inventory.serializeNBT(this.registryAccess()));
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public void tick() {

        super.tick();
        if (this.level().isClientSide()) {
            if (this.tickCount % 5 == 0) {
                double offsetX = (this.random.nextDouble() - 0.5d) * 0.25d;
                double offsetY = 0.4d - (this.random.nextDouble() - 0.5d) * 0.25d;
                double offsetZ = (this.random.nextDouble() - 0.5d) * 0.25d;

                double speedX = (this.random.nextDouble() - 0.5d) * 0.05d;
                double speedY = 0.066d - ((this.random.nextDouble() - 0.5d) * 0.033);
                double speedZ = (this.random.nextDouble() - 0.5d) * 0.05d;
                this.level().addParticle(
                        ISParticles.INVENTORY_SPIRIT_DUST.get(),
                        this.getX() + offsetX,
                        this.getY() + offsetY,
                        this.getZ() + offsetZ,
                        speedX,speedY,speedZ
                );
            }
        }
    }
}

package io.github.dogeiscut.inventory_spirits.content.inventory_spirit;

import io.github.dogeiscut.inventory_spirits.registry.ISEntities;
import io.github.dogeiscut.inventory_spirits.registry.ISParticles;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class InventorySpiritEntity extends Entity {

    private final List<StoredItemRecord> storedItems = new ArrayList<>();
    private UUID owner;

    public InventorySpiritEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
        this.owner = new UUID(0, 0);
    }

    public static InventorySpiritEntity fromPlayer(Player player, boolean clearPlayer) {
        InventorySpiritEntity entity = new InventorySpiritEntity(ISEntities.INVENTORY_SPIRIT.get(), player.level());
        entity.setOwner(player.getUUID());
        for (int i = 0; i < player.getInventory().items.size(); i++) {
            ItemStack stack = player.getInventory().items.get(i);
            if (!stack.isEmpty()) {
                entity.storeItem(stack, "inventory", i, "items");
                if (clearPlayer) player.getInventory().items.set(i, ItemStack.EMPTY);
            }
        }

        for (int i = 0; i < player.getInventory().armor.size(); i++) {
            ItemStack stack = player.getInventory().armor.get(i);
            if (!stack.isEmpty()) {
                entity.storeItem(stack, "inventory", i, "armor");
                if (clearPlayer) player.getInventory().armor.set(i, ItemStack.EMPTY);
            }
        }

        for (int i = 0; i < player.getInventory().offhand.size(); i++) {
            ItemStack stack = player.getInventory().offhand.get(i);
            if (!stack.isEmpty()) {
                entity.storeItem(stack, "inventory", i, "offhand");
                if (clearPlayer) player.getInventory().offhand.set(i, ItemStack.EMPTY);
            }
        }

        CuriosApi.getCuriosInventory(player).ifPresent(handler -> {
            handler.getCurios().forEach((identifier, stacksHandler) -> {
                var stacks = stacksHandler.getStacks();
                for (int i = 0; i < stacks.getSlots(); i++) {
                    ItemStack stack = stacks.getStackInSlot(i);
                    if (!stack.isEmpty()) {
                        entity.storeItem(stack, "curios", i, identifier);
                        if (clearPlayer) stacks.setStackInSlot(i, ItemStack.EMPTY);
                    }
                }
            });
        });
        return entity;
    }

    public void drop() {
        for (StoredItemRecord storedItem : storedItems) {
            Containers.dropItemStack(this.level(), this.getX() + 0.5d, this.getY() + 0.5d, this.getZ() + 0.5d, storedItem.stack());
        }
        this.remove(RemovalReason.KILLED);
    }

    public void storeItem(ItemStack stack, String category, int originalSlot, String subType) {
        if (!stack.isEmpty()) {
            this.storedItems.add(new StoredItemRecord(stack, category, originalSlot, subType));
        }
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (!this.level().isClientSide()) {
            this.drop();
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.CONSUME;
    }

    public List<StoredItemRecord> getStoredItems() {
        return this.storedItems;
    }

    public UUID getOwner() {
        return owner;
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {

    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compoundTag) {
        this.storedItems.clear();
        if (compoundTag.contains("StoredItemList", ListTag.TAG_LIST)) {
            ListTag list = compoundTag.getList("StoredItemsList", ListTag.TAG_COMPOUND);
            HolderLookup.Provider registries = this.registryAccess();
            for (int i = 0; i < list.size(); i++) {
                CompoundTag tag = list.getCompound(i);
                this.storedItems.add(StoredItemRecord.load(tag, registries));
            }
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compoundTag) {
        ListTag list = new ListTag();
        HolderLookup.Provider registries = this.registryAccess();
        for (StoredItemRecord record : this.storedItems) {
            list.add(record.save(registries));
        }
        compoundTag.put("StoredItemsList", list);
        compoundTag.putUUID("Owner", this.owner);
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

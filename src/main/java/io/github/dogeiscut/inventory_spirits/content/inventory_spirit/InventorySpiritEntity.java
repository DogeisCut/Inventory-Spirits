package io.github.dogeiscut.inventory_spirits.content.inventory_spirit;

import io.github.dogeiscut.inventory_spirits.registry.ISEntities;
import io.github.dogeiscut.inventory_spirits.registry.ISParticles;
import lain.mods.cos.api.CosArmorAPI;
import lain.mods.cos.api.inventory.CAStacksBase;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class InventorySpiritEntity extends Entity {

    private final List<StoredItemRecord> storedItems = new ArrayList<>();
    private int totalExperience;
    private UUID owner;

    public InventorySpiritEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
        this.totalExperience = 0;
        this.owner = new UUID(0, 0);
    }

    public static InventorySpiritEntity fromPlayer(Player player, boolean clearPlayer) {
        InventorySpiritEntity entity = new InventorySpiritEntity(ISEntities.INVENTORY_SPIRIT.get(), player.level());

        entity.setOwner(player.getUUID());

        entity.setTotalExperience(player.totalExperience);
        if (clearPlayer) player.totalExperience = 0;

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

        if (ModList.get().isLoaded("cosmeticarmorreworked")) {
            CAStacksBase cosArmor = CosArmorAPI.getCAStacks(player.getUUID());
            for (int i = 0; i < cosArmor.getSlots(); i++) {
                ItemStack stack = cosArmor.getStackInSlot(i);
                if (!stack.isEmpty()) {
                    entity.storeItem(stack, "cosmetic_armor", i, "armor");
                    if (clearPlayer) cosArmor.setStackInSlot(i, ItemStack.EMPTY);
                }
            }
        }

        return entity;
    }

    public void drop() {
        for (StoredItemRecord storedItem : storedItems) {
            this.spawnAtLocation(storedItem.stack(), 0.4f);
        }
        if (this.totalExperience > 0 && this.level() instanceof ServerLevel serverLevel) {
            ExperienceOrb.award(serverLevel, this.position(), this.totalExperience);
            this.totalExperience = 0;
        }
        this.remove(RemovalReason.KILLED);
    }

    public void collect(Player player) {
        if (this.level().isClientSide()) return;

        for (StoredItemRecord record : storedItems) {
            restoreItem(player, record);
        }
        storedItems.clear();

        if (this.totalExperience > 0) {
            player.giveExperiencePoints(this.totalExperience);
            this.totalExperience = 0;
        }

        this.discard();
    }

    // TODO: wire this to a config option once ISConfig is implemented.
    // false = fall back to a normal inventory placement when the original slot is occupied.
    // true = always force the item back into its original slot, kicking whatever's currently there onto the ground/inventory.
    private static final boolean KICK_ITEMS_FROM_ORIGINAL_SLOT = false;

    private void restoreItem(Player player, StoredItemRecord record) {
        ItemStack stack = record.stack();

        switch (record.category()) {
            case "inventory" -> {
                NonNullList<ItemStack> inv = switch (record.subType()) {
                    case "armor" -> player.getInventory().armor;
                    case "offhand" -> player.getInventory().offhand;
                    default -> player.getInventory().items;
                };
                restoreToVanillaSlot(player, inv, record.originalSlot(), stack);
            }
            case "curios" -> {
                ICurioStacksHandler stacksHandler = CuriosApi.getCuriosInventory(player)
                        .map(handler -> handler.getCurios().get(record.subType()))
                        .orElse(null);
                if (stacksHandler != null) {
                    restoreToHandlerSlot(player, stacksHandler.getStacks(), record.originalSlot(), stack);
                } else {
                    ItemStack remainder = stack.copy();
                    player.getInventory().add(remainder);
                    player.drop(remainder, true);
                }
            }
            case "cosmetic_armor" -> {
                if (ModList.get().isLoaded("cosmeticarmorreworked")) {
                    restoreToHandlerSlot(player, CosArmorAPI.getCAStacks(player.getUUID()), record.originalSlot(), stack);
                } else {
                    ItemStack remainder = stack.copy();
                    player.getInventory().add(remainder);
                    player.drop(remainder, true);
                }
            }
        }
    }

    private void restoreToVanillaSlot(Player player, NonNullList<ItemStack> inv, int slot, ItemStack stack) {
        if (slot < 0 || slot >= inv.size()) {
            player.drop(stack, true);
            return;
        }

        ItemStack current = inv.get(slot);
        if (current.isEmpty()) {
            inv.set(slot, stack.copy());
            return;
        }

        if (KICK_ITEMS_FROM_ORIGINAL_SLOT) {
            ItemStack remainder = current.copy();
            player.getInventory().add(remainder);
            player.drop(remainder, true);

            inv.set(slot, stack.copy());
            return;
        }

        ItemStack remainder = stack.copy();
        player.getInventory().add(remainder);
        player.drop(remainder, true);
    }

    private void restoreToHandlerSlot(Player player, IItemHandlerModifiable handler, int slot, ItemStack stack) {
        if (slot < 0 || slot >= handler.getSlots()) {
            player.drop(stack, true);
            return;
        }

        ItemStack current = handler.getStackInSlot(slot);
        if (current.isEmpty()) {
            handler.setStackInSlot(slot, stack.copy());
            return;
        }

        if (KICK_ITEMS_FROM_ORIGINAL_SLOT) {
            ItemStack remainder = current.copy();
            player.getInventory().add(remainder);
            player.drop(remainder, true);

            handler.setStackInSlot(slot, stack.copy());
            return;
        }

        ItemStack remainder = stack.copy();
        player.getInventory().add(remainder);
        player.drop(remainder, true);
    }

    public void storeItem(ItemStack stack, String category, int originalSlot, String subType) {
        if (!stack.isEmpty()) {
            this.storedItems.add(new StoredItemRecord(stack, category, originalSlot, subType));
        }
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (!this.level().isClientSide()) {
            this.collect(player);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public boolean skipAttackInteraction(Entity entity) {
        this.drop();
        return false;
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

    public int getTotalExperience() {
        return totalExperience;
    }

    public void setTotalExperience(int totalExperience) {
        this.totalExperience = totalExperience;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {

    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compoundTag) {
        this.storedItems.clear();
        if (compoundTag.contains("StoredItemsList", ListTag.TAG_LIST)) {
            ListTag list = compoundTag.getList("StoredItemsList", ListTag.TAG_COMPOUND);
            HolderLookup.Provider registries = this.registryAccess();
            for (int i = 0; i < list.size(); i++) {
                CompoundTag tag = list.getCompound(i);
                this.storedItems.add(StoredItemRecord.load(tag, registries));
            }
        }
        if (compoundTag.contains("Owner")) this.owner = compoundTag.getUUID("Owner");
        if (compoundTag.contains("TotalExperience")) this.totalExperience = compoundTag.getInt("TotalExperience");
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
        compoundTag.putInt("TotalExperience", this.totalExperience);
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

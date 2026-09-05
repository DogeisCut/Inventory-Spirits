package io.github.dogeiscut.inventory_spirits.content.inventory_spirit;

import io.github.dogeiscut.inventory_spirits.registry.ISConfig;
import io.github.dogeiscut.inventory_spirits.registry.ISEntities;
import io.github.dogeiscut.inventory_spirits.registry.ISParticles;
import lain.mods.cos.api.CosArmorAPI;
import lain.mods.cos.api.inventory.CAStacksBase;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class InventorySpiritEntity extends Entity {

    // TODO (Next Release): Config option
    private static final float MAX_HEALTH = 3.0f;
    // TODO (Next Release): Config option
    private static final double FLOAT_ACCELERATION = 0.005d;
    // TODO (Next Release): Config option
    private static final double MAX_FLOAT_SPEED = 3.0d;
    // TODO (Next Release): Config option
    private static final double FRICTION = 0.70d;
    // TODO (Next Release): Config option
    private static final int VERTICAL_SAFETY_MARGIN = 1;
    // TODO (Next Release): Config option
    private static final double ELASTICITY = 0.7d;

    private final List<StoredItemRecord> storedItems = new ArrayList<>();
    private int totalExperience;
    private UUID owner;

    private float health = MAX_HEALTH;
    private boolean spawnEffectsPlayed = false;

    private int lerpSteps = 0;
    private double lerpX, lerpY, lerpZ;
    private float lerpYRot, lerpXRot;

    public InventorySpiritEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
        this.totalExperience = 0;
        this.owner = new UUID(0, 0);
    }

    public static InventorySpiritEntity fromPlayer(Player player, boolean clearPlayer) {
        InventorySpiritEntity entity = new InventorySpiritEntity(ISEntities.INVENTORY_SPIRIT.get(), player.level());

        entity.setOwner(player.getUUID());

        int bankedExperience = getPlayerExperiencePoints(player);
        entity.setTotalExperience(bankedExperience);
        if (clearPlayer) {
            player.totalExperience = 0;
            player.experienceLevel = 0;
            player.experienceProgress = 0.0f;
        }

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

        if (ModList.get().isLoaded("curios")) {
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
        }

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

    // I may or may not have borrowed this from somewhere
    public static int getExperienceForLevel(int level) {
        if (level == 0)
            return 0;
        if (level >= 31)
            return (9 * level * level - 325 * level) / 2 + 2220;
        if (level >= 16)
            return (5 * level * level - 81 * level) / 2 + 360;
        return level * level + 6 * level;
    }

    private static int getPlayerExperiencePoints(Player player) {
        int pointsForCurrentLevel = getExperienceForLevel(player.experienceLevel);
        int pointsIntoCurrentLevel = Math.round(player.experienceProgress * player.getXpNeededForNextLevel());
        return pointsForCurrentLevel + pointsIntoCurrentLevel;
    }

    public void drop() {
        for (StoredItemRecord storedItem : storedItems) {
            this.spawnAtLocation(storedItem.stack().copy(), 0.4f);
        }
        if (this.totalExperience > 0 && this.level() instanceof ServerLevel serverLevel) {
            ExperienceOrb.award(serverLevel, this.position(), this.totalExperience);
            this.totalExperience = 0;
        }
        playDestroyEffects();
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

        playCollectEffects();
        this.discard();
    }

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
                if (ModList.get().isLoaded("curios")) {
                    ICurioStacksHandler stacksHandler = CuriosApi.getCuriosInventory(player)
                            .map(handler -> handler.getCurios().get(record.subType()))
                            .orElse(null);
                    if (stacksHandler != null) {
                        restoreToHandlerSlot(player, stacksHandler.getStacks(), record.originalSlot(), stack);
                    } else {
                        safeGiveOrDrop(player, stack);
                    }
                } else {
                    safeGiveOrDrop(player, stack);
                }
            }
            case "cosmetic_armor" -> {
                if (ModList.get().isLoaded("cosmeticarmorreworked")) {
                    restoreToHandlerSlot(player, CosArmorAPI.getCAStacks(player.getUUID()), record.originalSlot(), stack);
                } else {
                    safeGiveOrDrop(player, stack);
                }
            }
            default -> safeGiveOrDrop(player, stack);
        }
    }

    private void restoreToVanillaSlot(Player player, NonNullList<ItemStack> inv, int slot, ItemStack stack) {
        if (slot < 0 || slot >= inv.size()) {
            safeGiveOrDrop(player, stack);
            return;
        }

        ItemStack current = inv.get(slot);
        if (current.isEmpty()) {
            inv.set(slot, stack.copy());
            return;
        }

        if (ISConfig.kickItemsFromOriginalSlot) {
            ItemStack itemToKick = current.copy();
            inv.set(slot, stack.copy());
            safeGiveOrDrop(player, itemToKick);
        } else {
            safeGiveOrDrop(player, stack);
        }
    }

    private void restoreToHandlerSlot(Player player, IItemHandlerModifiable handler, int slot, ItemStack stack) {
        if (slot < 0 || slot >= handler.getSlots()) {
            safeGiveOrDrop(player, stack);
            return;
        }

        ItemStack current = handler.getStackInSlot(slot);
        if (current.isEmpty()) {
            handler.setStackInSlot(slot, stack.copy());
            return;
        }

        if (ISConfig.kickItemsFromOriginalSlot) {
            ItemStack itemToKick = current.copy();
            handler.setStackInSlot(slot, stack.copy());
            safeGiveOrDrop(player, itemToKick);
        } else {
            safeGiveOrDrop(player, stack);
        }
    }

    private void safeGiveOrDrop(Player player, ItemStack stack) {
        ItemStack remainder = stack.copy();

        // This has the funny side effect of just erasing items in creative.
        // But, I mean, that's how it works with picking up items normally in creative mode so...

        // Just figured that was worth noting because it initially confused me before I realised
        // it was intentional behavior.
        boolean addedAll = player.getInventory().add(remainder);

        if (!addedAll && !remainder.isEmpty()) {
            player.drop(remainder, false);
        }
    }

    public void storeItem(ItemStack stack, String category, int originalSlot, String subType) {
        if (!stack.isEmpty()) {
            this.storedItems.add(new StoredItemRecord(stack.copy(), category, originalSlot, subType));
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
    public boolean isPickable() {
        return true;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.level().isClientSide() || this.isRemoved() || this.isInvulnerableTo(source)) {
            return false;
        }

        this.markHurt();
        this.health -= amount;

        if (this.health <= 0.0f) {
            this.drop();
        }

        return true;
    }

    @Override
    public boolean isInvulnerableTo(DamageSource damageSource) {
        // TODO (Next Release): Config option
        return !damageSource.is(DamageTypes.PLAYER_ATTACK);
    }

    @Override
    public void lerpTo(double x, double y, double z, float yRot, float xRot, int steps) {
        this.lerpX = x;
        this.lerpY = y;
        this.lerpZ = z;
        this.lerpYRot = yRot;
        this.lerpXRot = xRot;
        this.lerpSteps = steps;
    }

    public List<StoredItemRecord> getStoredItems() {
        return Collections.unmodifiableList(this.storedItems);
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
        if (compoundTag.contains("Health")) this.health = compoundTag.getFloat("Health");
        if (compoundTag.contains("SpawnEffectsPlayed")) this.spawnEffectsPlayed = compoundTag.getBoolean("SpawnEffectsPlayed");
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
        compoundTag.putFloat("Health", this.health);
        compoundTag.putBoolean("SpawnEffectsPlayed", this.spawnEffectsPlayed);
    }

    @Override
    public void tick() {
        super.tick();

        this.setOldPosAndRot();

        if (this.level().isClientSide()) {
            if (this.lerpSteps > 0) {
                double nextX = Mth.lerp(1.0d / this.lerpSteps, this.getX(), this.lerpX);
                double nextY = Mth.lerp(1.0d / this.lerpSteps, this.getY(), this.lerpY);
                double nextZ = Mth.lerp(1.0d / this.lerpSteps, this.getZ(), this.lerpZ);
                float nextYRot = (float) Mth.lerp(1.0d / this.lerpSteps, (double) this.getYRot(), (double) this.lerpYRot);
                float nextXRot = (float) Mth.lerp(1.0d / this.lerpSteps, (double) this.getXRot(), (double) this.lerpXRot);
                this.lerpSteps--;
                this.setPos(nextX, nextY, nextZ);
                this.setRot(nextYRot, nextXRot);
            }

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
                        speedX, speedY, speedZ
                );
            }
            return;
        }

        if (!this.spawnEffectsPlayed) {
            this.spawnEffectsPlayed = true;
            playSpawnEffects();
        }

        applyFloatPhysics();
        playAmbientSounds();
    }

    private void applyFloatPhysics() {
        if (!(this.level() instanceof ServerLevel serverLevel)) return;

        Vec3 motion = this.getDeltaMovement();
        double dx = motion.x;
        double dy = motion.y;
        double dz = motion.z;

//        if (this.horizontalCollision) {
//            double
//        }

        int minY = serverLevel.getMinBuildHeight();
        int maxY = serverLevel.getMaxBuildHeight();
        double y = this.getY();

        // TODO (Bug): take into account actual lava level.
        boolean inLava = this.level().getFluidState(this.blockPosition()).is(FluidTags.LAVA);

        double lowerBound = minY + VERTICAL_SAFETY_MARGIN;
        double upperBound = maxY - VERTICAL_SAFETY_MARGIN;

        if (y < lowerBound) {
            dy += FLOAT_ACCELERATION * (1.0d + (lowerBound - y) * 0.05d);
        } else if (inLava) {
            dy += FLOAT_ACCELERATION;
        } else if (y > upperBound) {
            dy -= FLOAT_ACCELERATION * (1.0d + (y - upperBound) * 0.05d);
        } else {
            dy *= FRICTION;
        }

        dx *= FRICTION;
        dz *= FRICTION;

        dx = Mth.clamp(dx, -MAX_FLOAT_SPEED, MAX_FLOAT_SPEED);
        dy = Mth.clamp(dy, -MAX_FLOAT_SPEED, MAX_FLOAT_SPEED);
        dz = Mth.clamp(dz, -MAX_FLOAT_SPEED, MAX_FLOAT_SPEED);

        this.setDeltaMovement(dx, dy, dz);
        this.move(MoverType.SELF, this.getDeltaMovement());
    }

    @Override
    protected void onBelowWorld() {
        // TODO (Next Release): Config option
    }

    private void playSpawnEffects() {
        if (!(this.level() instanceof ServerLevel serverLevel)) return;
        serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.AMETHYST_BLOCK_BREAK, SoundSource.NEUTRAL, 0.25f, 1.6f);
        serverLevel.sendParticles(ParticleTypes.POOF,
                this.getX(), this.getY() + this.getBbHeight() * 0.5d, this.getZ(),
                12, 0.25d, 0.25d, 0.25d, 0.01d);
    }

    private void playAmbientSounds() {
        if (!(this.level() instanceof ServerLevel serverLevel)) return;
        if (this.tickCount % 100 == 0 && this.random.nextFloat() < 0.35f) {
            serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.NEUTRAL, 0.35f, 1.0f);
        }
    }

    private void playCollectEffects() {
        if (!(this.level() instanceof ServerLevel serverLevel)) return;
        serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.BEEHIVE_ENTER, SoundSource.PLAYERS, 0.8f, 2.1f);
        serverLevel.sendParticles(ISParticles.INVENTORY_SPIRIT_DUST.get(),
                this.getX(), this.getY() + this.getBbHeight() * 0.5d, this.getZ(),
                16, 0.3d, 0.3d, 0.3d, 0.05d);
    }

    private void playDestroyEffects() {
        if (!(this.level() instanceof ServerLevel serverLevel)) return;
        serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.GLASS_BREAK, SoundSource.NEUTRAL, 0.7f, 0.9f);
        serverLevel.sendParticles(ParticleTypes.POOF,
                this.getX(), this.getY() + this.getBbHeight() * 0.5d, this.getZ(),
                14, 0.3d, 0.3d, 0.3d, 0.03d);
    }
}

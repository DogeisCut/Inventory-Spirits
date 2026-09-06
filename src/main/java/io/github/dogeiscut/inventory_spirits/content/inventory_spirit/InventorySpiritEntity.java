package io.github.dogeiscut.inventory_spirits.content.inventory_spirit;

import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.mixinterface.entity.entities_stick_sublevels.EntityStickExtension;
import dev.ryanhcode.sable.sublevel.SubLevel;
import io.github.dogeiscut.inventory_spirits.registry.ISEntities;
import io.github.dogeiscut.inventory_spirits.registry.ISParticles;
import io.github.dogeiscut.inventory_spirits.registry.ISSoundEvents;
import io.github.dogeiscut.inventory_spirits.util.ExperienceHelper;
import io.github.dogeiscut.inventory_spirits.util.InventoryRestoreHelper;
import lain.mods.cos.api.CosArmorAPI;
import lain.mods.cos.api.inventory.CAStacksBase;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.ModList;
import org.jetbrains.annotations.Nullable;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.*;

public class InventorySpiritEntity extends Entity {

    // TODO (Next Release): Config option
    private static final float MAX_DAMAGE = 3.0f;
    private static final float DAMAGE_DECAY_PER_TICK = MAX_DAMAGE / 40.0f;
    // TODO (Next Release): Config option
    private static final double FLOAT_ACCELERATION = 0.005d;
    // TODO (Next Release): Config option
    private static final double MAX_FLOAT_SPEED = 3.0d;
    // TODO (Next Release): Config option
    private static final double FRICTION = 0.70d;
    // TODO (Next Release): Config option
    private static final int VERTICAL_SAFETY_MARGIN = 1;
    // TODO (Next Release): Config option
    private static final boolean ALLOW_STEALING = false;

    private static final EntityDataAccessor<Optional<UUID>> DATA_OWNER_UUID =
            SynchedEntityData.defineId(InventorySpiritEntity.class, EntityDataSerializers.OPTIONAL_UUID);

    private final List<StoredItemRecord> storedItems = new ArrayList<>();
    private int totalExperience;

    private float accumulatedDamage = 0.0f;
    private boolean spawnEffectsPlayed = false;

    private int lerpSteps = 0;
    private double lerpX, lerpY, lerpZ;
    private float lerpYRot, lerpXRot;

    public InventorySpiritEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
        this.totalExperience = 0;
    }

    @Nullable
    public static InventorySpiritEntity fromPlayer(Player player, boolean clearPlayer) {
        InventorySpiritEntity entity = new InventorySpiritEntity(ISEntities.INVENTORY_SPIRIT.get(), player.level());

        entity.setOwner(player.getUUID());

        int bankedExperience = ExperienceHelper.getPlayerExperiencePoints(player);
        entity.setTotalExperience(bankedExperience);
        if (clearPlayer) {
            player.totalExperience = 0;
            player.experienceLevel = 0;
            player.experienceProgress = 0.0f;
        }

        for (int i = 0; i < player.getInventory().items.size(); i++) {
            ItemStack stack = player.getInventory().items.get(i);
            if (!stack.isEmpty()) {
                entity.storeItem(stack, StoredItemRecord.Category.INVENTORY, i, "items");
                if (clearPlayer) player.getInventory().items.set(i, ItemStack.EMPTY);
            }
        }

        for (int i = 0; i < player.getInventory().armor.size(); i++) {
            ItemStack stack = player.getInventory().armor.get(i);
            if (!stack.isEmpty()) {
                entity.storeItem(stack, StoredItemRecord.Category.INVENTORY, i, "armor");
                if (clearPlayer) player.getInventory().armor.set(i, ItemStack.EMPTY);
            }
        }

        for (int i = 0; i < player.getInventory().offhand.size(); i++) {
            ItemStack stack = player.getInventory().offhand.get(i);
            if (!stack.isEmpty()) {
                entity.storeItem(stack, StoredItemRecord.Category.INVENTORY, i, "offhand");
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
                            entity.storeItem(stack, StoredItemRecord.Category.CURIOS, i, identifier);
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
                    entity.storeItem(stack, StoredItemRecord.Category.COSMETIC_ARMOR, i, "armor");
                    if (clearPlayer) cosArmor.setStackInSlot(i, ItemStack.EMPTY);
                }
            }
        }

        if (entity.getTotalExperience() == 0 && entity.getStoredItems().isEmpty()) {
            return null;
        }

        return entity;
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
            InventoryRestoreHelper.restoreItem(player, record);
        }
        storedItems.clear();

        if (this.totalExperience > 0) {
            player.giveExperiencePoints(this.totalExperience);
            this.totalExperience = 0;
        }

        player.displayClientMessage(Component.translatable("entity.inventory_spirits.inventory_spirit.collected"), false);
        playCollectEffects();
        this.discard();
    }

    public void storeItem(ItemStack stack, StoredItemRecord.Category category, int originalSlot, String subType) {
        if (!stack.isEmpty()) {
            this.storedItems.add(new StoredItemRecord(stack.copy(), category, originalSlot, subType));
        }
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (!this.level().isClientSide()) {
            if (ALLOW_STEALING || player.getUUID().equals(getOwner()) || getOwner() == null) {
                if (player.isShiftKeyDown()) {
                    this.collect(player);
                } else {
                    player.displayClientMessage(Component.translatable("entity.inventory_spirits.inventory_spirit.hint"), true);
                    ClientboundSoundPacket packet = new ClientboundSoundPacket(
                            ISSoundEvents.SPIRIT_REJECT,
                            SoundSource.NEUTRAL,
                            player.getX(),
                            player.getY(),
                            player.getZ(),
                            1.0f,
                            1.0f,
                            player.getRandom().nextLong()
                    );
                    ((ServerPlayer) player).connection.send(packet);
                }
            } else {
                player.displayClientMessage(Component.translatable("entity.inventory_spirits.inventory_spirit.steal"), true);
                ClientboundSoundPacket packet = new ClientboundSoundPacket(
                        ISSoundEvents.SPIRIT_REJECT,
                        SoundSource.NEUTRAL,
                        player.getX(),
                        player.getY(),
                        player.getZ(),
                        1.0f,
                        1.0f,
                        player.getRandom().nextLong()
                );
                ((ServerPlayer) player).connection.send(packet);
            }
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
        // TODO (Next Release): Additional config for handling stealing with breaking
//        if (ALLOW_STEALING || source.getEntity().getUUID().equals(getOwner()) || getOwner() == null) {
//
//        }

        this.markHurt();
        this.accumulatedDamage += amount;

        if (this.accumulatedDamage > MAX_DAMAGE) {
            this.drop();
        } else if (this.level() instanceof ServerLevel serverLevel) {
            float randomPitch = 0.8F + random.nextFloat() * 0.4F;
            serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                    ISSoundEvents.SPIRIT_HIT.get(), SoundSource.NEUTRAL, 1.0f, randomPitch);
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

    @Nullable
    public UUID getOwner() {
        return this.entityData.get(DATA_OWNER_UUID).orElse(null);
    }

    public void setOwner(@Nullable UUID owner) {
        this.entityData.set(DATA_OWNER_UUID, Optional.ofNullable(owner));
    }

    public int getTotalExperience() {
        return totalExperience;
    }

    public void setTotalExperience(int totalExperience) {
        this.totalExperience = totalExperience;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_OWNER_UUID, Optional.empty());
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
        if (compoundTag.hasUUID("Owner")) this.setOwner(compoundTag.getUUID("Owner"));
        if (compoundTag.contains("TotalExperience")) this.totalExperience = compoundTag.getInt("TotalExperience");
        if (compoundTag.contains("AccumulatedDamage")) this.accumulatedDamage = compoundTag.getFloat("AccumulatedDamage");
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
        UUID owner = this.getOwner();
        if (owner != null) compoundTag.putUUID("Owner", owner);
        compoundTag.putInt("TotalExperience", this.totalExperience);
        compoundTag.putFloat("AccumulatedDamage", this.accumulatedDamage);
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

        if (this.accumulatedDamage > 0.0f) {
            this.accumulatedDamage = Math.max(0.0f, this.accumulatedDamage - DAMAGE_DECAY_PER_TICK);
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

        // my horrid attempt at fixing this thing's lerping when interacting with sub-levels.
        // it doesn't work.
        if (ModList.get().isLoaded("sable")) {
            SubLevel trackingSubLevel = Sable.HELPER.getTrackingSubLevel(this);
            if (trackingSubLevel != null) {
                EntityStickExtension stickExtension = (EntityStickExtension) this;
                Vec3 plotPosition = stickExtension.sable$getPlotPosition();

                if (plotPosition != null) {
                    dx *= FRICTION;
                    dy *= FRICTION;
                    dz *= FRICTION;

                    dx = Mth.clamp(dx, -MAX_FLOAT_SPEED, MAX_FLOAT_SPEED);
                    dy = Mth.clamp(dy, -MAX_FLOAT_SPEED, MAX_FLOAT_SPEED);
                    dz = Mth.clamp(dz, -MAX_FLOAT_SPEED, MAX_FLOAT_SPEED);

                    this.setDeltaMovement(dx, dy, dz);
                    stickExtension.sable$setPlotPosition(plotPosition.add(dx, dy, dz));
                    return;
                }
            }
        }

        int minY = serverLevel.getMinBuildHeight();
        int maxY = serverLevel.getMaxBuildHeight();
        double y = this.getY();

        BlockPos pos = this.blockPosition();
        FluidState fluidState = this.level().getFluidState(pos);
        boolean inLava = fluidState.is(FluidTags.LAVA);
        double lavaSurfaceY = pos.getY() + fluidState.getHeight(this.level(), pos);

        double lowerBound = minY + VERTICAL_SAFETY_MARGIN;
        double upperBound = maxY - VERTICAL_SAFETY_MARGIN;

        if (y < lowerBound) {
            dy += FLOAT_ACCELERATION * (1.0d + (lowerBound - y) * 0.05d);
        } else if (inLava) {
            double depthBelowSurface = Math.max(lavaSurfaceY - y, 0.0d);
            dy += FLOAT_ACCELERATION * (1.0d + depthBelowSurface * 0.05d);
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
        float randomPitch = 0.8F + random.nextFloat() * 0.4F;
        playSound(ISSoundEvents.SPIRIT_SPAWN.get(), 1.0f, randomPitch);
        serverLevel.sendParticles(ParticleTypes.POOF,
                this.getX(), this.getY() + this.getBbHeight() * 0.5d, this.getZ(),
                12, 0.25d, 0.25d, 0.25d, 0.01d);
    }

    private void playAmbientSounds() {
        if (!(this.level() instanceof ServerLevel serverLevel)) return;
        float randomPitch = 0.8F + random.nextFloat() * 0.4F;
        if (this.tickCount % 33 == 0 && this.random.nextFloat() < 0.35f) {
            playSound(ISSoundEvents.SPIRIT_AMBIENT.get(), 1.0f, randomPitch);
        }
    }
    private void playCollectEffects() {
        if (!(this.level() instanceof ServerLevel serverLevel)) return;
        float randomPitch = 0.8F + random.nextFloat() * 0.4F;
        serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                ISSoundEvents.SPIRIT_COLLECT.get(), SoundSource.PLAYERS, 1.0f, randomPitch);
        serverLevel.sendParticles(ISParticles.INVENTORY_SPIRIT_DUST.get(),
                this.getX(), this.getY() + this.getBbHeight() * 0.5d, this.getZ(),
                16, 0.3d, 0.3d, 0.3d, 0.05d);
    }

    private void playDestroyEffects() {
        if (!(this.level() instanceof ServerLevel serverLevel)) return;
        float randomPitch = 0.8F + random.nextFloat() * 0.4F;
        serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                ISSoundEvents.SPIRIT_BREAK.get(), SoundSource.NEUTRAL, 1.0f, randomPitch);
        serverLevel.sendParticles(ParticleTypes.POOF,
                this.getX(), this.getY() + this.getBbHeight() * 0.5d, this.getZ(),
                14, 0.3d, 0.3d, 0.3d, 0.03d);
    }
}
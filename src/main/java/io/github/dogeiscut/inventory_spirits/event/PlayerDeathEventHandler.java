package io.github.dogeiscut.inventory_spirits.event;

import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.mixinterface.entity.entities_stick_sublevels.EntityStickExtension;
import dev.ryanhcode.sable.sublevel.SubLevel;
import io.github.dogeiscut.inventory_spirits.InventorySpirits;
import io.github.dogeiscut.inventory_spirits.content.inventory_spirit.InventorySpiritEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingExperienceDropEvent;

@EventBusSubscriber(modid = InventorySpirits.ID, bus = EventBusSubscriber.Bus.GAME)
public class PlayerDeathEventHandler {


    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof Player player) || player.level().isClientSide()) {
            return;
        }

        if (player.level().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY)) {
            return;
        }

        Level level = player.level();

        InventorySpiritEntity entity = InventorySpiritEntity.fromPlayer(player, true);
        if (entity == null) {
            return;
        }

        Vec3 deathPos = new Vec3(player.getX(), player.getY() + 0.25d, player.getZ());

        if (ModList.get().isLoaded("sable")) {
            SubLevel subLevel = Sable.HELPER.getTrackingSubLevel(player);

            if (subLevel != null) {
                Vec3 plotLocalPos = subLevel.logicalPose().transformPositionInverse(deathPos);

                ((EntityStickExtension) entity).sable$setPlotPosition(plotLocalPos);

                Vec3 worldPos = subLevel.logicalPose().transformPosition(plotLocalPos);
                entity.moveTo(worldPos.x, worldPos.y, worldPos.z, player.getYRot(), player.getXRot());
                level.addFreshEntity(entity);
                return;
            }
        }

        entity.moveTo(deathPos.x, deathPos.y, deathPos.z, player.getYRot(), player.getXRot());
        level.addFreshEntity(entity);
    }
}
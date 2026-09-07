package io.github.dogeiscut.inventory_spirits.compat.jade;

import io.github.dogeiscut.inventory_spirits.InventorySpirits;
import io.github.dogeiscut.inventory_spirits.content.inventory_spirit.InventorySpiritEntity;
import io.github.dogeiscut.inventory_spirits.util.ExperienceHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec2;
import org.jetbrains.annotations.Nullable;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.JadeIds;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.theme.IThemeHelper;
import snownee.jade.api.ui.IElement;
import snownee.jade.api.ui.IElementHelper;
import snownee.jade.util.CommonProxy;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class InventorySpiritComponentProvider implements IEntityComponentProvider {
    public static final InventorySpiritComponentProvider INSTANCE = new InventorySpiritComponentProvider();
    public static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(InventorySpirits.ID, "spirit_contents");

    private static final ResourceLocation XP_BAR_BACKGROUND = ResourceLocation.fromNamespaceAndPath(InventorySpirits.ID, "experience_bar_mini_background");
    private static final ResourceLocation XP_BAR_PROGRESS = ResourceLocation.fromNamespaceAndPath(InventorySpirits.ID, "experience_bar_mini_progress");
    private static final int XP_BAR_WIDTH = 91;
    private static final int XP_BAR_HEIGHT = 5;

    private static final int XP_TEXT_COLOR = 0x7EFC20;

    private static final int ITEM_ICONS_PER_ROW = 9;
    private static final float ITEM_ICON_SCALE = 0.7f;

    private InventorySpiritComponentProvider() {
    }

    @Override
    public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
        Optional<InventorySpiritDataProvider.Data> maybeData =
                InventorySpiritDataProvider.INSTANCE.decodeFromData(accessor);
        if (maybeData.isEmpty()) return;
        InventorySpiritDataProvider.Data data = maybeData.get();

        if (accessor.getEntity() instanceof InventorySpiritEntity spirit) {
            appendOwner(tooltip, spirit.getOwner());
        }
        appendExperience(tooltip, data.totalExperience());
        appendItems(tooltip, data.items());
    }

    private void appendOwner(ITooltip tooltip, @Nullable UUID owner) {
        if (owner == null) return;

        String name = CommonProxy.getLastKnownUsername(owner);
        if (name == null) {
            name = "???";
        }

        tooltip.replace(JadeIds.CORE_OBJECT_NAME, IThemeHelper.get().title(Component.translatable("jade.inventory_spirits.inventory_spirit_named", name)));
    }

    private void appendExperience(ITooltip tooltip, int totalExperience) {
        if (totalExperience <= 0) return;

        int level = ExperienceHelper.getLevelForExperience(totalExperience);
        float progress = ExperienceHelper.getProgressForExperience(totalExperience);

        int xpAtLevelStart = ExperienceHelper.getExperienceForLevel(level);
        int xpForThisLevel = ExperienceHelper.getExperienceForLevel(level + 1) - xpAtLevelStart;
        int pointsIntoLevel = totalExperience - xpAtLevelStart;

        IElementHelper elements = IElementHelper.get();

        Component levelText = Component.translatable("jade.inventory_spirits.experience", level);
        OutlinedTextElement outlinedTextElement = new OutlinedTextElement(levelText, XP_TEXT_COLOR);
        tooltip.add(outlinedTextElement);
        outlinedTextElement.message(level + " levels");

        IElement bar = elements.progress(progress, XP_BAR_BACKGROUND, XP_BAR_PROGRESS, XP_BAR_WIDTH, XP_BAR_HEIGHT, true).translate(new Vec2(0, -1));
        bar.message(pointsIntoLevel + " out of " + xpForThisLevel + " points");
        tooltip.append(bar);
    }

    private void appendItems(ITooltip tooltip, List<ItemStack> items) {
        if (items.isEmpty()) return;

        IElementHelper elements = IElementHelper.get();
        for (int i = 0; i < items.size(); i += ITEM_ICONS_PER_ROW) {
            List<ItemStack> row = items.subList(i, Math.min(i + ITEM_ICONS_PER_ROW, items.size()));
            IElement first = elements.item(row.get(0), ITEM_ICON_SCALE);
            tooltip.add(first);
            for (int j = 1; j < row.size(); j++) {
                tooltip.append(elements.item(row.get(j), ITEM_ICON_SCALE));
            }
        }
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }
}
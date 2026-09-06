package io.github.dogeiscut.inventory_spirits.util;

import net.minecraft.world.entity.player.Player;

public final class ExperienceHelper {

    private ExperienceHelper() {
    }

    // Borrowed this from Create: Enchantment Industries
    public static int getExperienceForLevel(int level) {
        if (level == 0)
            return 0;
        if (level >= 31)
            return (9 * level * level - 325 * level) / 2 + 2220;
        if (level >= 16)
            return (5 * level * level - 81 * level) / 2 + 360;
        return level * level + 6 * level;
    }

    public static int getPlayerExperiencePoints(Player player) {
        int pointsForCurrentLevel = getExperienceForLevel(player.experienceLevel);
        int pointsIntoCurrentLevel = Math.round(player.experienceProgress * player.getXpNeededForNextLevel());
        return pointsForCurrentLevel + pointsIntoCurrentLevel;
    }

    public static int getLevelForExperience(int totalExperience) {
        int level = 0;
        while (getExperienceForLevel(level + 1) <= totalExperience) {
            level++;
        }
        return level;
    }

    public static float getProgressForExperience(int totalExperience) {
        int level = getLevelForExperience(totalExperience);
        int xpAtLevelStart = getExperienceForLevel(level);
        int xpForThisLevel = getExperienceForLevel(level + 1) - xpAtLevelStart;
        if (xpForThisLevel <= 0)
            return 0f;
        return (totalExperience - xpAtLevelStart) / (float) xpForThisLevel;
    }
}
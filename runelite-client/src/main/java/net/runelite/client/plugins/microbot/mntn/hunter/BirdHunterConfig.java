package net.runelite.client.plugins.microbot.mntn.hunter;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

// * Used for configuration options when toggling plugin
@ConfigGroup("Fishing")
public interface BirdHunterConfig extends Config {
    @ConfigSection(
            name = "General",
            description = "General",
            position = 0
    )
    String generalSection = "general";

    @ConfigItem(
            keyName = "BuryBones",
            name = "Bury Bones?",
            description = "Bury bones for prayer xp.",
            position = 0,
            section = generalSection
    )
    default boolean BuryBones()
    {
        return false;
    }

    @ConfigItem(
            keyName = "NumberOfTraps",
            name = "Number of Traps?",
            description = "Pick how many traps to use.",
            position = 2,
            section = generalSection
    )
    default int NumberOfTraps()
    {
        return 1;
    }

}

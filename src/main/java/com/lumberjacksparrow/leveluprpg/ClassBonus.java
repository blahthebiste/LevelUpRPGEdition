package com.lumberjacksparrow.leveluprpg;

import net.minecraft.util.ResourceLocation;


public final class ClassBonus {
    /**
     * The key used for registering skill data into players
     */
    public static ResourceLocation SKILL_LOCATION = new ResourceLocation("com/lumberjacksparrow/leveluprpg", "skills");

    /**
     * The sub keys used when registering each skill data
     */
    public static String[] skillNames = {
            "Vitality", "Might", "Finesse", "Focus", "Stealth", "Luck", // Core Attributes
            "UnspentSkillPoints"
    };
}

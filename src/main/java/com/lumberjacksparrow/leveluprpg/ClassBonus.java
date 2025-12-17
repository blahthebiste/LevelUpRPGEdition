package com.lumberjacksparrow.leveluprpg;

import net.minecraft.util.ResourceLocation;


public final class ClassBonus {
    /**
     * The key used for registering skill data into players
     */
    public static final ResourceLocation SKILL_LOCATION = new ResourceLocation("com/lumberjacksparrow/leveluprpg", "skills");

    /**
     * The sub keys used when registering each skill data
     */
    public final static String[] skillNames = {
            "Vitality", "Might", "Finesse", "Focus", "Stealth", "Luck", // Core Attributes
            "UnspentSkillPoints"
    };
/*
    public static void addBonusToSkill(PlayerExtendedProperties properties, String name, int bonus, boolean isNew, EntityPlayer player) {
        properties.addToSkill(name, bonus * (isNew ? 1 : -1), player);
    }


    private static void applyBonus(PlayerExtendedProperties properties, byte playerClass, boolean isNew, EntityPlayer player) {
        CLASSES clas = CLASSES.from(playerClass);
        if (clas.isNone())
            return;
        if (clas.hasOnlyOneSkill()) {
            addBonusToSkill(properties, skillNames[clas.bigStatBonus], bonusPoints, isNew, player);
            return;
        }
        int small = bonusPoints / 4;
        int big = bonusPoints - 2 * small;//Make sure all points are allocated no matter what value bonus is
        addBonusToSkill(properties, skillNames[clas.bigStatBonus], big, isNew, player);
        addBonusToSkill(properties, skillNames[clas.smallStatBonus1], small, isNew, player);
        addBonusToSkill(properties, skillNames[clas.smallStatBonus2], small, isNew, player);
    }

    /**
     * Handle class change
     * First remove all bonus points from the old class,
     * then add all bonus points for the new one

    // No longer necessary, until we decide to give classes innate skills without them spending a skill point
    public static void applyBonus(PlayerExtendedProperties properties, byte oldClass, byte newClass) {
//        applyBonus(properties, oldClass, false);
//        applyBonus(properties, newClass, true);
    }

    public enum CLASSES { // TODO: remove, clean up, or rework stat bonuses
        NONE(0, 0, 0),
        BERSERKER(-1, -1, -1),
        CLERIC(2, 6, 0),
        DRUID(5, 8, 6),
        ARCHER(1, 2, 5),
        ROGUE(4, 3, 7),
        WIZARD(9, 10, 3);
        private final int bigStatBonus, smallStatBonus1, smallStatBonus2;

        CLASSES(int bigStatBonus, int smallStatBonus1, int smallStatBonus2) {
            this.bigStatBonus = bigStatBonus;
            this.smallStatBonus1 = smallStatBonus1;
            this.smallStatBonus2 = smallStatBonus2;
        }

        public static CLASSES from(byte b) {
            if (b < 0)
                return NONE;
            return values()[b];
        }

        public boolean isNone() {
            return this == NONE;
        }

        public boolean hasOnlyOneSkill() {
            return this.bigStatBonus == this.smallStatBonus1 && this.bigStatBonus == this.smallStatBonus2;
        }
    }*/
}

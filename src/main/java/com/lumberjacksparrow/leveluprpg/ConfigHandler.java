package com.lumberjacksparrow.leveluprpg;

import meldexun.betterconfig.api.BetterConfig;
import meldexun.betterconfig.api.BetterConfigManager;
import meldexun.betterconfig.api.Sync;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@BetterConfig(modid = LevelUpRPG.ID)
public class ConfigHandler {

    @Config.Comment("Misc/General Options and tweaks")
    @Config.Name("General Options")
    public static final GeneralConfig GENERAL_CONFIG = new GeneralConfig();

    @Config.Comment("Options related to the various skills that players can improve")
    @Config.Name("Skill Options")
    public static final SkillConfig SKILL_CONFIG = new SkillConfig();

    @Config.Comment("Options related to the various classes that players can become")
    @Config.Name("Class Options")
    public static final ClassesConfig CLASSES_CONFIG = new ClassesConfig();

    @Config.Comment("Client-Side Options for the HUD")
    @Config.Name("HUD Options")
    public static final HUDConfig HUD_CONFIG = new HUDConfig();

    @Sync
    public static class GeneralConfig {

        @Config.Comment("The command that is run every second for Mana regen. <player> is replaced with the player's username, and <amount> is replaced by the mana regen as determined by other factors.")
        @Config.Name("Mana regen command")
        public String manaRegenCommand = "/addPlayerMana <player> <amount>";

        @Config.Comment("This command is run whenever the Book of Benediction releases right-click, while off cool-down. <player> is replaced with the name of the player who used it. By default, it generates a massive holy nova.")
        @Config.Name("Book of Benediction command")
        public String bookOfBenedictionCommand = "/cast ebwizardry:healing_aura <player> {blast:20.0,duration:0.02,potency:20.0}";

        @Config.Comment("In vanilla, this is 1.5x, meaning critical hits deal +50% more damage.")
        @Config.Name("Base crit damage multiplier")
        public double baseCritMult = 1.5;

        @Config.Comment("Sneak attacks deal bonus damage. This value affects sneak attacks with all ranged attacks. Example: at 1.5, a sneak attack with a bow would deal 50% extra damage.")
        @Config.Name("Ranged sneak attack damage multiplier")
        public double rangedSneakAttackMult = 1.5;

        @Config.Comment("Sneak attacks deal bonus damage. This value affects sneak attacks with all melee attacks. Example: at 2.0, a sneak attack with a sword would deal double damage.")
        @Config.Name("Melee sneak attack damage multiplier")
        public double meleeSneakAttackMult = 2.0;

        @Config.Comment("Default max mana for players. MUST BE MANUALLY SYNCED WITH WIZARDRY PLAYER MANA.")
        @Config.Name("Base mana")
        public int baseMana = 10;

        @Config.Comment("The number of ticks that you must hold down right-click on the Book of Benediction to use it. Example: if this is set to 60, then it will take 3 seconds to use the book.")
        @Config.Name("Book of Benediction use time (ticks)")
        public int bookOfBenedictionUseTime = 60; // Default use duration in ticks = 3 seconds

        @Config.Comment("The number of ticks that the Book of Benediction goes on cool-down for when used. Example: if this is set to 60, then the cooldown will be 3 seconds.")
        @Config.Name("Book of Benediction cooldown (ticks)")
        public int bookOfBenedictionCooldown = 12000; // Default cooldown in ticks = 10 minutes

        @Config.Comment("If this is set to false, the Book of Benediction item will be disabled.")
        @Config.Name("Enable the Book of Benediction item?")
        public boolean bookOfBenedictionEnabled = true;

        @Config.Comment("If this is set to false, anyone can use the Book of Benediction.")
        @Config.Name("Only Clerics can use the Book of Benediction?")
        public boolean bookOfBenedictionRestricted = true;

    }

    @Sync
    public static class SkillConfig {

        @Config.Comment("Example: at 0.4, 20 points of Finesse would improve draw speed by 80%.")
        @Config.Name("Draw-speed bonus% per point of Finesse")
        public double finesseDrawSpeedBonus = 0.4;

        @Config.Comment("Example: at 0.2, 5 points of Finesse would reduce fall damage by 1.")
        @Config.Name("Fall damage reduction per point of Finesse")
        public double finesseFallDamageReduction = 0.2;

        @Config.Comment("Used by the Mana Regen Command. Example: if this is 0.5, then every second, a player with 20 Intelligence would regen 10 mana.")
        @Config.Name("Mana regen per Intelligence")
        public double intelligenceBonusManaRegen = 0.2;

        @Config.Comment("Example: If this is 30, then with 5 Intelligence, you would have +150 max mana.")
        @Config.Name("Max mana increase per Intelligence")
        public int intelligenceBonusMana = 10;

        @Config.Comment("Example: at 0.01, 1 point of Luck would grant an extra +1% crit chance.")
        @Config.Name("Extra random crit chance per point of Luck")
        public double luckBonusCritChance = 0.01;

        @Config.Comment("Example: if this is 1.0, then every point of Luck improves your vanilla luck attribute by 1.")
        @Config.Name("Bonus vanilla luck per point of Luck")
        public double luckBonusPerLuck = 1.0; // Yes this is silly and confusing if it isn't 1=1

        @Config.Comment("The number of skill points granted on each level up. Minimum is 0")
        @Config.Name("Skill Points gained per level")
        public double skillPointsGainPerLevel = 1.0D;

        @Config.Comment("How many skill points are lost on death, in percent.")
        @Config.Name("Skill points lost on death")
        public int resetSkillOnDeath = 0;

        @Config.Comment("The cap on points per skill. Minimum is 1")
        @Config.Name("Max points per skill")
        public int maxPointsPerSkill = 20;

        @Config.Comment("Example: if this is 0.25, your mining speed will be be increases by 25% per Might, while empty handed.")
        @Config.Name("Bonus empty-handed mining speed per point of Might")
        public double mightBonusBreakSpeed = 0.25;

        @Config.Comment("Example: if this is 0.25, your melee attacks would deal +1 damage with 4 points of Might.")
        @Config.Name("Bonus melee damage per point of Might")
        public double mightBonusMeleeDamage = 0.25;

        @Config.Comment("Note that most vanilla mobs have an aggro range of 16, except zombies who can see you from 35 blocks away.")
        @Config.Name("Reduce mob aggro range (in blocks) per point of Sneak (while sneaking)")
        public double sneakAggroRangeReduction = 1.75;

        @Config.Comment("Example: at 0.075, 1 point of Sneak would increase your crit damage multiplier from 1.5x to 1.575x.")
        @Config.Name("Crit damage multiplier bonus granted per point of Sneak")
        public double sneakCritMult = 0.075;

        @Config.Comment("Example: if this is 1.0, then 7 Vitality would add +3.5 hearts of max HP.")
        @Config.Name("Bonus max HP per point of Vitality")
        public double vitalityHPBonus = 1.0;

        @Config.Comment("Example: if this is 0.1, then 10 Vitality would add +1 toughness.")
        @Config.Name("Bonus toughness per point of Vitality")
        public double vitalityToughnessBonus = 0.1;

    }

    @Sync
    public static class ClassesConfig {

        @Config.Comment("If enabled, players choose a class before allocating skill points. Disabling this does not remove class benefits from existing players.")
        @Config.Name("Enable classes")
        public boolean allowClasses = true;

        @Config.Comment("Does the player lose their class on death?")
        @Config.Name("Reset player class on death")
        public boolean resetClassOnDeath = false;

        @Config.Comment("Level at which a player can choose a class, and begin earning skill points")
        @Config.Name("Starting level")
        public int minLevel = 4;

        @Config.Comment("Affects the Berserker's main perk. Example: if this is set to 6.0, then Berserkers heal 3 full hearts upon scoring a melee kill.")
        @Config.Name("Berserker heal on kill")
        public double berserkerHealOnKill = 6.0;

        @Config.Comment("Affects the Wizard's main perk. Used by the Mana Regen Command. Example: if this is 5.0, then every second, wizard players regen +5 extra mana.")
        @Config.Name("Wizard bonus mana regen")
        public double wizardBonusManaRegen = 5.0;

        @Config.Comment("This command is run when a player selects Druid as their class, replacing <player> with their name. It is meant to grant them access to the morphing ability.")
        @Config.Name("Grant Morphing ability command")
        public String druidGrantMorphAbilityCommand = "/morph enable <player>";

        @Config.Comment("This command is run when a player resets their class, or becomes a class other than Druid, replacing <player> with their name. It is meant to revoke their access to the morphing ability.")
        @Config.Name("Revoke Morphing command")
        public String druidRemoveMorphAbilityCommand = "/morph disable <player>";

        @Config.Comment("This command is run when a player resets their class, or becomes a class other than Druid, replacing <player> with their name. It is meant to change them back into their human form.")
        @Config.Name("Force de-morph command")
        public String druidForceDemorphCommand = "/morph demorph <player> true";

    }

    public static class HUDConfig {

        @Config.Comment("If anything should be rendered on screen at all.")
        @Config.Name("Allow HUD")
        public boolean allowHUD = true;

        @Config.Comment("Should the player class be displayed in the top left corner.")
        @Config.Name("Render HUD on Top Left")
        public boolean renderTopLeft = true;

        @Config.Comment("Should available skill points be displayed on the experience bar.")
        @Config.Name("Render HUD on Exp Bar")
        public boolean renderExpBar = true;
    }

    @Mod.EventBusSubscriber(modid = LevelUpRPG.ID)
    private static class EventHandler{

        @SubscribeEvent
        public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
            if(event.getModID().equals(LevelUpRPG.ID)) {
                BetterConfigManager.sync(LevelUpRPG.ID);
            }
        }
    }

}

package com.lumberjacksparrow.leveluprpg;

import com.lumberjacksparrow.leveluprpg.capabilities.LevelUpCapability;
import com.lumberjacksparrow.leveluprpg.event.*;
import com.lumberjacksparrow.leveluprpg.player.IPlayerClass;
import com.lumberjacksparrow.leveluprpg.player.PlayerExtendedProperties;
import com.lumberjacksparrow.leveluprpg.proxy.SkillProxy;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.FMLEventChannel;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

import java.util.logging.Logger;

@Mod(modid = LevelUpRPG.ID, name = "${mod_name}", version = "${version}", guiFactory = "com.lumberjacksparrow.leveluprpg.gui.ConfigGUI")
public final class LevelUpRPG {
    public final static String ID = "leveluprpg";
    @Instance(value = ID)
    public static LevelUpRPG instance;
    @SidedProxy(clientSide = "com.lumberjacksparrow.leveluprpg.proxy.SkillClientProxy", serverSide = "com.lumberjacksparrow.leveluprpg.proxy.SkillProxy")
    public static SkillProxy proxy;
    private Property[] clientProperties;
    private Property[] serverProperties;
    private static Configuration config;
    public static boolean allowHUD = true, renderTopLeft = true, renderExpBar = true, changeFOV = true, allowClasses = false, bookOfBenedictionEnabled = true, bookOfBenedictionRestricted = true;
    public static FMLEventChannel initChannel, skillChannel, classChannel, configChannel;
    public static String manaRegenCommand = "/addPlayerMana <player> <amount>";
    public static String bookOfBenedictionCommand = "/cast ebwizardry:healing_aura <player> {blast:20.0, duration:0.02}";
    public static double manaRegenPerIntelligence = 0.2;
    public static double wizardBonusManaRegen = 5.0;
    public static int bookOfBenedictionUseTime = 3 * 20; // Default use duration = 3 seconds
    public static int bookOfBenedictionCooldown = 10 * 60 * 20; // Default cooldown = 10 minutes
    // Currently not used, but may be implemented in the future
    public static int bonusPoints = 0;
    // Cap on each skill
    public static int maxPointsPerSkill = 20;

    @EventHandler
    public void load(FMLInitializationEvent event) {
        System.out.println("DEBUG: LevelUpRPG FMLInitializationEvent");
        Logger logger = Logger.getLogger("com/lumberjacksparrow/leveluprpg");
        logger.info("[Level Up] registering events");
        MinecraftForge.EVENT_BUS.register(BowEventHandler.INSTANCE);
        MinecraftForge.EVENT_BUS.register(FightEventHandler.INSTANCE);
        logger.info("[Level Up] registering packets");
        SkillPacketHandler sk = new SkillPacketHandler();
        initChannel = NetworkRegistry.INSTANCE.newEventDrivenChannel(SkillPacketHandler.CHAN[0]);
        initChannel.register(sk);
        classChannel = NetworkRegistry.INSTANCE.newEventDrivenChannel(SkillPacketHandler.CHAN[1]);
        classChannel.register(sk);
        skillChannel = NetworkRegistry.INSTANCE.newEventDrivenChannel(SkillPacketHandler.CHAN[2]);
        skillChannel.register(sk);
        configChannel = NetworkRegistry.INSTANCE.newEventDrivenChannel(SkillPacketHandler.CHAN[3]);
        configChannel.register(sk);
        logger.info("[Level Up] registering clientside stuff");
        proxy.registerGui();
    }

    @EventHandler
    public void load(FMLPreInitializationEvent event) {
        LevelUpRegistry.init();
        config = new Configuration(event.getSuggestedConfigurationFile());
        config.addCustomCategoryComment("HUD", "Entirely client side. No need to sync.");
        initClientProperties();
        config.addCustomCategoryComment("Items", "Need to be manually synced to the client on a dedicated server");
        config.addCustomCategoryComment("Cheats", "Will be automatically synced to the client on a dedicated server");
        config.addCustomCategoryComment("Tweaks", "New options added by Morph Tweaked");
        initServerProperties();
        useServerProperties();
        CapabilityManager.INSTANCE.register(IPlayerClass.class, new LevelUpCapability.CapabilityPlayerClass<>(), PlayerExtendedProperties::new);
        if (config.hasChanged())
            config.save();
        MinecraftForge.EVENT_BUS.register(FMLEventHandler.INSTANCE);
        MinecraftForge.EVENT_BUS.register(new PlayerEventHandler());
    }

    @EventHandler
    public void load(FMLPostInitializationEvent event) {

    }

    private void initClientProperties() {
        clientProperties = new Property[]{
                config.get("HUD", "allow HUD", allowHUD, "If anything should be rendered on screen at all.").setRequiresMcRestart(true),
                config.get("HUD", "render HUD on Top Left", renderTopLeft, "Should the player class be displayed in the top left corner."),
                config.get("HUD", "render HUD on Exp Bar", renderExpBar, "Should available skill points be displayed on the experience bar."),
                config.get("Tweaks", "Book of Benediction cooldown (ticks)", bookOfBenedictionCooldown, "The number of ticks that the Book of Benediction goes on cool-down for when used. Example: if this is set to 60, then the cooldown will be 3 seconds."),
                config.get("Tweaks", "Book of Benediction use time (ticks)", bookOfBenedictionUseTime, "The number of ticks that you must hold down right-click on the Book of Benediction to use it. Example: if this is set to 60, then it will take 3 seconds to use the book."),
                config.get("Tweaks", "The command executed by the Book of Benediction", bookOfBenedictionCommand, "This command is run whenever the Book of Benediction is right-clicked, and off cool-down."),
                config.get("Tweaks", "Mana regen command", manaRegenCommand, "The command that is run every second for Mana regen. <player> is replaced with the player's username, and <amount> is replaced by the mana regen as determined by other factors."),

        };
        allowHUD = clientProperties[0].getBoolean();
        renderTopLeft = clientProperties[1].getBoolean();
        renderExpBar = clientProperties[2].getBoolean();
        bookOfBenedictionCooldown = clientProperties[3].getInt();
        bookOfBenedictionUseTime = clientProperties[4].getInt();
        bookOfBenedictionCommand = clientProperties[5].getString();
        manaRegenCommand = clientProperties[6].getString();
    }

    private void initServerProperties() {
        String category = "Cheats";
        serverProperties = new Property[]{
                config.get(category, "Max points per skill", maxPointsPerSkill, "Minimum is 1"),
                config.get(category, "Bonus points for classes", bonusPoints, "Points given when choosing a class, allocated automatically.\n Minimum is 0, Maximum is max points per skill times 2"),
                config.get(category, "Skill Points gained per level", PlayerEventHandler.skillPointsPerLevel, "Minimum is 0"),
                config.get(category, "Skill points lost on death", (int) PlayerEventHandler.resetSkillOnDeath * 100, "How many skill points are lost on death, in percent.").setMinValue(0).setMaxValue(100),
                config.get(category, "Reset player class on death", PlayerEventHandler.resetClassOnDeath, "Does the player lose their class on death?"),
                // New tweak options:
                config.get("Tweaks", "Enable classes", allowClasses, "If enabled, players choose a class before allocating skill points. Disabling this does not remove class benefits from existing players."),
                config.get("Tweaks", "Mana regen per Intelligence", manaRegenPerIntelligence, "Used by the Mana Regen Command. Example: if this is 0.5, then every second, a player with 20 Intelligence would regen 10 mana."),
                config.get("Tweaks", "Wizard bonus mana regen", wizardBonusManaRegen, "Used by the Mana Regen Command. Example: if this is 5.0, then every second, wizard player regens +5 extra mana."),
                config.get("Tweaks", "Whether the Book of Benediction item is enabled", bookOfBenedictionEnabled, "If this is set to false, the Book of Benediction item will be disabled."),
                config.get("Tweaks", "Only Clerics can use the Book of Benediction?", bookOfBenedictionRestricted, "If this is set to false, anyone can use the Book of Benediction."),
        };
    }

    public void useServerProperties() {
        maxPointsPerSkill = serverProperties[0].getInt();
        bonusPoints = serverProperties[1].getInt();
        double skillPointsPerLevel = serverProperties[2].getDouble();
        if (skillPointsPerLevel >= 0.0D)
            PlayerEventHandler.skillPointsPerLevel = skillPointsPerLevel <= maxPointsPerSkill ? skillPointsPerLevel : maxPointsPerSkill;
        PlayerEventHandler.resetSkillOnDeath = (float) serverProperties[3].getInt() / 100.00F;
        PlayerEventHandler.resetClassOnDeath = serverProperties[4].getBoolean();
        allowClasses = serverProperties[5].getBoolean();
        manaRegenPerIntelligence = serverProperties[6].getDouble();
        wizardBonusManaRegen = serverProperties[7].getDouble();
        bookOfBenedictionEnabled = serverProperties[8].getBoolean();
        bookOfBenedictionRestricted = serverProperties[9].getBoolean();
    }

    public Property[] getServerProperties() {
        return serverProperties;
    }

    public boolean[] getGUIProperties() {
        boolean[] result = new boolean[clientProperties.length - 2];
        for (int i = 0; i < clientProperties.length - 2; i++) {
                result[i] = clientProperties[i].getBoolean();
        }
        return result;
    }

    public void refreshValues(boolean[] values) {
        LevelUpRPG.allowHUD = values[0];
        LevelUpRPG.renderTopLeft = values[1];
        LevelUpRPG.renderExpBar = values[2];
        LevelUpRPG.changeFOV = values[3];
        for (int i = 0; i < values.length; i++) {
            clientProperties[i].set(values[i]);
        }
        config.save();
    }

    // Hard coded map of property ID to stat
    public static int getVitality(EntityPlayer player) {
        return PlayerExtendedProperties.getSkillFromIndex(player, 0);
    }

    public static int getMight(EntityPlayer player) {
        return PlayerExtendedProperties.getSkillFromIndex(player, 1);
    }

    public static int getFinesse(EntityPlayer player) {
        return PlayerExtendedProperties.getSkillFromIndex(player, 2);
    }

    public static int getFocus(EntityPlayer player) {
        return PlayerExtendedProperties.getSkillFromIndex(player, 3);
    }

    public static int getStealth(EntityPlayer player) {
        return PlayerExtendedProperties.getSkillFromIndex(player, 4);
    }

    public static int getLuck(EntityPlayer player) {
        return PlayerExtendedProperties.getSkillFromIndex(player, 5);
    }

}

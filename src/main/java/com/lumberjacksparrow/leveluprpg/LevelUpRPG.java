package com.lumberjacksparrow.leveluprpg;

import com.lumberjacksparrow.leveluprpg.api.IProcessor;
import com.lumberjacksparrow.leveluprpg.capabilities.LevelUpCapability;
import com.lumberjacksparrow.leveluprpg.event.*;
import com.lumberjacksparrow.leveluprpg.player.IPlayerClass;
import com.lumberjacksparrow.leveluprpg.player.PlayerExtendedProperties;
import com.lumberjacksparrow.leveluprpg.proxy.SkillProxy;
import com.lumberjacksparrow.leveluprpg.proxy.SkillClientProxy;
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

@Mod(modid = LevelUpRPG.ID, name = "${mod_name}", version = "${version}", guiFactory = "com.lumberjacksparrow.leveluprpg.ConfigLevelUp")
public final class LevelUpRPG {
    public final static String ID = "leveluprpg";
    @Instance(value = ID)
    public static LevelUpRPG instance;
    @SidedProxy(clientSide = "com.lumberjacksparrow.leveluprpg.proxy.SkillClientProxy", serverSide = "com.lumberjacksparrow.leveluprpg.proxy.SkillProxy")
    public static SkillProxy proxy;
    private Property[] clientProperties;
    private Property[] serverProperties;
    private static Configuration config;
    public static boolean allowHUD = true, renderTopLeft = true, renderExpBar = true, changeFOV = true;
    public static FMLEventChannel initChannel, skillChannel, classChannel, configChannel;

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
        if(LevelUpRegistry.respecBook != null) proxy.register(LevelUpRegistry.respecBook, "levelup:respec_book");
        if(LevelUpRegistry.respecBookFull != null) proxy.register(LevelUpRegistry.respecBookFull, "levelup:respec_book_full");
    }

    @EventHandler
    public void load(FMLPreInitializationEvent event) {
        LevelUpRegistry.init();
        config = new Configuration(event.getSuggestedConfigurationFile());
        config.addCustomCategoryComment("HUD", "Entirely client side. No need to sync.");
        initClientProperties();
        config.addCustomCategoryComment("Items", "Need to be manually synced to the client on a dedicated server");
        config.addCustomCategoryComment("Cheats", "Will be automatically synced to the client on a dedicated server");
        initServerProperties();
        useServerProperties();
        CapabilityManager.INSTANCE.register(IPlayerClass.class, new LevelUpCapability.CapabilityPlayerClass<IPlayerClass>(), PlayerExtendedProperties.class);
        CapabilityManager.INSTANCE.register(IProcessor.class, new LevelUpCapability.CapabilityProcessorClass<IProcessor>(), LevelUpCapability.CapabilityProcessorDefault.class);
        if (config.hasChanged())
            config.save();
        if (event.getSourceFile().getName().endsWith(".jar")) {
            proxy.tryUseMUD();
        }
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
                config.get("HUD", "render HUD on Exp Bar", renderExpBar, "Should available skill points be displayed on the experience bar.")};
        allowHUD = clientProperties[0].getBoolean();
        renderTopLeft = clientProperties[1].getBoolean();
        renderExpBar = clientProperties[2].getBoolean();
    }

    private void initServerProperties() {
        String cat = "Cheats";
        serverProperties = new Property[]{
                config.get(cat, "Max points per skill", ClassBonus.getMaxSkillPoints(), "Minimum is 1"),
                config.get(cat, "Bonus points for classes", ClassBonus.getBonusPoints(), "Points given when choosing a class, allocated automatically.\n Minimum is 0, Maximum is max points per skill times 2"),
                config.get(cat, "Skill Points gained per level", PlayerEventHandler.skillPointsPerLevel, "Minimum is 0"),
                config.get(cat, "Skill points lost on death", (int) PlayerEventHandler.resetSkillOnDeath * 100, "How much skill points are lost on death, in percent.").setMinValue(0).setMaxValue(100),
                config.get(cat, "Reset player class on death", PlayerEventHandler.resetClassOnDeath, "Do the player lose the class he choose on death ?")};
    }

    public void useServerProperties() {
        ClassBonus.setSkillMax(serverProperties[0].getInt());
        ClassBonus.setBonusPoints(serverProperties[1].getInt());
        double opt = serverProperties[2].getDouble();
        if (opt >= 0.0D)
            PlayerEventHandler.skillPointsPerLevel = opt <= ClassBonus.getMaxSkillPoints() ? opt : ClassBonus.getMaxSkillPoints();
        PlayerEventHandler.resetSkillOnDeath = (float) serverProperties[3].getInt() / 100.00F;
        PlayerEventHandler.resetClassOnDeath = serverProperties[4].getBoolean();
    }

    public Property[] getServerProperties() {
        return serverProperties;
    }

    public boolean[] getClientProperties() {
        boolean[] result = new boolean[clientProperties.length];
        for (int i = 0; i < clientProperties.length; i++) {
            result[i] = clientProperties[i].getBoolean();
        }
        return result;
    }

    public void refreshValues(boolean[] values) {
        if (values.length == clientProperties.length) {
            LevelUpRPG.allowHUD = values[0];
            LevelUpRPG.renderTopLeft = values[1];
            LevelUpRPG.renderExpBar = values[2];
            LevelUpRPG.changeFOV = values[3];
            for (int i = 0; i < values.length; i++) {
                clientProperties[i].set(values[i]);
            }
            config.save();
        }
    }

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

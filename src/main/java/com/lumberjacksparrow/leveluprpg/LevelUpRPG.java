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
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.FMLEventChannel;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.MinecraftForge;

import java.util.logging.Logger;

@Mod(
        modid = Tags.MODID,
        version = Tags.VERSION,
        name = Tags.NAME,
        dependencies =
                "required:betterconfig@[1.3.0,)"
)
public class LevelUpRPG {
    public static String ID = "leveluprpg";
//    @Instance(value = ID)
//    public static LevelUpRPG instance;
    @SidedProxy(clientSide = "com.lumberjacksparrow.leveluprpg.proxy.SkillClientProxy", serverSide = "com.lumberjacksparrow.leveluprpg.proxy.SkillProxy")
    public static SkillProxy proxy;
    public static FMLEventChannel initChannel, skillChannel, classChannel;
    // Currently not used, but may be implemented in the future
    //public static int bonusPoints = 0;

    @EventHandler
    public void load(FMLInitializationEvent event) {
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
        logger.info("[Level Up] registering clientside stuff");
        proxy.registerGui();
    }

    @EventHandler
    public void load(FMLPreInitializationEvent event) {
        LevelUpRegistry.init();
        CapabilityManager.INSTANCE.register(IPlayerClass.class, new LevelUpCapability.CapabilityPlayerClass<>(), PlayerExtendedProperties::new);
        MinecraftForge.EVENT_BUS.register(FMLEventHandler.INSTANCE);
        MinecraftForge.EVENT_BUS.register(new PlayerEventHandler());
    }



     //       PlayerEventHandler.skillPointsPerLevel = skillPointsPerLevel <= maxPointsPerSkill ? skillPointsPerLevel : maxPointsPerSkill;

    // Hard coded map of property ID to Skill
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

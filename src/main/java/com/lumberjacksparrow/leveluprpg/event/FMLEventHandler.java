package com.lumberjacksparrow.leveluprpg.event;

import com.lumberjacksparrow.leveluprpg.ClassBonus;
import com.lumberjacksparrow.leveluprpg.LevelUpRPG;
import com.lumberjacksparrow.leveluprpg.player.PlayerExtendedProperties;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.common.IPlantable;

import java.util.*;

public final class FMLEventHandler {

    public static final FMLEventHandler INSTANCE = new FMLEventHandler();
    /**
     * Blocks that could be crops, but should be left alone by Farming skill
     */
    private List<IPlantable> blackListedCrops;

    private FMLEventHandler() {
    }

    @SubscribeEvent
    public void onPlayerUpdate(TickEvent.PlayerTickEvent event) {
        EntityPlayer player = event.player;
        if (event.phase == TickEvent.Phase.START) {
            //Give points on leveluprpg
            if (PlayerExtendedProperties.getPlayerClass(player) != 0) {
                double diff = PlayerEventHandler.skillPointsPerLevel * (player.experienceLevel - PlayerEventHandler.minLevel) + ClassBonus.getBonusPoints() - PlayerExtendedProperties.getClassOfPlayer(player).getSkillPoints();
                if (diff >= 1.0D)
                    PlayerExtendedProperties.getClassOfPlayer(player).addToSkill("UnspentSkillPoints", (int) Math.floor(diff), player);
            }
        }
        if(player == null || player.getEntityWorld().isRemote || player.getEntityWorld().getMinecraftServer() == null) {
            return;
        }
        int focus = LevelUpRPG.getFocus(player);
        if(focus != 0) {
            // Mana regen: just run the mana regen command each second
            if(player.world.getWorldTime() % 20 == 0 ) {
                String manaRegenCommand = "/addPlayerMana " + player.getName() + " "+(((float)focus)/5.0f);
                player.getEntityWorld().getMinecraftServer().commandManager.executeCommand(player.getEntityWorld().getMinecraftServer(), manaRegenCommand);
            }
        }
    }

    /**
     * Track player changing dimension to update skill points data
     */
    @SubscribeEvent
    public void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        loadPlayer(event.player);
    }

    /**
     * Track player respawn to update skill points data
     */
    @SubscribeEvent
    public void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        loadPlayer(event.player);
    }

    /**
     * Track player login to update skill points data and some configuration values
     */
    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.player instanceof EntityPlayerMP) {
            loadPlayer(event.player);
            LevelUpRPG.configChannel.sendTo(SkillPacketHandler.getConfigPacket(LevelUpRPG.instance.getServerProperties()), (EntityPlayerMP) event.player);
        }
    }

    /**
     * Help build the packet to send to client for updating skill point data
     */
    public void loadPlayer(EntityPlayer player) {
        if (player instanceof EntityPlayerMP) {
            byte cl = PlayerExtendedProperties.getPlayerClass(player);
            int[] data = PlayerExtendedProperties.getClassOfPlayer(player).getPlayerData(false);
            LevelUpRPG.initChannel.sendTo(SkillPacketHandler.getPacket(Side.CLIENT, 0, cl, data), (EntityPlayerMP) player);
        }
    }
}

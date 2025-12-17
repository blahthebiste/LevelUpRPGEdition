package com.lumberjacksparrow.leveluprpg.event;

import com.lumberjacksparrow.leveluprpg.LevelUpRPG;
import com.lumberjacksparrow.leveluprpg.player.PlayerExtendedProperties;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import static com.lumberjacksparrow.leveluprpg.LevelUpRPG.*;
import static com.lumberjacksparrow.leveluprpg.player.PlayerExtendedProperties.getFrom;

public final class FMLEventHandler {

    public static final FMLEventHandler INSTANCE = new FMLEventHandler();

    private FMLEventHandler() {
    }

    @SubscribeEvent
    public void onPlayerUpdate(TickEvent.PlayerTickEvent event) {
        EntityPlayer player = event.player;
        if(event.phase != TickEvent.Phase.START) //player == null || player.getEntityWorld().isRemote || player.getEntityWorld().getMinecraftServer() == null ||
        {
            return;
        }
        //Give points on level up
        if (!allowClasses || PlayerExtendedProperties.getPlayerClass(player) != 0) {
            double diff = PlayerEventHandler.skillPointsPerLevel * (player.experienceLevel - PlayerEventHandler.minLevel) + bonusPoints - PlayerExtendedProperties.getFrom(player).getTotalSkillPoints();
            if (diff >= 1.0D) {
                PlayerExtendedProperties.getFrom(player).addToSkill("UnspentSkillPoints", (int) Math.floor(diff), player);
            }
        }
        if(player == null || player.getEntityWorld().isRemote || player.getEntityWorld().getMinecraftServer() == null)
        {
            return;
        }
        // Mana regen
        if(player.world.getWorldTime() % 20 == 0) {
            doManaRegen(player);
        }
    }

    public void doManaRegen(EntityPlayer player){
        if(player.getEntityWorld().getMinecraftServer() == null) {
            // This should probably never happen
            return;
        }
        int focus = LevelUpRPG.getFocus(player);
        // Mana regen: just run the mana regen command each second
        double amount = ((double)focus)*manaRegenPerIntelligence;
        // Wizards get +4 mana regen per second
        if("wizard".equalsIgnoreCase(getFrom(player).getClassName())) {
            amount += wizardBonusManaRegen;
        }
        if(amount > 0.0f) {
            String manaRegenCommandFilled = manaRegenCommand.replace("<player>", player.getName()).replace("<amount>", String.valueOf(amount));
            player.getEntityWorld().getMinecraftServer().commandManager.executeCommand(player.getEntityWorld().getMinecraftServer(), manaRegenCommandFilled);
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
            int[] data = PlayerExtendedProperties.getFrom(player).getPlayerData(false);
            LevelUpRPG.initChannel.sendTo(SkillPacketHandler.getPacket(Side.CLIENT, 0, cl, data), (EntityPlayerMP) player);
        }
    }
}

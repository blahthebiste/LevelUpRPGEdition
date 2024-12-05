package levelup.event;

import levelup.ClassBonus;
import levelup.LevelUp;
import levelup.player.PlayerExtendedProperties;
import levelup.SkillPacketHandler;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.event.entity.living.PotionEvent;
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
            //Give points on levelup
            if (PlayerExtendedProperties.getPlayerClass(player) != 0) {
                double diff = PlayerEventHandler.skillPointsPerLevel * (player.experienceLevel - PlayerEventHandler.minLevel) + ClassBonus.getBonusPoints() - PlayerExtendedProperties.getClassOfPlayer(player).getSkillPoints();
                if (diff >= 1.0D)
                    PlayerExtendedProperties.getClassOfPlayer(player).addToSkill("UnspentSkillPoints", (int) Math.floor(diff), player);
            }
        }
        if(player == null || player.getEntityWorld().isRemote || player.getEntityWorld().getMinecraftServer() == null) {
            return;
        }
        int focus = LevelUp.getFocus(player);
        if(focus != 0) {
            // Mana regen: just run the mana regen command each second
            if(player.world.getWorldTime() % 20 == 0 ) {
                String manaRegenCommand = "/addPlayerMana " + player.getName() + " "+(((float)focus)/5.0f);
                player.getEntityWorld().getMinecraftServer().commandManager.executeCommand(player.getEntityWorld().getMinecraftServer(), manaRegenCommand);
            }
        }
    }

    /**
     * Apply bonemeal on non-black-listed blocks around player
     */
//    private void growCropsAround(World world, int range, EntityPlayer player) {
//        int posX = (int) player.posX;
//        int posY = (int) player.posY;
//        int posZ = (int) player.posZ;
//        int dist = range / 2 + 2;
//        for (Object o : BlockPos.getAllInBox(new BlockPos(posX - dist, posY - dist, posZ - dist), new BlockPos(posX + dist + 1, posY + dist + 1, posZ + dist + 1))) {
//            BlockPos pos = (BlockPos) o;
//            Block block = world.getBlockState(pos).getBlock();
//            if(block instanceof IPlantable && !blackListedCrops.contains(block)) {
//                world.scheduleUpdate(pos, block, block.tickRate(world));
//            }
//        }
//    }

    /**
     * Converts given black-listed names into blocks for the internal black-list
     */
//    public void addCropsToBlackList(List<String> blackList) {
//        if (blackListedCrops == null)
//            blackListedCrops = new ArrayList<IPlantable>(blackList.size());
//        for (String txt : blackList) {
//            Block crop = Block.REGISTRY.getObject(new ResourceLocation(txt));
//            if (crop instanceof IPlantable)
//                blackListedCrops.add((IPlantable) crop);
//        }
//    }

    /**
     * Helper to retrieve skill points from the index
     */
//    public static int getSkill(EntityPlayer player, int id) {
//        return PlayerExtendedProperties.getSkillFromIndex(player, id);
//    }

    /**
     * Add more output when smelting food for Cooking and other items for Smelting
     *//*
    @SubscribeEvent
    public void onSmelting(PlayerEvent.ItemSmeltedEvent event) {
        if (!event.player.world.isRemote) {
            Random random = event.player.getRNG();
            ItemStack add = null;
            if (event.smelting.getItemUseAction() == EnumAction.EAT) {
                if (random.nextFloat() <= getSkill(event.player, 7) / 200F) {
                    add = event.smelting.copy();
                }
            } else if (random.nextFloat() <= getSkill(event.player, 4) / 200F) {
                add = event.smelting.copy();
            }
            EntityItem entityitem = ForgeHooks.onPlayerTossEvent(event.player, add, true);
            if (entityitem != null) {
                entityitem.setNoPickupDelay();
                entityitem.setOwner(event.player.getName());
            }
        }
    }*/

    /**
     * Track player crafting to give additional XP
     */
//    @SubscribeEvent
//    public void onCrafting(PlayerEvent.ItemCraftedEvent event) {
//        LevelUp.takenFromCrafting(event.player, event.crafting, event.craftMatrix);
//    }

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
            LevelUp.configChannel.sendTo(SkillPacketHandler.getConfigPacket(LevelUp.instance.getServerProperties()), (EntityPlayerMP) event.player);
        }
    }

    /**
     * Help build the packet to send to client for updating skill point data
     */
    public void loadPlayer(EntityPlayer player) {
        if (player instanceof EntityPlayerMP) {
            byte cl = PlayerExtendedProperties.getPlayerClass(player);
            int[] data = PlayerExtendedProperties.getClassOfPlayer(player).getPlayerData(false);
            LevelUp.initChannel.sendTo(SkillPacketHandler.getPacket(Side.CLIENT, 0, cl, data), (EntityPlayerMP) player);
        }
    }
}

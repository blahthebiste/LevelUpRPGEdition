package levelup.event;

import levelup.ClassBonus;
import levelup.LevelUp;
import levelup.player.PlayerExtendedProperties;
import levelup.SkillPacketHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.common.IPlantable;

import java.util.*;

public final class FMLEventHandler {
    /**
     * Attribute modifier uuids
     */
    private static final UUID luckUID = UUID.fromString("4f7637c8-6106-40d0-96cb-e47f83bfa415");
    private static final UUID attackDamageUID = UUID.fromString("a4dc0b04-f78a-43f6-8805-5ebfaab10b18");
    private static final UUID maxHPUID = UUID.fromString("34dc0b04-f48a-43f6-8805-5ebfaab10b18");
    private static final UUID toughnessUID = UUID.fromString("24dc0b04-f48a-43f6-8805-5ebfaab10b18");

    public static final FMLEventHandler INSTANCE = new FMLEventHandler();
    /**
     * Blocks that could be crops, but should be left alone by Farming skill
     */
    private List<IPlantable> blackListedCrops;

    private FMLEventHandler() {
    }

    @SubscribeEvent
    public void onPlayerUpdate(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            EntityPlayer player = event.player;
            //Give points on levelup
            if (PlayerExtendedProperties.getPlayerClass(player) != 0) {
                double diff = PlayerEventHandler.skillPointsPerLevel * (player.experienceLevel - PlayerEventHandler.minLevel) + ClassBonus.getBonusPoints() - PlayerExtendedProperties.from(player).getSkillPoints();
                if (diff >= 1.0D)
                    PlayerExtendedProperties.from(player).addToSkill("UnspentSkillPoints", (int) Math.floor(diff));
            }
            // Luck modifier
            IAttributeInstance luckAttributeInstance = player.getEntityAttribute(SharedMonsterAttributes.LUCK);
            AttributeModifier mod;
            int luck = LevelUp.getLuck(player);
            if (luck != 0) {
                // Add luck at a 1-to-1 ratio
                if (luckAttributeInstance.getModifier(luckUID) != null) {
                    luckAttributeInstance.removeModifier(luckUID);
                }
                mod = new AttributeModifier(luckUID, "BonusLuckFromSkill", luck, 0);
                luckAttributeInstance.applyModifier(mod);
            }
            // Melee attack damage modifier
            IAttributeInstance attackDamageAttributeInstance = player.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE);
            int might = LevelUp.getMight(player);
            if (might != 0) {
                // Add +0.25 melee damage per point of might
                if (attackDamageAttributeInstance.getModifier(attackDamageUID) != null) {
                    attackDamageAttributeInstance.removeModifier(attackDamageUID);
                }
                mod = new AttributeModifier(attackDamageUID, "BonusMightFromSkill", might * 0.25F, 0);
                attackDamageAttributeInstance.applyModifier(mod);
            }
            // Max HP modifier
            int vitality = LevelUp.getVitality(player);
            if (vitality != 0) {
                IAttributeInstance maxHPAttributeInstance = player.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH);
                IAttributeInstance toughnessAttributeInstance = player.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS);
                // Add max HP at a 1-to-1 ratio
                if (maxHPAttributeInstance.getModifier(maxHPUID) != null) {
                    maxHPAttributeInstance.removeModifier(maxHPUID);
                }
                mod = new AttributeModifier(maxHPUID, "BonusMaxHPFromSkill", vitality, 0);
                maxHPAttributeInstance.applyModifier(mod);
                // Toughness modifier
                // Add 1 toughness per 5 Vitality
                if (toughnessAttributeInstance.getModifier(toughnessUID) != null) {
                    toughnessAttributeInstance.removeModifier(toughnessUID);
                }
                mod = new AttributeModifier(toughnessUID, "BonusToughnessFromSkill", (vitality / 5.0F), 0);
                toughnessAttributeInstance.applyModifier(mod);
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
            int[] data = PlayerExtendedProperties.from(player).getPlayerData(false);
            LevelUp.initChannel.sendTo(SkillPacketHandler.getPacket(Side.CLIENT, 0, cl, data), (EntityPlayerMP) player);
        }
    }

    // Remove attribute modifiers from the player
    public void clearAllModifiers(EntityPlayer player) {
        IAttributeInstance luckAttributeInstance = player.getEntityAttribute(SharedMonsterAttributes.LUCK);
        IAttributeInstance attackDamageAttributeInstance = player.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE);
        IAttributeInstance maxHPAttributeInstance = player.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH);
        IAttributeInstance toughnessAttributeInstance = player.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS);
        if (luckAttributeInstance.getModifier(luckUID) != null) {
            luckAttributeInstance.removeModifier(luckUID);
        }
        if (attackDamageAttributeInstance.getModifier(attackDamageUID) != null) {
            attackDamageAttributeInstance.removeModifier(attackDamageUID);
        }
        if (maxHPAttributeInstance.getModifier(maxHPUID) != null) {
            maxHPAttributeInstance.removeModifier(maxHPUID);
        }
        if (toughnessAttributeInstance.getModifier(toughnessUID) != null) {
            toughnessAttributeInstance.removeModifier(toughnessUID);
        }
    }
}

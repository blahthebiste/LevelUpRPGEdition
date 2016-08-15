package levelup;

import com.google.common.collect.Sets;
import levelup.capabilities.LevelUpCapability;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemSpade;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.oredict.OreDictionary;

import java.util.*;

public final class PlayerEventHandler {
    /**
     * Configurable flags related to breaking speed
     */
    public static boolean oldSpeedDigging = true, oldSpeedRedstone = true;
    /**
     * Configurable flags related to player death
     */
    public static float resetSkillOnDeath = 0.00F;
    public static boolean resetClassOnDeath = false;
    /**
     * If duplicated ores can be placed
     */
    public static boolean noPlaceDuplicate = true;
    /**
     * How much each level give in skill points
     */
    public static double xpPerLevel = 3.0D;
    /**
     * Level at which a player can choose a class, and get its first skill points
     */
    public final static int minLevel = 4;
    /**
     * Random additional loot for Fishing
     */
    private static ItemStack[] lootList = new ItemStack[]{new ItemStack(Items.BONE), new ItemStack(Items.REEDS), new ItemStack(Items.ARROW), new ItemStack(Items.APPLE),
            new ItemStack(Items.BUCKET), new ItemStack(Items.BOAT), new ItemStack(Items.ENDER_PEARL), new ItemStack(Items.FISHING_ROD), new ItemStack(Items.CHAINMAIL_CHESTPLATE), new ItemStack(Items.IRON_INGOT)};
    /**
     * Internal ore counter
     */
    private static Map<Block, Integer> blockToCounter = new IdentityHashMap<Block, Integer>();

    static {
        blockToCounter.put(Blocks.COAL_ORE, 0);
        blockToCounter.put(Blocks.LAPIS_ORE, 1);
        blockToCounter.put(Blocks.REDSTONE_ORE, 2);
        blockToCounter.put(Blocks.IRON_ORE, 3);
        blockToCounter.put(Blocks.GOLD_ORE, 4);
        blockToCounter.put(Blocks.EMERALD_ORE, 5);
        blockToCounter.put(Blocks.DIAMOND_ORE, 6);
        blockToCounter.put(Blocks.QUARTZ_ORE, 7);
    }

    /**
     * Items given by Digging ground
     */
    private static ItemStack digLoot[] = {new ItemStack(Items.CLAY_BALL, 8), new ItemStack(Items.BOWL, 2), new ItemStack(Items.COAL, 4), new ItemStack(Items.PAINTING), new ItemStack(Items.STICK, 4),
            new ItemStack(Items.STRING, 2)};
    private static ItemStack digLoot1[] = {new ItemStack(Items.STONE_SWORD), new ItemStack(Items.STONE_SHOVEL), new ItemStack(Items.STONE_PICKAXE), new ItemStack(Items.STONE_AXE)};
    private static ItemStack digLoot2[] = {new ItemStack(Items.SLIME_BALL, 2), new ItemStack(Items.REDSTONE, 8), new ItemStack(Items.IRON_INGOT), new ItemStack(Items.GOLD_INGOT)};
    private static ItemStack digLoot3[] = {new ItemStack(Items.DIAMOND)};
    /**
     * Internal ores list for Mining
     */
    private static Set<Block> ores = Sets.newIdentityHashSet();

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onBreak(PlayerEvent.BreakSpeed event) {
        ItemStack itemstack = event.getEntityPlayer().getHeldItemMainhand();
        IBlockState state = event.getState();
        Block block = state.getBlock();
        if (itemstack != null)
            if (oldSpeedDigging && itemstack.getItem() instanceof ItemSpade) {
                if (block instanceof BlockDirt || block instanceof BlockGravel) {
                    event.setNewSpeed(event.getNewSpeed() * itemstack.getStrVsBlock(state) / 0.5F);
                }
            } else if (oldSpeedRedstone && itemstack.getItem() instanceof ItemPickaxe && block instanceof BlockRedstoneOre) {
                event.setNewSpeed(event.getNewSpeed() * itemstack.getStrVsBlock(state) / 3F);
            }
        if (block instanceof BlockStone || block == Blocks.COBBLESTONE || block == Blocks.OBSIDIAN || block instanceof BlockOre) {
            event.setNewSpeed(event.getNewSpeed() + getSkill(event.getEntityPlayer(), 0) / 5 * 0.2F);
        } else if (state.getMaterial() == Material.WOOD) {
            event.setNewSpeed(event.getNewSpeed() + getSkill(event.getEntityPlayer(), 3) / 5 * 0.2F);
        }
    }

    /**
     * Track player deaths to reset values when appropriate,
     * and player final strikes on mobs to give bonus xp
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onDeath(LivingDeathEvent event) {
        if (event.getEntityLiving() instanceof EntityPlayer) {
            if (resetClassOnDeath) {
                PlayerExtendedProperties.from((EntityPlayer) event.getEntityLiving()).setPlayerClass((byte) 0);
            }
            if (resetSkillOnDeath > 0.00F) {
                PlayerExtendedProperties.from((EntityPlayer) event.getEntityLiving()).takeSkillFraction(resetSkillOnDeath);
            }
        } else if (event.getEntityLiving() instanceof EntityMob && event.getSource().getEntity() instanceof EntityPlayer) {
            LevelUp.giveBonusFightingXP((EntityPlayer) event.getSource().getEntity());
        }
    }

    /**
     * Change fishing by adding some loots
     * Prevent flagged block placement
     */
    @SubscribeEvent(priority = EventPriority.LOW)
    public void onFishInteract(PlayerInteractEvent.RightClickItem event) {
        if (event.getResult() != Event.Result.DENY)
            //if (event.action == Action.RIGHT_CLICK_AIR) {
        {   EntityFishHook hook = event.getEntityPlayer().fishEntity;
                if (hook != null && hook.caughtEntity == null && hook.ticksCatchable > 0) {//Not attached to some random stuff, and within the time frame for catching
                    int loot = getFishingLoot(event.getEntityPlayer());
                    if (loot >= 0) {
                        ItemStack stack = event.getEntityPlayer().inventory.getCurrentItem();
                        int i = stack.stackSize;
                        int j = stack.getItemDamage();
                        stack.damageItem(loot, event.getEntityPlayer());
                        event.getEntityPlayer().swingArm(event.getHand());
                        event.getEntityPlayer().inventory.setInventorySlotContents(event.getEntityPlayer().inventory.currentItem, stack);
                        if (event.getEntityPlayer().capabilities.isCreativeMode) {
                            stack.stackSize = i;
                            if (stack.isItemStackDamageable()) {
                                stack.setItemDamage(j);
                            }
                        }
                        if (stack.stackSize <= 0) {
                            event.getEntityPlayer().inventory.setInventorySlotContents(event.getEntityPlayer().inventory.currentItem, null);
                            MinecraftForge.EVENT_BUS.post(new PlayerDestroyItemEvent(event.getEntityPlayer(), stack, event.getHand()));
                        }
                        if (!event.getEntityPlayer().isHandActive() && event.getEntityPlayer() instanceof EntityPlayerMP) {
                            ((EntityPlayerMP) event.getEntityPlayer()).sendContainerToPlayer(event.getEntityPlayer().inventoryContainer);
                        }
                        event.setResult(Event.Result.DENY);
                        if (!hook.worldObj.isRemote) {
                            EntityItem entityitem = new EntityItem(hook.worldObj, hook.posX, hook.posY, hook.posZ, lootList[loot]);
                            double d5 = hook.angler.posX - hook.posX;
                            double d6 = hook.angler.posY - hook.posY;
                            double d7 = hook.angler.posZ - hook.posZ;
                            double d8 = MathHelper.sqrt_double(d5 * d5 + d6 * d6 + d7 * d7);
                            double d9 = 0.1D;
                            entityitem.motionX = d5 * d9;
                            entityitem.motionY = d6 * d9 + MathHelper.sqrt_double(d8) * 0.08D;
                            entityitem.motionZ = d7 * d9;
                            hook.worldObj.spawnEntityInWorld(entityitem);
                            hook.angler.worldObj.spawnEntityInWorld(new EntityXPOrb(hook.angler.worldObj, hook.angler.posX, hook.angler.posY + 0.5D, hook.angler.posZ + 0.5D, event.getEntityPlayer().getRNG().nextInt(6) + 1));
                        }
                    }
                }
            }
    }

    @SubscribeEvent
    public void tryPlaceOre(PlayerInteractEvent.RightClickBlock event)
    {
        if(noPlaceDuplicate)
        {
            ItemStack stack = event.getEntityPlayer().inventory.getCurrentItem();
            if(stack != null && stack.hasTagCompound() && stack.getTagCompound().hasKey("NoPlacing"))
                event.setResult(Event.Result.DENY);
        }
    }

    @SubscribeEvent
    public void onHarvest(BlockEvent.HarvestDropsEvent event) {
        if (event.getHarvester() != null && !event.getWorld().isRemote) {
            int skill;
            Random random = event.getHarvester().getRNG();
            if (event.getState().getBlock() instanceof BlockOre || event.getState().getBlock() instanceof BlockRedstoneOre || ores.contains(event.getState().getBlock())) {
                skill = getSkill(event.getHarvester(), 0);
                if (!blockToCounter.containsKey(event.getState().getBlock())) {
                    blockToCounter.put(event.getState().getBlock(), blockToCounter.size());
                }
                if (!event.isSilkTouching())
                    LevelUp.incrementOreCounter(event.getHarvester(), blockToCounter.get(event.getState().getBlock()));
                if (random.nextDouble() <= skill / 200D) {
                    boolean foundBlock = false;
                    for (ItemStack stack : event.getDrops()) {
                        if (stack != null && event.getState().getBlock() == Block.getBlockFromItem(stack.getItem())) {
                            writeNoPlacing(stack);
                            stack.stackSize += 1;
                            foundBlock = true;
                            break;
                        }
                    }
                    if (!foundBlock) {
                        Item ID = event.getState().getBlock().getItemDropped(event.getState(), random, 0);
                        if (ID != null) {
                            int qutity = event.getState().getBlock().quantityDropped(event.getState(), 0, random);
                            if (qutity > 0)
                                event.getDrops().add(new ItemStack(ID, qutity, event.getState().getBlock().damageDropped(event.getState())));
                        }
                    }
                }
            } else if (event.getState().getBlock() instanceof BlockLog) {
                skill = getSkill(event.getHarvester(), 3);
                if (random.nextDouble() <= skill / 150D) {
                    ItemStack planks = null;
                    for (ItemStack stack : event.getDrops()) {
                        if (stack != null && event.getState().getBlock() == Block.getBlockFromItem(stack.getItem())) {
                            planks = getPlanks(event.getHarvester(), stack.copy());
                            if(planks != null) {
                                planks.stackSize = 2;
                                break;
                            }
                        }
                    }
                    if (planks != null)
                        event.getDrops().add(planks);
                }
                if (random.nextDouble() <= skill / 150D) {
                    event.getDrops().add(new ItemStack(Items.STICK, 2));
                }
            } else if (event.getState().getMaterial() == Material.GROUND) {
                skill = getSkill(event.getHarvester(), 11);
                if (random.nextFloat() <= skill / 200F) {
                    ItemStack[] aitemstack4 = digLoot;
                    float f = random.nextFloat();
                    if (f <= 0.002F) {
                        aitemstack4 = digLoot3;
                    } else {
                        if (f <= 0.1F) {
                            aitemstack4 = digLoot2;
                        } else if (f <= 0.4F) {
                            aitemstack4 = digLoot1;
                        }
                    }
                    removeFromList(event.getDrops(), event.getState().getBlock());
                    ItemStack itemstack = aitemstack4[random.nextInt(aitemstack4.length)];
                    final int size = itemstack.stackSize;
                    ItemStack toDrop = itemstack.copy();
                    toDrop.stackSize = 1;
                    if (toDrop.getMaxDamage() > 20) {
                        toDrop.setItemDamage(random.nextInt(80) + 20);
                    } else {
                        for (int i1 = 0; i1 < size - 1; i1++) {
                            if (random.nextFloat() < 0.5F) {
                                event.getDrops().add(toDrop.copy());
                            }
                        }
                    }
                    event.getDrops().add(toDrop);
                }
            } else if (event.getState().getBlock() instanceof BlockGravel) {
                skill = getSkill(event.getHarvester(), 11);
                if (random.nextInt(10) < skill / 5) {
                    removeFromList(event.getDrops(), event.getState().getBlock());
                    event.getDrops().add(new ItemStack(Items.FLINT));
                }
            }
        }
    }

    private void removeFromList(List<ItemStack> drops, Block block) {
        Iterator<ItemStack> itr = drops.iterator();
        while (itr.hasNext()) {
            ItemStack drop = itr.next();
            if (drop != null && block == Block.getBlockFromItem(drop.getItem())) {
                itr.remove();
            }
        }
    }

    /**
     * Convenience method to write the "no-placement" flag onto a block
     */
    private void writeNoPlacing(ItemStack toDrop) {
        if (!noPlaceDuplicate)
            return;
        NBTTagCompound tagCompound = toDrop.getTagCompound();
        if (tagCompound == null)
            tagCompound = new NBTTagCompound();
        tagCompound.setBoolean("NoPlacing", true);
        toDrop.setTagCompound(tagCompound);
    }

    /**
     * Converts a log block into craftable planks, if possible
     *
     * @return default planks if no crafting against the log is possible
     */
    private ItemStack getPlanks(EntityPlayer player, ItemStack drop) {
        InventoryCrafting craft = new ContainerPlayer(player.inventory, !player.worldObj.isRemote, player).craftMatrix;
        craft.setInventorySlotContents(1, drop);
        return CraftingManager.getInstance().findMatchingRecipe(craft, player.worldObj);
    }

    /**
     * Adds additional drops for Farming when breaking crops
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onBlockBroken(BlockEvent.BreakEvent event) {
        if (!event.getWorld().isRemote && event.getPlayer() != null) {
            if (event.getState().getBlock() instanceof BlockCrops || event.getState().getBlock() instanceof BlockStem) {//BlockNetherWart ?
                if(!((IGrowable) event.getState().getBlock()).canGrow(event.getWorld(), event.getPos(), event.getState(), false)) {//Fully grown
                    doCropDrops(event);
                }
            }else if(event.getState().getBlock() instanceof BlockMelon){
                doCropDrops(event);
            }
        }
    }

    private void doCropDrops(BlockEvent.BreakEvent event){
        Random random = event.getPlayer().getRNG();
        int skill = getSkill(event.getPlayer(), 9);
        if (random.nextInt(10) < skill / 5) {
            Item ID = event.getState().getBlock().getItemDropped(event.getState(), random, 0);
            if(ID == null){
                if(event.getState().getBlock() == Blocks.PUMPKIN_STEM){
                    ID = Items.PUMPKIN_SEEDS;
                }else if(event.getState().getBlock() == Blocks.MELON_STEM){
                    ID = Items.MELON_SEEDS;
                }
            }
            if (ID != null)
                event.getWorld().spawnEntityInWorld(new EntityItem(event.getWorld(), event.getPos().getX(), event.getPos().getY(), event.getPos().getZ(), new ItemStack(ID, 1, event.getState().getBlock().damageDropped(event.getState()))));
        }
    }

    /**
     * Register base skill data to players
     */
    @SubscribeEvent
    public void onEntityConstruct(AttachCapabilitiesEvent evt)
    {
        evt.addCapability(ClassBonus.SKILL_LOCATION, new ICapabilitySerializable<NBTTagCompound>() {
            IPlayerClass instance = LevelUpCapability.CAPABILITY_CLASS.getDefaultInstance();
            @Override
            public boolean hasCapability(Capability<?> capability, EnumFacing facing)
            {
                return capability == LevelUpCapability.CAPABILITY_CLASS;
            }

            @Override
            public <T> T getCapability(Capability<T> capability, EnumFacing facing)
            {
                return capability == LevelUpCapability.CAPABILITY_CLASS ? LevelUpCapability.CAPABILITY_CLASS.<T>cast(instance) : null;
            }

            @Override
            public NBTTagCompound serializeNBT()
            {
                return ((NBTTagCompound)LevelUpCapability.CAPABILITY_CLASS.getStorage().writeNBT(LevelUpCapability.CAPABILITY_CLASS, instance, null));
            }

            @Override
            public void deserializeNBT(NBTTagCompound tag)
            {
                LevelUpCapability.CAPABILITY_CLASS.getStorage().readNBT(LevelUpCapability.CAPABILITY_CLASS, instance, null, tag);
            }
        });
    }

    /**
     * Copy skill data when needed
     */
    @SubscribeEvent
    public void onPlayerClone(PlayerEvent.Clone event) {
        if (!event.isWasDeath() || !resetClassOnDeath || resetSkillOnDeath < 1.00F) {
            NBTTagCompound data = new NBTTagCompound();
            PlayerExtendedProperties.from(event.getOriginal()).saveNBTData(data);
            PlayerExtendedProperties.from(event.getEntityPlayer()).loadNBTData(data);
        }
    }

    /**
     * Keep track of registered ores blocks, for mining xp compatibility
     */
    @SubscribeEvent
    public void onOreRegister(OreDictionary.OreRegisterEvent event) {
        if (event.getName().startsWith("ore") && event.getOre() != null && event.getOre().getItem() != null) {
            Block ore = Block.getBlockFromItem(event.getOre().getItem());
            if (ore != Blocks.AIR && !(ore instanceof BlockOre || ore instanceof BlockRedstoneOre)) {
                ores.add(ore);
            }
        }
    }

    /**
     * Helper to get a random slot value for the fish drop list
     *
     * @return -1 if no drop is required
     */
    public static int getFishingLoot(EntityPlayer player) {
        if (player.getRNG().nextDouble() > (getSkill(player, 10) / 5) * 0.05D) {
            return -1;
        } else {
            return player.getRNG().nextInt(lootList.length);
        }
    }

    /**
     * Helper to retrieve skill points from the index
     */
    public static int getSkill(EntityPlayer player, int id) {
        return PlayerExtendedProperties.getSkillFromIndex(player, id);
    }
}

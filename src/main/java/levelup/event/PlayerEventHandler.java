package levelup.event;

import levelup.ClassBonus;
import levelup.player.IPlayerClass;
import levelup.LevelUp;
import levelup.player.PlayerExtendedProperties;
import levelup.capabilities.LevelUpCapability;
import net.minecraft.entity.Entity;
import net.minecraft.item.*;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;

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
    public static double skillPointsPerLevel = 1.0D;
    /**
     * Level at which a player can choose a class, and get its first skill points
     */
    public final static int minLevel = 4;


//    @SubscribeEvent(priority = EventPriority.LOW)
//    public void onBreak(PlayerEvent.BreakSpeed event) {
//        ItemStack itemstack = event.getEntityPlayer().getHeldItemMainhand();
//        IBlockState state = event.getState();
//        Block block = state.getBlock();
//        float speed = event.getNewSpeed();
//        if (itemstack != ItemStack.EMPTY)
//            if (oldSpeedDigging && itemstack.getItem() instanceof ItemSpade) {
//                if (block instanceof BlockDirt || block instanceof BlockGravel) {
//                    event.setNewSpeed(speed * 0.5F);
//                }
//            } else if (oldSpeedRedstone && itemstack.getItem() instanceof ItemPickaxe && block instanceof BlockRedstoneOre) {
//                event.setNewSpeed(speed / 3F);
//            }
//        if (block instanceof BlockStone || block == Blocks.COBBLESTONE || block == Blocks.OBSIDIAN || block instanceof BlockOre || ores.contains(block)) {
//            if (getSkill(event.getEntityPlayer(), 0) > 4)
//                event.setNewSpeed(speed + (getSkill(event.getEntityPlayer(), 0) / 5 * 0.3F));
//        } else if (state.getMaterial() == Material.WOOD) {
//            if (getSkill(event.getEntityPlayer(), 3) > 4)
//                event.setNewSpeed(speed + (getSkill(event.getEntityPlayer(), 3) / 5 * 0.2F));
//        }
//    }

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
        }
    }

    /**
     * Change fishing by adding some loots
     * Prevent flagged block placement
     */
//    @SubscribeEvent(priority = EventPriority.LOW)
//    public void onFishInteract(PlayerInteractEvent.RightClickItem event) {
//        if (event.getResult() != Event.Result.DENY)
//        {
//            EntityFishHook hook = event.getEntityPlayer().fishEntity;
//            if (hook != null && hook.caughtEntity == null && hook.ticksCatchable > 0) {//Not attached to some random stuff, and within the time frame for catching
//                ItemStack loot = getFishingLoot(event.getWorld(), event.getEntityPlayer());
//                if (loot != null) {
//                    ItemStack stack = event.getEntityPlayer().inventory.getCurrentItem();
//                    int i = stack.getCount();
//                    int j = stack.getItemDamage();
//                    stack.damageItem(1, event.getEntityPlayer());
//                    event.getEntityPlayer().swingArm(event.getHand());
//                    event.getEntityPlayer().inventory.setInventorySlotContents(event.getEntityPlayer().inventory.currentItem, stack);
//                    if (event.getEntityPlayer().capabilities.isCreativeMode) {
//                        stack.grow(i);
//                        if (stack.isItemStackDamageable()) {
//                            stack.setItemDamage(j);
//                        }
//                    }
//                    if (stack.getCount() <= 0) {
//                        event.getEntityPlayer().inventory.setInventorySlotContents(event.getEntityPlayer().inventory.currentItem, ItemStack.EMPTY);
//                        MinecraftForge.EVENT_BUS.post(new PlayerDestroyItemEvent(event.getEntityPlayer(), stack, event.getHand()));
//                    }
//                    if (!event.getEntityPlayer().isHandActive() && event.getEntityPlayer() instanceof EntityPlayerMP) {
//                        ((EntityPlayerMP) event.getEntityPlayer()).sendContainerToPlayer(event.getEntityPlayer().inventoryContainer);
//                    }
//                    event.setResult(Event.Result.DENY);
//                    if (!hook.world.isRemote) {
//                        EntityItem entityitem = new EntityItem(hook.world, hook.posX, hook.posY, hook.posZ, loot.copy());
//                        double d5 = hook.getAngler().posX - hook.posX;
//                        double d6 = hook.getAngler().posY - hook.posY;
//                        double d7 = hook.getAngler().posZ - hook.posZ;
//                        double d8 = MathHelper.sqrt(d5 * d5 + d6 * d6 + d7 * d7);
//                        double d9 = 0.1D;
//                        entityitem.motionX = d5 * d9;
//                        entityitem.motionY = d6 * d9 + MathHelper.sqrt(d8) * 0.08D;
//                        entityitem.motionZ = d7 * d9;
//                        hook.world.spawnEntity(entityitem);
//                        hook.getAngler().world.spawnEntity(new EntityXPOrb(hook.getAngler().world, hook.getAngler().posX, hook.getAngler().posY + 0.5D, hook.getAngler().posZ + 0.5D, event.getEntityPlayer().getRNG().nextInt(6) + 1));
//                    }
//                }
//            }
//        }
//    }

//    @SubscribeEvent
//    public void onHarvest(BlockEvent.HarvestDropsEvent event) {
//        if (event.getHarvester() != null && !event.getWorld().isRemote) {
//            int skill;
//            IBlockState state = event.getState();
//            Random random = event.getHarvester().getRNG();
//            if (PlankCache.contains(state.getBlock(), state.getBlock().damageDropped(state))) {
//                skill = getSkill(event.getHarvester(), 3);
//                if (random.nextDouble() <= skill / 150D) {
//                    ItemStack planks = PlankCache.getProduct(state.getBlock(), state.getBlock().damageDropped(state));
//                    if (planks != null)
//                        event.getDrops().add(PlankCache.getProduct(state.getBlock(), state.getBlock().damageDropped(state)).copy());
//                }
//                if (random.nextDouble() <= skill / 150D) {
//                    event.getDrops().add(new ItemStack(Items.STICK, 2));
//                }
//            } else if (state.getBlock() instanceof BlockOre || state.getBlock() instanceof BlockRedstoneOre || ores.contains(state.getBlock())) {
//                skill = getSkill(event.getHarvester(), 0);
//                if (!blockToCounter.containsKey(state.getBlock())) {
//                    blockToCounter.put(state.getBlock(), blockToCounter.size());
//                }
//                if (!event.isSilkTouching())
//                    LevelUp.incrementOreCounter(event.getHarvester(), blockToCounter.get(state.getBlock()));
//                if (random.nextDouble() <= skill / 200D) {
//                    boolean foundBlock = false;
//                    ItemStack newOre = ItemStack.EMPTY;
//                    for (ItemStack stack : event.getDrops()) {
//                        if (stack != ItemStack.EMPTY && state.getBlock() == Block.getBlockFromItem(stack.getItem())) {
//                            writeNoPlacing(stack);
//                            newOre = stack.copy();
//                            foundBlock = true;
//                            break;
//                        }
//                    }
//                    if(newOre != ItemStack.EMPTY)
//                        event.getDrops().add(newOre);
//                    if (!foundBlock) {
//                        Item ID = state.getBlock().getItemDropped(state, random, event.getFortuneLevel());
//                        if (ID != null) {
//                            int qutity = state.getBlock().quantityDropped(state, event.getFortuneLevel(), random);
//                            if (qutity > 0)
//                                event.getDrops().add(new ItemStack(ID, qutity, state.getBlock().damageDropped(state)));
//                        }
//                    }
//                }
//                else if (LevelUp.oreNoPlace) {
//                    for (ItemStack stack : event.getDrops()) {
//                        if (stack != ItemStack.EMPTY && state.getBlock() == Block.getBlockFromItem(stack.getItem())) {
//                            writeNoPlacing(stack);
//                            break;
//                        }
//                    }
//                }
//            } else if (!event.isSilkTouching()) {
//                skill = getSkill(event.getHarvester(), 11);
//                if(state.getMaterial() == Material.GROUND) {
//                    if (random.nextFloat() <= skill / 200F) {
//                        ItemStack loot = getDigLoot(event.getWorld(), event.getHarvester());
//                        if (loot != null) {
//                            removeFromList(event.getDrops(), state.getBlock());
//                            ItemStack toDrop = loot.copy();
//                            event.getDrops().add(toDrop);
//                        }
//                    }
//                }
//                else if(state.getBlock() instanceof BlockGravel) {
//                    if (random.nextInt(10) < skill / 5) {
//                        removeFromList(event.getDrops(), state.getBlock());
//                        event.getDrops().add(new ItemStack(Items.FLINT));
//                    }
//                }
//            }
//        }
//    }

    /**
     * Converts a log block into craftable planks, if possible
     *
     * @return default planks if no crafting against the log is possible
     */
//    private ItemStack getPlanks(EntityPlayer player, ItemStack drop) {
//        InventoryCrafting craft = new ContainerPlayer(player.inventory, !player.world.isRemote, player).craftMatrix;
//        craft.setInventorySlotContents(1, drop);
//        return CraftingManager.findMatchingRecipe(craft, player.world).getRecipeOutput();
//    }

    /**
     * Adds additional drops for Farming when breaking crops
     */
//    @SubscribeEvent(priority = EventPriority.LOWEST)
//    public void onBlockBroken(BlockEvent.BreakEvent event) {
////        if (!event.getWorld().isRemote && event.getPlayer() != null) {
////            if (event.getState().getBlock() instanceof BlockCrops || event.getState().getBlock() instanceof BlockStem) {//BlockNetherWart ?
////                if(!((IGrowable) event.getState().getBlock()).canGrow(event.getWorld(), event.getPos(), event.getState(), false)) {//Fully grown
////                    doCropDrops(event);
////                }
////            }else if(event.getState().getBlock() instanceof BlockMelon){
////                doCropDrops(event);
////            }
////        }
//    }

//    private void doCropDrops(BlockEvent.BreakEvent event){
//        Random random = event.getPlayer().getRNG();
//        int skill = getSkill(event.getPlayer(), 9);
//        if (random.nextInt(10) < skill / 5) {
//            Item ID = event.getState().getBlock().getItemDropped(event.getState(), random, 0);
//            if(ID == null){
//                if(event.getState().getBlock() == Blocks.PUMPKIN_STEM){
//                    ID = Items.PUMPKIN_SEEDS;
//                }else if(event.getState().getBlock() == Blocks.MELON_STEM){
//                    ID = Items.MELON_SEEDS;
//                }
//            }
//            if (ID != null)
//                event.getWorld().spawnEntity(new EntityItem(event.getWorld(), event.getPos().getX(), event.getPos().getY(), event.getPos().getZ(), new ItemStack(ID, 1, event.getState().getBlock().damageDropped(event.getState()))));
//        }
//    }

    /**
     * Register furnace capability
     */
//    @SubscribeEvent
//    public void registerFurnaceCap(AttachCapabilitiesEvent<TileEntity> evt) {
//        if(evt.getObject() instanceof TileEntityFurnace) {
//            final TileEntityFurnace furnace = (TileEntityFurnace)evt.getObject();
//            evt.addCapability(ClassBonus.FURNACE_LOCATION, new ICapabilitySerializable<NBTTagCompound>() {
//                IProcessor instance = new CapabilityFurnace(furnace);
//
//                @Override
//                public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
//                    return capability == LevelUpCapability.MACHINE_PROCESSING;
//                }
//
//                @Override
//                public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
//                    return capability == LevelUpCapability.MACHINE_PROCESSING ? LevelUpCapability.MACHINE_PROCESSING.<T>cast(instance) : null;
//                }
//
//                @Override
//                public NBTTagCompound serializeNBT() {
//                    return ((NBTTagCompound) LevelUpCapability.MACHINE_PROCESSING.getStorage().writeNBT(LevelUpCapability.MACHINE_PROCESSING, instance, null));
//                }
//
//                @Override
//                public void deserializeNBT(NBTTagCompound tag) {
//                    LevelUpCapability.MACHINE_PROCESSING.getStorage().readNBT(LevelUpCapability.MACHINE_PROCESSING, instance, null, tag);
//                }
//            });
//        }
//        else if(evt.getObject() instanceof TileEntityBrewingStand) {
//            final TileEntityBrewingStand stand = (TileEntityBrewingStand)evt.getObject();
//            evt.addCapability(ClassBonus.FURNACE_LOCATION, new ICapabilitySerializable<NBTTagCompound>() {
//                IProcessor instance = new CapabilityBrewingStand(stand);
//
//                @Override
//                public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
//                    return capability == LevelUpCapability.MACHINE_PROCESSING;
//                }
//
//                @Override
//                public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
//                    return capability == LevelUpCapability.MACHINE_PROCESSING ? LevelUpCapability.MACHINE_PROCESSING.<T>cast(instance) : null;
//                }
//
//                @Override
//                public NBTTagCompound serializeNBT() {
//                    return ((NBTTagCompound) LevelUpCapability.MACHINE_PROCESSING.getStorage().writeNBT(LevelUpCapability.MACHINE_PROCESSING, instance, null));
//                }
//
//                @Override
//                public void deserializeNBT(NBTTagCompound tag) {
//                    LevelUpCapability.MACHINE_PROCESSING.getStorage().readNBT(LevelUpCapability.MACHINE_PROCESSING, instance, null, tag);
//                }
//            });
//        }
//    }

    /**
     * Register base skill data to players
     */
    @SubscribeEvent
    public void onEntityConstruct(AttachCapabilitiesEvent<Entity> evt)
    {
        if(evt.getObject() instanceof EntityPlayer) {
            evt.addCapability(ClassBonus.SKILL_LOCATION, new ICapabilitySerializable<NBTTagCompound>() {
                IPlayerClass instance = LevelUpCapability.CAPABILITY_CLASS.getDefaultInstance();

                @Override
                public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
                    return capability == LevelUpCapability.CAPABILITY_CLASS;
                }

                @Override
                public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
                    return capability == LevelUpCapability.CAPABILITY_CLASS ? LevelUpCapability.CAPABILITY_CLASS.<T>cast(instance) : null;
                }

                @Override
                public NBTTagCompound serializeNBT() {
                    return ((NBTTagCompound) LevelUpCapability.CAPABILITY_CLASS.getStorage().writeNBT(LevelUpCapability.CAPABILITY_CLASS, instance, null));
                }

                @Override
                public void deserializeNBT(NBTTagCompound tag) {
                    LevelUpCapability.CAPABILITY_CLASS.getStorage().readNBT(LevelUpCapability.CAPABILITY_CLASS, instance, null, tag);
                }
            });
        }
    }

    /**
     * Copy skill data when needed
     */
    @SubscribeEvent
    public void onPlayerClone(PlayerEvent.Clone event) {
        if (!event.isWasDeath() || !resetClassOnDeath || resetSkillOnDeath < 1.00F) {
            System.out.println("Loading check...");
            NBTTagCompound data = new NBTTagCompound();
            PlayerExtendedProperties.from(event.getOriginal()).saveNBTData(data);
            PlayerExtendedProperties.from(event.getEntityPlayer()).loadNBTData(data);
        }
    }

    /**
     * Keep track of registered ores blocks, for mining xp compatibility
     */
//    public static void registerOres() {
//        for(String ore : OreDictionary.getOreNames()) {
//            if(ore.startsWith("ore")) {
//                if(OreDictionary.getOres(ore) != null && !OreDictionary.getOres(ore).isEmpty()) {
//                    for(ItemStack stack : OreDictionary.getOres(ore)) {
//                        if(stack.getItem() instanceof ItemBlock) {
//                            Block block = ((ItemBlock)stack.getItem()).getBlock();
//                            if(!(block instanceof BlockOre) && !(block instanceof BlockRedstoneOre)) {
//                                if(!ores.contains(block))
//                                    ores.add(block);
//                            }
//                        }
//                    }
//                }
//            }
//        }
//    }

    /**
     * Helper to get a random slot value for the fish drop list
     *
     * @return null if no drop is required
     */
//    public static ItemStack getFishingLoot(World world, EntityPlayer player) {
//        if(!world.isRemote) {
//            if (player.getRNG().nextDouble() <= (getSkill(player, 10) / 5) * 0.05D) {
//                LootContext.Builder build = new LootContext.Builder((WorldServer) world);
//                build.withLuck(/*(float) EnchantmentHelper.getLuckOfSeaModifier(player) + */player.getLuck());
//                return world.getLootTableManager().getLootTableFromLocation(fishingLoot).generateLootForPools(player.getRNG(), build.build()).get(0);
//            }
//        }
//        return null;
//    }

}

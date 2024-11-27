package levelup.event;

import levelup.ClassBonus;
import levelup.player.IPlayerClass;
import levelup.LevelUp;
import levelup.player.PlayerExtendedProperties;
import levelup.capabilities.LevelUpCapability;
import net.minecraft.entity.Entity;
import net.minecraft.item.*;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;

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


    // CODE FOR MIGHT IMPROVING BARE-HANDED BLOCK BREAK SPEED
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onBreak(PlayerEvent.BreakSpeed event) {
        EntityPlayer player = event.getEntityPlayer();
        ItemStack itemstack = player.getHeldItemMainhand();
        if (itemstack.isEmpty() && LevelUp.getMight(player) > 0) {
            float speed = event.getNewSpeed(); // This is the original breakspeed
            float might = (float)LevelUp.getMight(event.getEntityPlayer());
            float breakSpeedMultiplier = 1.0F + (might*0.25F); // 25%(?) bonus break speed per might
            event.setNewSpeed(speed*breakSpeedMultiplier);
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
                PlayerExtendedProperties.getClassOfPlayer((EntityPlayer) event.getEntityLiving()).setPlayerClass((byte) 0);
            }
            if (resetSkillOnDeath > 0.00F) {
                PlayerExtendedProperties.getClassOfPlayer((EntityPlayer) event.getEntityLiving()).takeSkillFraction(resetSkillOnDeath);
            }
        }
    }

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
            PlayerExtendedProperties.getClassOfPlayer(event.getOriginal()).saveNBTData(data);
            PlayerExtendedProperties.getClassOfPlayer(event.getEntityPlayer()).loadNBTData(data);
        }
    }
}

package com.lumberjacksparrow.leveluprpg.event;

import com.lumberjacksparrow.leveluprpg.ClassBonus;
import com.lumberjacksparrow.leveluprpg.ConfigHandler;
import com.lumberjacksparrow.leveluprpg.LevelUpRPG;
import com.lumberjacksparrow.leveluprpg.player.IPlayerClass;
import com.lumberjacksparrow.leveluprpg.player.PlayerExtendedProperties;
import com.lumberjacksparrow.leveluprpg.capabilities.LevelUpCapability;
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

import javax.annotation.Nonnull;

public final class PlayerEventHandler {

    // CODE FOR MIGHT IMPROVING BARE-HANDED BLOCK BREAK SPEED
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onBreak(PlayerEvent.BreakSpeed event) {
        EntityPlayer player = event.getEntityPlayer();
        ItemStack itemstack = player.getHeldItemMainhand();
        if (itemstack.isEmpty() && LevelUpRPG.getMight(player) > 0) {
            float speed = event.getNewSpeed(); // This is the original breakspeed
            float might = (float) LevelUpRPG.getMight(event.getEntityPlayer());
            float breakSpeedMultiplier = (float) (1.0F + (might*ConfigHandler.SKILL_CONFIG.mightBonusBreakSpeed)); // 25%(?) bonus break speed per might
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
            if (ConfigHandler.CLASSES_CONFIG.resetClassOnDeath) {
                PlayerExtendedProperties.getFrom((EntityPlayer) event.getEntityLiving()).setPlayerClass((byte) 0, (EntityPlayer) event.getEntityLiving());
            }
            if (ConfigHandler.SKILL_CONFIG.resetSkillOnDeath > 0) {
                PlayerExtendedProperties.getFrom((EntityPlayer) event.getEntityLiving()).takeSkillFraction(ConfigHandler.SKILL_CONFIG.resetSkillOnDeath);
            }
        }
    }


    /**
     * Register base skill data to players
     */
    @SubscribeEvent
    public void onEntityConstruct(AttachCapabilitiesEvent<Entity> evt)
    {
        if(evt.getObject() instanceof EntityPlayer) {
            evt.addCapability(ClassBonus.SKILL_LOCATION, new ICapabilitySerializable<NBTTagCompound>() {
                final IPlayerClass instance = LevelUpCapability.CAPABILITY_CLASS.getDefaultInstance();

                @Override
                public boolean hasCapability(@Nonnull Capability<?> capability, EnumFacing facing) {
                    return capability == LevelUpCapability.CAPABILITY_CLASS;
                }

                @Override
                public <T> T getCapability(@Nonnull Capability<T> capability, EnumFacing facing) {
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
        if (!event.isWasDeath() || !ConfigHandler.CLASSES_CONFIG.resetClassOnDeath || ConfigHandler.SKILL_CONFIG.resetSkillOnDeath < 100) {
            System.out.println("Loading check...");
            NBTTagCompound data = new NBTTagCompound();
            PlayerExtendedProperties.getFrom(event.getOriginal()).saveNBTData(data);
            PlayerExtendedProperties.getFrom(event.getEntityPlayer()).loadNBTData(data);
        }
    }
}

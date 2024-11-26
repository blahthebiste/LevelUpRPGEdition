package levelup.event;

import levelup.ClassBonus;
import levelup.LevelUp;
import levelup.player.IPlayerClass;
import levelup.player.PlayerExtendedProperties;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.item.ItemShield;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSourceIndirect;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent;

import java.util.logging.Level;

public final class FightEventHandler {
    public static final FightEventHandler INSTANCE = new FightEventHandler();

    private FightEventHandler() {
    }

    @SubscribeEvent
    public void onCalcCrit(CriticalHitEvent event) {
        EntityPlayer entityPlayer = event.getEntityPlayer();
        int luck = LevelUp.getLuck(entityPlayer);
        float critMultiplier = event.getDamageModifier();
//        entityPlayer.sendMessage(new TextComponentString("Current mult = "+ critMultiplier));
        // Bonus random crit chance based on Luck stat:
        if (entityPlayer.getRNG().nextDouble() <= luck / 100D) { // 1% per luck
            event.setResult(Event.Result.ALLOW);
        }
        // If a random crit or normal crit was active, apply bonus crit damage based on Sneak stat
        if(event.getResult().equals(Event.Result.ALLOW) || event.isVanillaCritical()) {
            int sneak = LevelUp.getStealth(entityPlayer);
            critMultiplier = 1.5F + (0.075F*sneak); // +7.5% crit multiplier per point
//            entityPlayer.sendStatusMessage(new TextComponentString("Final mult = "+ critMultiplier), true);
            event.setDamageModifier(critMultiplier);
        }
    }

    @SubscribeEvent
    public void onAttacked(LivingDamageEvent event) {
        float damage = event.getAmount();
        // CODE FOR REDUCING FALL DAMAGE BASED ON FINESSE:
        if (event.getEntityLiving() instanceof EntityPlayer && event.getSource().damageType.equals("fall")) {
            EntityPlayer player = (EntityPlayer) event.getEntityLiving();
            float finesse = (float) LevelUp.getFinesse(player);
            damage -= (finesse/5.0F);
        }
        event.setAmount(damage);
    }

    @SubscribeEvent
    public void onHurting(LivingHurtEvent event) {
        DamageSource damagesource = event.getSource();
        float damage = event.getAmount();
        if (damagesource.getTrueSource() instanceof EntityPlayer) {
            EntityPlayer entityplayer = (EntityPlayer) damagesource.getTrueSource();
            if (damagesource instanceof EntityDamageSourceIndirect) {
                // CODE TO SCALE ARROW DAMAGE BASED ON FINESSE??
//                if (!damagesource.damageType.equals("arrow")) {
//                    damage *= 1.0F + LevelUp.getFinesse(entityplayer) / 100F;
//                }
                // CODE FOR RANGED SNEAK ATTACKS:
                if (getDistance(event.getEntityLiving(), entityplayer) < 256F && entityplayer.isSneaking() && !canSeePlayer(event.getEntityLiving()) && !entityIsFacing(event.getEntityLiving(), entityplayer)) {
                    damage *= 1.5F;
                    entityplayer.sendStatusMessage(new TextComponentTranslation("sneak.attack", 1.5), true);
                }
            } else {
                // CODE FOR MELEE SNEAK ATTACKS:
                if (entityplayer.isSneaking() && !canSeePlayer(event.getEntityLiving()) && !entityIsFacing(event.getEntityLiving(), entityplayer)) {
                    damage *= 2.0F;
                    entityplayer.sendStatusMessage(new TextComponentTranslation("sneak.attack", 2), true);
                }
            }
        }
        event.setAmount(damage);
    }

    private boolean isBlocking(EntityPlayer player)
    {
        return player.isHandActive() && player.getActiveItemStack() != ItemStack.EMPTY && player.getActiveItemStack().getItem() instanceof ItemShield;
    }

    // MORE SNEAK CALCULATIONS:
    @SubscribeEvent
    public void onTargetSet(LivingSetAttackTargetEvent event) {
        if (event.getTarget() instanceof EntityPlayer && event.getEntityLiving() instanceof EntityMob) { // Maybe don't limit to just mobs?
            if (event.getTarget().isSneaking() && !entityHasVisionOf(event.getEntityLiving(), (EntityPlayer) event.getTarget())
                    && event.getEntityLiving().getRevengeTimer() != event.getEntityLiving().ticksExisted) {
                ((EntityMob) event.getEntityLiving()).setAttackTarget(null);
            }
        }
    }


    public static boolean canSeePlayer(EntityLivingBase entityLiving) {
        EntityPlayer entityplayer = entityLiving.world.getClosestPlayerToEntity(entityLiving, 16D);
        return entityplayer != null && entityLiving.canEntityBeSeen(entityplayer) && (!entityplayer.isSneaking() || entityHasVisionOf(entityLiving, entityplayer));
    }

    public static float getDistance(EntityLivingBase entityLiving, EntityLivingBase entityliving1) {
        return MathHelper.floor((entityliving1.posX - entityLiving.posX) * (entityliving1.posX - entityLiving.posX) + (entityliving1.posZ - entityLiving.posZ)
                * (entityliving1.posZ - entityLiving.posZ));
    }

    @SuppressWarnings("UnusedDeclaration")
    public static float getPointDistance(double d, double d1, double d2, double d3) {
        return MathHelper.floor((d2 - d) * (d2 - d) + (d3 - d1) * (d3 - d1));
    }

    public static boolean compareAngles(float f, float f1, float f2) {
        if (MathHelper.abs(f - f1) < f2) {
            return true;
        }
        if (f + f2 >= 360F) {
            if ((f + f2) - 360F > f1) {
                return true;
            }
        }
        if (f1 + f2 >= 360F) {
            if ((f1 + f2) - 360F > f) {
                return true;
            }
        }
        return false;
    }

    // Sneak stat affects whether a player can be seen
    public static boolean entityHasVisionOf(EntityLivingBase entityLiving, EntityPlayer player) {
        if (entityLiving == null || player == null) {
            return false;
        }
        // CALCULATIONS FOR HOW STEALTH STAT AFFECTS AGGRO RANGE OF ENEMIES:
        float mobAggroRange = (float)entityLiving.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).getAttributeValue();
        float upClose = mobAggroRange - 1F; // No matter the mob's aggro range, max stealth lets you get up to 1 space away.
        float increment = upClose / ClassBonus.getMaxSkillPoints() ; // Also scales to max skill level.
        if (getDistance(entityLiving, player) > mobAggroRange - ((float)LevelUp.getStealth(player)) * increment) {
            return false;
        }
        return entityLiving.canEntityBeSeen(player) && entityIsFacing(player, entityLiving);
    }

    // Allow backstabs for sneak attacks
    public static boolean entityIsFacing(EntityLivingBase entityLiving, EntityLivingBase entityliving1) {
        if (entityLiving == null || entityliving1 == null) {
            return false;
        }
        float f = -(float) (entityliving1.posX - entityLiving.posX);
        float f1 = (float) (entityliving1.posZ - entityLiving.posZ);
        float f2 = entityLiving.rotationYaw;
        if (f2 < 0.0F) {
            float f3 = (MathHelper.floor(MathHelper.abs(f2) / 360F) + 1.0F) * 360F;
            f2 = f3 + f2;
        } else {
            while (f2 > 360F) {
                f2 -= 360F;
            }
        }
        float f4 = (float) ((Math.atan2(f, f1) * 180F) / Math.PI);
        if (f < 0.0F) {
            f4 = 360F + f4;
        }
        return compareAngles(f2, f4, 22.5F);
    }
}

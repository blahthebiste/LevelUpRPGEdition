package com.lumberjacksparrow.leveluprpg.event;

import com.lumberjacksparrow.leveluprpg.ConfigHandler;
import com.lumberjacksparrow.leveluprpg.LevelUpRPG;
import com.lumberjacksparrow.leveluprpg.mixin.ArrowStackAccessMixin;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
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

import static com.lumberjacksparrow.leveluprpg.player.PlayerExtendedProperties.getFrom;
import static java.util.Objects.isNull;

public final class FightEventHandler {
    public static final FightEventHandler INSTANCE = new FightEventHandler();

    @SubscribeEvent
    public void onCalcCrit(CriticalHitEvent event) {
        EntityPlayer entityPlayer = event.getEntityPlayer();
        int luck = LevelUpRPG.getLuck(entityPlayer);
        // Bonus random crit chance based on Luck stat:
        if (entityPlayer.getRNG().nextDouble() <= luck* ConfigHandler.SKILL_CONFIG.luckBonusCritChance) { // 1% per luck
            event.setResult(Event.Result.ALLOW);
        }
        // Rogues always crit while sneaking
        if(entityPlayer.isSneaking() && "rogue".equalsIgnoreCase(getFrom(entityPlayer).getClassName())) {
            event.setResult(Event.Result.ALLOW);
        }
        // If a random crit or normal crit was active, apply bonus crit damage based on Sneak stat
        if(event.getResult().equals(Event.Result.ALLOW) || event.isVanillaCritical()) {
            int sneak = LevelUpRPG.getStealth(entityPlayer);
            float critMultiplier = (float) (ConfigHandler.GENERAL_CONFIG.baseCritMult + (ConfigHandler.SKILL_CONFIG.sneakCritMult *sneak)); // +7.5% crit multiplier per point
            event.setDamageModifier(critMultiplier);
        }
    }

    @SubscribeEvent
    public void onAttacked(LivingDamageEvent event) {
        float damage = event.getAmount();
        // CODE FOR REDUCING FALL DAMAGE BASED ON FINESSE:
        if (event.getEntityLiving() instanceof EntityPlayer && event.getSource().damageType.equals("fall")) {
            EntityPlayer player = (EntityPlayer) event.getEntityLiving();
            float finesse = (float) LevelUpRPG.getFinesse(player);
            damage -= (float) (finesse * ConfigHandler.SKILL_CONFIG.finesseFallDamageReduction);
            if(damage < 0.0F) damage = 0.0F;
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
//                    damage *= 1.0F + LevelUpRPG.getFinesse(entityplayer) / 100F;
//                }
                // CODE FOR RANGED SNEAK ATTACKS:
                if (getDistance(event.getEntityLiving(), entityplayer) < 256F && entityplayer.isSneaking() && cannotSeePlayer(event.getEntityLiving()) && entityIsBehind(event.getEntityLiving(), entityplayer)) {
                    damage *= (float) ConfigHandler.GENERAL_CONFIG.rangedSneakAttackMult;
                    entityplayer.sendStatusMessage(new TextComponentTranslation("sneak.attack", ConfigHandler.GENERAL_CONFIG.rangedSneakAttackMult), true);
                }
                // Archer's returning arrow skill:
                if(damagesource.isProjectile() && "archer".equalsIgnoreCase(getFrom(entityplayer).getClassName())) {
                    Entity ent = damagesource.getImmediateSource();
                    if(ent instanceof EntityArrow) {
                        EntityArrow arrow = (EntityArrow) ent;
                        ItemStack arrowStack = ((ArrowStackAccessMixin)arrow).invokeGetArrowStack();
                        entityplayer.inventory.addItemStackToInventory(arrowStack);
                    }
                }
            } else {
                // CODE FOR MELEE SNEAK ATTACKS:
                if (entityplayer.isSneaking() && cannotSeePlayer(event.getEntityLiving()) && entityIsBehind(event.getEntityLiving(), entityplayer)) {
                    damage *= (float) ConfigHandler.GENERAL_CONFIG.meleeSneakAttackMult;
                    entityplayer.sendStatusMessage(new TextComponentTranslation("sneak.attack", ConfigHandler.GENERAL_CONFIG.meleeSneakAttackMult), true);
                }
            }
        }
        event.setAmount(damage);
    }

    // Berserker: on melee kill, regain some HP.
    @SubscribeEvent
    public void onLivingDeath(LivingDeathEvent event) {
        if(isNull(event) || isNull(event.getSource()) || isNull(event.getSource().getTrueSource())) {
            return;
        }
        if(event.getSource().isProjectile() || event.getSource().isMagicDamage()) {
            // Player did not use melee weapon for this kill, so do nothing
            return;
        }
        Entity killer = event.getSource().getTrueSource();
        if(killer instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer)killer;
            if("berserker".equalsIgnoreCase(getFrom(player).getClassName())){
                // The player was a berserker and killed with melee; give them 6 healing
                player.heal((float) ConfigHandler.CLASSES_CONFIG.berserkerHealOnKill);
            }
        }
    }

    // MORE SNEAK CALCULATIONS:
    @SubscribeEvent
    public void onTargetSet(LivingSetAttackTargetEvent event) {
        if (event.getTarget() instanceof EntityPlayer && event.getEntityLiving() instanceof EntityMob) { // Maybe don't limit to just mobs?
            if (event.getTarget().isSneaking() && withinDetectionRange(event.getEntityLiving(), (EntityPlayer) event.getTarget())
                    && event.getEntityLiving().getRevengeTimer() != event.getEntityLiving().ticksExisted) {
                ((EntityMob) event.getEntityLiving()).setAttackTarget(null);
            }
        }
    }


    public static boolean cannotSeePlayer(EntityLivingBase entityLiving) {
        EntityPlayer entityplayer = entityLiving.world.getClosestPlayerToEntity(entityLiving, 16D);
        return entityplayer == null || !entityLiving.canEntityBeSeen(entityplayer) || (entityplayer.isSneaking() && withinDetectionRange(entityLiving, entityplayer));
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
            return (f1 + f2) - 360F > f;
        }
        return false;
    }

    // Sneak stat affects whether a player can be seen
    public static boolean withinDetectionRange(EntityLivingBase entityLiving, EntityPlayer player) {
        if (entityLiving == null || player == null) {
            return true;
        }
        // CALCULATIONS FOR HOW STEALTH STAT AFFECTS AGGRO RANGE OF ENEMIES:
        float mobAggroRange = (float)entityLiving.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).getAttributeValue();
        if (getDistance(entityLiving, player) > mobAggroRange - (LevelUpRPG.getStealth(player)*ConfigHandler.SKILL_CONFIG.sneakAggroRangeReduction)) { // Changed stealth to direct reduction.
            return true;
        }
        return !entityLiving.canEntityBeSeen(player) || entityIsBehind(player, entityLiving);
    }

    // Allow backstabs for sneak attacks
    public static boolean entityIsBehind(EntityLivingBase entityLiving, EntityLivingBase entityliving1) {
        if (entityLiving == null || entityliving1 == null) {
            return true;
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
        return !compareAngles(f2, f4, 22.5F);
    }
}

package levelup.event;

import levelup.player.PlayerExtendedProperties;
import net.minecraft.item.ItemShield;
import net.minecraft.item.ItemStack;
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

public final class FightEventHandler {
    public static final FightEventHandler INSTANCE = new FightEventHandler();

    private FightEventHandler() {
    }

    @SubscribeEvent
    public void onHurting(LivingHurtEvent event) {
        DamageSource damagesource = event.getSource();
        float i = event.getAmount();
        if (damagesource.getEntity() instanceof EntityPlayer) {
            EntityPlayer entityplayer = (EntityPlayer) damagesource.getEntity();
            if (damagesource instanceof EntityDamageSourceIndirect) {
                if (!damagesource.damageType.equals("arrow")) {
                    i *= 1.0F + BowEventHandler.getArcherSkill(entityplayer) / 100F;
                }
                if (getDistance(event.getEntityLiving(), entityplayer) < 256F && entityplayer.isSneaking() && !canSeePlayer(event.getEntityLiving()) && !entityIsFacing(event.getEntityLiving(), entityplayer)) {
                    i *= 1.5F;
                    entityplayer.sendStatusMessage(new TextComponentTranslation("sneak.attack", 1.5), true);
                }
            } else {
                if (entityplayer.getHeldItemMainhand() != ItemStack.EMPTY) {
                    int j = getSwordSkill(entityplayer);
                    if (entityplayer.getRNG().nextDouble() <= j / 200D)
                        i *= 2.0F;
                    i *= 1.0F + j / 5 / 20F;
                }
                if (entityplayer.isSneaking() && !canSeePlayer(event.getEntityLiving()) && !entityIsFacing(event.getEntityLiving(), entityplayer)) {
                    i *= 2.0F;
                    entityplayer.sendStatusMessage(new TextComponentTranslation("sneak.attack", 2), true);
                }
            }
        }
        if (event.getEntityLiving() instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) event.getEntityLiving();
            int j = getDefenseSkill(player);
            if (!damagesource.isUnblockable())
                i *= 1.0F - j / 5 / 20F;
            if (isBlocking(player) && player.getRNG().nextFloat() < j / 100F) {
                i *= 0F;
            }
        }
        event.setAmount(i);
    }

    private boolean isBlocking(EntityPlayer player)
    {
        return player.isHandActive() && player.getActiveItemStack() != ItemStack.EMPTY && player.getActiveItemStack().getItem() instanceof ItemShield;
    }

    @SubscribeEvent
    public void onTargetSet(LivingSetAttackTargetEvent event) {
        if (event.getTarget() instanceof EntityPlayer && event.getEntityLiving() instanceof EntityMob) {
            if (event.getTarget().isSneaking() && !entityHasVisionOf(event.getEntityLiving(), (EntityPlayer) event.getTarget())
                    && event.getEntityLiving().getRevengeTimer() != event.getEntityLiving().ticksExisted) {
                ((EntityMob) event.getEntityLiving()).setAttackTarget(null);
            }
        }
    }

    private int getDefenseSkill(EntityPlayer player) {
        return PlayerExtendedProperties.getSkillFromIndex(player, 2);
    }

    private int getSwordSkill(EntityPlayer player) {
        return PlayerExtendedProperties.getSkillFromIndex(player, 1);
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

    public static boolean entityHasVisionOf(EntityLivingBase entityLiving, EntityPlayer player) {
        if (entityLiving == null || player == null) {
            return false;
        }
        if (getDistance(entityLiving, player) > 256F - PlayerExtendedProperties.from(player).getSkillFromIndex("Sneaking") / 5 * 12.8F) {
            return false;
        }
        return entityLiving.canEntityBeSeen(player) && entityIsFacing(player, entityLiving);
    }

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

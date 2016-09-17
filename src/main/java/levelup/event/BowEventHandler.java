package levelup.event;

import levelup.player.PlayerExtendedProperties;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraftforge.event.entity.player.ArrowNockEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;

public final class BowEventHandler {
    public static final BowEventHandler INSTANCE = new BowEventHandler();

    private BowEventHandler() {
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onSpawn(EntityJoinWorldEvent event) {
        if (event.getEntity() instanceof EntityArrow) {
            EntityArrow arrow = (EntityArrow) event.getEntity();
            if (arrow.shootingEntity instanceof EntityPlayer) {
                int archer = getArcherSkill((EntityPlayer) arrow.shootingEntity);
                if (archer != 0) {
                    arrow.motionX *= 1.0F + archer / 100F;
                    arrow.motionY *= 1.0F + archer / 100F;
                    arrow.motionZ *= 1.0F + archer / 100F;
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onBowUse(ArrowNockEvent event)
    {
        int archer = getArcherSkill(event.getEntityPlayer());
        if(archer > 4) {
            event.getEntityPlayer().setActiveHand(event.getHand());
            setItemUseCount(event.getEntityPlayer());
            event.setAction(new ActionResult<ItemStack>(EnumActionResult.SUCCESS, event.getBow()));
        }
    }

    public static int getArcherSkill(EntityPlayer player) {
        return PlayerExtendedProperties.getSkillFromIndex(player, 5);
    }

    private void setItemUseCount(EntityPlayer player) {
        player.activeItemStackUseCount -= getArcherSkill(player) / 5;
    }
}

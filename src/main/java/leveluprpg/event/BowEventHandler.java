package leveluprpg.event;

import leveluprpg.LevelUp;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraftforge.event.entity.player.ArrowNockEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.entity.player.EntityPlayer;

public final class BowEventHandler {
    public static final BowEventHandler INSTANCE = new BowEventHandler();

    private BowEventHandler() {
    }

    // ARROW SPEED CODE:
//    @SubscribeEvent(priority = EventPriority.LOW)
//    public void onSpawn(EntityJoinWorldEvent event) {
//        if (event.getEntity() instanceof EntityArrow) {
//            EntityArrow arrow = (EntityArrow) event.getEntity();
//            if (arrow.shootingEntity instanceof EntityPlayer) {
//                int archer = getArcherSkill((EntityPlayer) arrow.shootingEntity);
//                if (archer != 0) {
//                    arrow.motionX *= 1.0F + archer / 100F;
//                    arrow.motionY *= 1.0F + archer / 100F;
//                    arrow.motionZ *= 1.0F + archer / 100F;
//                }
//            }
//        }
//    }


    // BOW DRAW-SPEED CODE:
    @SubscribeEvent(priority = EventPriority.LOW)
    public void onBowUse(ArrowNockEvent event)
    {
        int finesse = LevelUp.getFinesse(event.getEntityPlayer());
        if(finesse > 0) {
            event.getEntityPlayer().setActiveHand(event.getHand());
            setItemUseCount(event.getEntityPlayer());
            event.setAction(new ActionResult<ItemStack>(EnumActionResult.SUCCESS, event.getBow()));
        }
    }

    // Quickens using the bow (pullback)
    private void setItemUseCount(EntityPlayer player) {
        player.activeItemStackUseCount -= (int)(((float)LevelUp.getFinesse(player))/2.5F);
    }
}

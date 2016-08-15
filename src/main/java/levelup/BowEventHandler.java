package levelup;

import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.item.EnumAction;
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
    public void onBowUse(PlayerInteractEvent.RightClickItem event) {
        if (event.getItemStack() != null && event.getItemStack().getMaxStackSize() == 1 && event.getItemStack().getItemUseAction() == EnumAction.BOW) {
            int archer = getArcherSkill(event.getEntityPlayer());
            if (archer != 0 && event.getEntityPlayer().getItemInUseCount() > archer / 5)
                event.getEntityPlayer().getActiveItemStack().getItem().onUsingTick(event.getEntityPlayer().getActiveItemStack(), event.getEntityPlayer(), event.getEntityPlayer().getItemInUseCount() - (archer - 5));
        }
    }

    public static int getArcherSkill(EntityPlayer player) {
        return PlayerExtendedProperties.getSkillFromIndex(player, 5);
    }
}

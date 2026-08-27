package com.lumberjacksparrow.leveluprpg.event;

import com.lumberjacksparrow.leveluprpg.ConfigHandler;
import com.lumberjacksparrow.leveluprpg.LevelUpRPG;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraftforge.event.entity.player.ArrowNockEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public final class BowEventHandler {
    public static final BowEventHandler INSTANCE = new BowEventHandler();

    // BOW DRAW-SPEED CODE:
    @SubscribeEvent(priority = EventPriority.LOW)
    public void onBowUse(ArrowNockEvent event)
    {
        int finesse = LevelUpRPG.getFinesse(event.getEntityPlayer());
        if(finesse > 0) {
            event.getEntityPlayer().setActiveHand(event.getHand());
            // Quickens using the bow (pullback)
            event.getEntityPlayer().activeItemStackUseCount -= (int)((LevelUpRPG.getFinesse(event.getEntityPlayer()))* ConfigHandler.SKILL_CONFIG.finesseDrawSpeedBonus);
            event.setAction(new ActionResult<>(EnumActionResult.SUCCESS, event.getBow()));
        }
    }
}

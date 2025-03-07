package com.lumberjacksparrow.leveluprpg.gui;

import com.lumberjacksparrow.leveluprpg.ClassBonus;
import com.lumberjacksparrow.leveluprpg.LevelUpRPG;
import com.lumberjacksparrow.leveluprpg.player.PlayerExtendedProperties;
import com.lumberjacksparrow.leveluprpg.event.PlayerEventHandler;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;

import java.awt.*;
import java.util.List;

public final class LevelUpHUD extends Gui {
    public static final LevelUpHUD INSTANCE = new LevelUpHUD();
    private float val, valIncr;

    private LevelUpHUD() {
        val = 0.7F;
        valIncr = 0.005F;
    }

    public void addToText(List<String> left) {
        byte playerClass = PlayerExtendedProperties.getPlayerClass(LevelUpRPG.proxy.getPlayer());
        if (playerClass != 0) {
            if (!LevelUpRPG.renderExpBar) {
                int skillXP = PlayerExtendedProperties.getClassOfPlayer(LevelUpRPG.proxy.getPlayer()).getSkillFromIndex("UnspentSkillPoints");
                if (skillXP > 0) {
                    left.add(I18n.format("hud.skill.text1", skillXP));
                }
            }
            left.add(I18n.format("hud.skill.text2", I18n.format("class" + playerClass + ".name")));
        } else if (canSelectClass()) {
            if (!LevelUpRPG.renderExpBar)
                left.add(I18n.format("hud.skill.select"));
        }
    }

    @SubscribeEvent
    public void renderLvlUpHUD(RenderGameOverlayEvent.Pre event) {
        if (LevelUpRPG.allowHUD && LevelUpRPG.proxy.getPlayer() != null) {
            if (LevelUpRPG.renderTopLeft && event.getType() == ElementType.TEXT)
                addToText(((RenderGameOverlayEvent.Text) event).getLeft());
            if (LevelUpRPG.renderExpBar && event.getType() == ElementType.EXPERIENCE)
                addToExpBar(event.getResolution());
        }
    }

//    @SubscribeEvent
//    public void onFOV(FOVUpdateEvent event){
//        if(!LevelUpRPG.changeFOV && !event.getEntity().isUser()) {
//            int skill = 0;
//            if(event.getEntity().isSneaking()){
//                skill = 2 * FMLEventHandler.getSkill(event.getEntity(), 8);
//            }else if(event.getEntity().isSprinting()){
//                skill = FMLEventHandler.getSkill(event.getEntity(), 6);
//            }
//            if(skill > 0){
//                event.setNewfov(event.getFov() - 0.5F);
//                event.setNewfov(event.getNewfov() * 1/(1.0F + skill / 100F));
//                event.setNewfov(event.getNewfov() + 0.5F);
//            }
//        }
//    }

    private void addToExpBar(ScaledResolution res) {
        val += valIncr;
        if (val >= 1.0F || val <= 0.4F) {
            valIncr *= -1F;
        }
        if (val > 1.0F) {
            val = 1.0F;
        }
        if (val < 0.4F) {
            val = 0.4F;
        }
        String text = null;
        if (canShowSkills()) {
            int skillXP = PlayerExtendedProperties.getClassOfPlayer(LevelUpRPG.proxy.getPlayer()).getSkillFromIndex("UnspentSkillPoints");
            if (skillXP > 0 && PlayerExtendedProperties.getClassOfPlayer(LevelUpRPG.proxy.getPlayer()).getSkillPoints() < getTotalSkillPoints())
                text = I18n.format("hud.skill.text1", skillXP);
        } else if (canSelectClass())
            text = I18n.format("hud.skill.select");
        int x = (res.getScaledWidth() - Minecraft.getMinecraft().fontRenderer.getStringWidth(text)) / 2;
        int y = res.getScaledHeight() - 29;
        if (text != null) {
            int col = Color.HSBtoRGB(0.2929688F, 1.0F, val) & 0xffffff;
            Minecraft.getMinecraft().fontRenderer.drawString(text, x, y, col);
        }
        Minecraft.getMinecraft().getTextureManager().bindTexture(Gui.ICONS);//Icons texture reset
    }

    public static boolean canSelectClass() {
        if (LevelUpRPG.proxy.getPlayer().experienceLevel >= PlayerEventHandler.minLevel)
            return true;
        else {
            int points = PlayerExtendedProperties.getClassOfPlayer(LevelUpRPG.proxy.getPlayer()).getSkillPoints();
            return points > PlayerEventHandler.minLevel * PlayerEventHandler.skillPointsPerLevel || points > ClassBonus.getBonusPoints();
        }
    }

    public static boolean canShowSkills() {
        return PlayerExtendedProperties.getClassOfPlayer(LevelUpRPG.proxy.getPlayer()).hasClass();
    }

    private static int getTotalSkillPoints() {
        return ClassBonus.getMaxSkillPoints() * (ClassBonus.skillNames.length - 1);
    }
}

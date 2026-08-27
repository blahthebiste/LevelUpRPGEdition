package com.lumberjacksparrow.leveluprpg.gui;

import com.lumberjacksparrow.leveluprpg.ClassBonus;
import com.lumberjacksparrow.leveluprpg.ConfigHandler;
import com.lumberjacksparrow.leveluprpg.LevelUpRPG;
import com.lumberjacksparrow.leveluprpg.player.PlayerExtendedProperties;
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
            if (!ConfigHandler.HUD_CONFIG.renderExpBar) {
                int unspentSkillPoints = PlayerExtendedProperties.getFrom(LevelUpRPG.proxy.getPlayer()).getSkillByName("UnspentSkillPoints");
                if (unspentSkillPoints > 0) {
                    left.add(I18n.format("hud.skill.text1", unspentSkillPoints));
                }
            }
            left.add(I18n.format("hud.skill.text2", I18n.format("class" + playerClass + ".name")));
        } else if (canSelectClass()) {
            if (!ConfigHandler.HUD_CONFIG.renderExpBar)
                left.add(I18n.format("hud.skill.select"));
        }
    }

    @SubscribeEvent
    public void renderLvlUpHUD(RenderGameOverlayEvent.Pre event) {
        if (ConfigHandler.HUD_CONFIG.allowHUD && LevelUpRPG.proxy.getPlayer() != null) {
            if (ConfigHandler.HUD_CONFIG.renderTopLeft && event.getType() == ElementType.TEXT)
                addToText(((RenderGameOverlayEvent.Text) event).getLeft());
            if (ConfigHandler.HUD_CONFIG.renderExpBar && event.getType() == ElementType.EXPERIENCE)
                addToExpBar(event.getResolution());
        }
    }

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
            int unspentSkillPoints = PlayerExtendedProperties.getFrom(LevelUpRPG.proxy.getPlayer()).getSkillByName("UnspentSkillPoints");
            if (unspentSkillPoints > 0 && PlayerExtendedProperties.getFrom(LevelUpRPG.proxy.getPlayer()).getTotalSkillPoints() < maxTotalSkillPoints())
                text = I18n.format("hud.skill.text1", unspentSkillPoints);
        } else if (canSelectClass())
            text = I18n.format("hud.skill.select");
        if (text != null) {
            int x = (res.getScaledWidth() - Minecraft.getMinecraft().fontRenderer.getStringWidth(text)) / 2;
            int y = res.getScaledHeight() - 29;
            int col = Color.HSBtoRGB(0.2929688F, 1.0F, val) & 0xffffff;
            Minecraft.getMinecraft().fontRenderer.drawString(text, x, y, col);
        }
        Minecraft.getMinecraft().getTextureManager().bindTexture(Gui.ICONS);//Icons texture reset
    }

    public static boolean canSelectClass() {
        if(!ConfigHandler.CLASSES_CONFIG.allowClasses) {
            return false;
        }
        if (LevelUpRPG.proxy.getPlayer().experienceLevel >= ConfigHandler.CLASSES_CONFIG.minLevel)
            return true;
        else {
            int points = PlayerExtendedProperties.getFrom(LevelUpRPG.proxy.getPlayer()).getTotalSkillPoints();
            return points > ConfigHandler.CLASSES_CONFIG.minLevel * ConfigHandler.SKILL_CONFIG.skillPointsGainPerLevel;
        }
    }

    public static boolean canShowSkills() {
        return !ConfigHandler.CLASSES_CONFIG.allowClasses || PlayerExtendedProperties.getFrom(LevelUpRPG.proxy.getPlayer()).hasClass();
    }

    private static int maxTotalSkillPoints() {
        return ConfigHandler.SKILL_CONFIG.maxPointsPerSkill * (ClassBonus.skillNames.length - 1);
    }
}

package levelup.gui;

import levelup.ClassBonus;
import levelup.LevelUp;
import levelup.player.PlayerExtendedProperties;
import levelup.SkillPacketHandler;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import static levelup.player.PlayerExtendedProperties.getPlayerClass;

public final class GuiSkills extends GuiScreen {
    private boolean closedWithButton;
    private final static int offset = 80;
    private final int[] skills = new int[ClassBonus.skillNames.length];
    private int[] skillsPrev = null;
    byte cl = -1;

    @Override
    protected void actionPerformed(GuiButton guibutton) {
        if (guibutton.id == 0) {
            closedWithButton = true;
            mc.displayGuiScreen(null);
            mc.setIngameFocus();
        } else if (guibutton.id == 100) {
            closedWithButton = false;
            mc.displayGuiScreen(null);
            mc.setIngameFocus();
        } else if (guibutton.id < 21) {
            if (getSkillOffset(skills.length - 1) > 0 && getSkillOffset(guibutton.id - 1) < ClassBonus.getMaxSkillPoints()) {
                if(guibutton.id < 7) { // Class skills
                    skills[guibutton.id + (cl * 6) - 1]++;
                }
                else { // Neutral skills
                    skills[guibutton.id - 7]++;
                }
                skills[skills.length - 1]--; // Deduct skill points
            }
        } else { // Minus buttons.
            if(guibutton.id < 27) { // Class skills
                if(skills[guibutton.id + (cl * 6) - 21] > 0) { // Only decrement down to zero
                    skills[guibutton.id + (cl * 6) - 21]--;
                    skills[skills.length - 1]++; // Refund skill points
                }
            }
            else { // Neutral skills
                if (skills[guibutton.id - 27] > 0) { // Only decrement down to zero
                    skills[guibutton.id - 27]--;
                    skills[skills.length - 1]++; // Refund skill points
                }
            }
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    public void drawScreen(int i, int j, float f) {
        drawDefaultBackground();
        String skillDescription1 = "";
        String skillDescription2 = "";
        if (cl < 0)
            cl = getPlayerClass(mc.player);
        if (cl > 0) {
            drawCenteredString(fontRenderer, I18n.format("hud.skill.text2", I18n.format("class" + cl + ".name")), width / 2, 2, 0xffffff);
        }
        if(cl == 0) {
            // Draw neutral skills only
            for (int x = 1; x <= 6; x++) {
                drawCenteredString(fontRenderer, I18n.format("skill" + x + ".name") + ": " + getSkillOffset(x-1), width / 2, 20 + 32 * (x-1), 0xffffff);
            }
        }
        else {
            // Draw neutral and class skills
            for (int x = 1; x <= 6; x++) {
                drawCenteredString(fontRenderer, I18n.format("skill" + (x + (cl*6)) + ".name") + ": " + getSkillOffset(x-1 + (cl*6)), width / 2 - offset, 20 + 32 * (x-1), 0xffffff);
                drawCenteredString(fontRenderer, I18n.format("skill" + x + ".name") + ": " + getSkillOffset(x-1), width / 2 + offset, 20 + 32 * (x-1), 0xffffff);
            }
        }
        for (Object button : buttonList) {
            int l = ((GuiButton) button).id;
            if (l < 1 || l > 99) {
                continue;
            }
            if (l > 20) {
                l -= 20;
            }
            if (((GuiButton) button).mousePressed(mc, i, j)) { //1-12 are the skills
                if(l < 7) { // Class skills
                    skillDescription1 = I18n.format("skill" + (l + (cl * 6)) + ".tooltip1");
                    skillDescription2 = I18n.format("skill" + (l + (cl * 6)) + ".tooltip2");
                }
                else { // Neutral skills
                    skillDescription1 = I18n.format("skill" + (l - 6) + ".tooltip1");
                    skillDescription2 = I18n.format("skill" + (l - 6) + ".tooltip2");
                }
            }
        }
        drawCenteredString(fontRenderer, skillDescription1, width / 2, height / 6 + 148, 0xffffff);
        drawCenteredString(fontRenderer, skillDescription2, width / 2, height / 6 + 160, 0xffffff);
        drawCenteredString(fontRenderer, I18n.format("xp.next", getExperiencePoints(mc.player)), width / 2, height / 6 + 192, 0xFFFFFF);
        super.drawScreen(i, j, f);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void initGui() {
        closedWithButton = false;
        buttonList.clear();
        updateSkillList();
        buttonList.add(new GuiButton(0, width / 2 + 96, height / 6 + 168, 96, 20, I18n.format("gui.done")));
        buttonList.add(new GuiButton(100, width / 2 - 192, height / 6 + 168, 96, 20, I18n.format("gui.cancel")));
        for (int index = 0; index < 6; index++) {
            buttonList.add(new GuiButton(1 + index, (width / 2 + 44) - offset, 15 + 32 * index, 20, 20, "+"));
            buttonList.add(new GuiButton(7 + index, width / 2 + 44 + offset, 15 + 32 * index, 20, 20, "+"));
            buttonList.add(new GuiButton(21 + index, width / 2 - 64 - offset, 15 + 32 * index, 20, 20, "-"));
            buttonList.add(new GuiButton(27 + index, (width / 2 - 64) + offset, 15 + 32 * index, 20, 20, "-"));
        }
    }

    @Override
    public void onGuiClosed() {
        if (closedWithButton && skills[skills.length - 1] != 0) {
            FMLProxyPacket packet = SkillPacketHandler.getPacket(Side.SERVER, 2, (byte) -1, skills);
            LevelUp.skillChannel.sendToServer(packet);
        }
    }

    private void updateSkillList() {
        if (skillsPrev == null) {
            skillsPrev = new int[skills.length];
            for (int i = 0; i < skills.length; i++) {
                skillsPrev[i] = PlayerExtendedProperties.getSkillFromIndex(mc.player, i);
            }
        }
    }

    private int getSkillOffset(int i) {
        return skillsPrev[i] + skills[i];
    }

    private int getExperiencePoints(EntityPlayer player)
    {
        int cap = player.xpBarCap();
        int total = (int)(player.xpBarCap() * player.experience);
        return cap - total;
    }
}

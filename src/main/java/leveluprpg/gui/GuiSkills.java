package leveluprpg.gui;

import leveluprpg.ClassBonus;
import leveluprpg.LevelUp;
import leveluprpg.player.PlayerExtendedProperties;
import leveluprpg.SkillPacketHandler;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import static leveluprpg.player.PlayerExtendedProperties.getPlayerClass;

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
            if (getTotalSkillLevelPlusPending(skills.length - 1) > 0 && getTotalSkillLevelPlusPending(guibutton.id - 1) < ClassBonus.getMaxSkillPoints()) {
//                if(guibutton.id < 7) { // Class skills
//                    skills[guibutton.id + (cl * 6) - 1]++;
//                }
//                else { // Neutral skills
                skills[guibutton.id-1]++;
//                }
                skills[skills.length - 1]--; // Deduct skill points
            }
        } else { // Minus buttons.
//            if(guibutton.id < 27) { // Class skills
//                if(skills[guibutton.id + (cl * 6) - 21] > 0) { // Only decrement down to zero
//                    skills[guibutton.id + (cl * 6) - 21]--;
//                    skills[skills.length - 1]++; // Refund skill points
//                }
//            }
//            else { // Neutral skills
            if (skills[guibutton.id - 21] > 0) { // Only decrement down to zero
                skills[guibutton.id - 21]--;
                skills[skills.length - 1]++; // Refund skill points
            }
//            }
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
//        if(cl == 0) {
            // Draw neutral skills only
        for (int x = 1; x <= skills.length - 1; x++) {
            drawString(fontRenderer, I18n.format("skill" + x + ".name") + ": " + getTotalSkillLevelPlusPending(x-1), (width / 2) - 50, 24 + 25 * (x-1), 0xffffff);
        }
//        }
//        else {
//            // Draw class skills
//            for (int x = 1; x <= 6; x++) {
////                drawCenteredString(fontRenderer, I18n.format("skill" + (x + (cl*6)) + ".name") + ": " + getSkillOffset(x-1 + (cl*6)), width / 2 - offset, 20 + 32 * (x-1), 0xffffff);
//                drawCenteredString(fontRenderer, I18n.format("skill" + x + ".name") + ": " + getSkillOffset(x-1), width / 2 + offset, 20 + 32 * (x-1), 0xffffff);
//            }
//        }
        for (Object button : buttonList) {
            int l = ((GuiButton) button).id;
            if (l < 1 || l > 99) {
                continue;
            }
            if (l > 20) {
                l -= 20;
            }
            if (((GuiButton) button).mousePressed(mc, i, j)) { //1-12 are the skills
//                if(l < 7) { // Class skills
//                    skillDescription1 = I18n.format("skill" + (l + (cl * 6)) + ".tooltip1");
//                    skillDescription2 = I18n.format("skill" + (l + (cl * 6)) + ".tooltip2");
//                }
//                else { // Neutral skills
                skillDescription1 = I18n.format("skill" + l + ".tooltip1");
                skillDescription2 = I18n.format("skill" + l + ".tooltip2");
//                }
            }
        }
        drawCenteredString(fontRenderer, skillDescription1, width / 2, height / 6 + 136, 0xffffff);
        drawCenteredString(fontRenderer, skillDescription2, width / 2, height / 6 + 148, 0xffffff);
        drawCenteredString(fontRenderer, I18n.format("xp.next", getExperiencePoints(mc.player)), width / 2, height / 6 + 192, 0xFFFFFF);
        super.drawScreen(i, j, f);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void initGui() {
        closedWithButton = false;
        buttonList.clear();
        updateSkillList();
        buttonList.add(new GuiButton(0, width / 2 + 96, height / 6 + 180, 96, 20, I18n.format("gui.done")));
        buttonList.add(new GuiButton(100, width / 2 - 192, height / 6 + 180, 96, 20, I18n.format("gui.cancel")));
        // Make +/- buttons for each skill
        for (int index = 0; index < skills.length-1; index++) {
            buttonList.add(new GuiButton(1 + index, (width / 2) + 46, 15 + 25 * index, 20, 20, "+"));
//            buttonList.add(new GuiButton(7 + index, width / 2 + 44 + offset, 15 + 32 * index, 20, 20, "+"));
            buttonList.add(new GuiButton(21 + index, (width / 2) +16, 15 + 25 * index, 20, 20, "-"));
//            buttonList.add(new GuiButton(27 + index, (width / 2 - 64) + offset, 15 + 32 * index, 20, 20, "-"));
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

    // Returns the skill level of the skill with the given ID, including pending skills.
    // Meaning, if you have 7 of a skill, and are in the process of levelling up with
    // 3 more points applied to that skill, this would return 10.
    private int getTotalSkillLevelPlusPending(int id) {
        return skillsPrev[id] + skills[id];
    }

    private int getExperiencePoints(EntityPlayer player)
    {
        int cap = player.xpBarCap();
        int total = (int)(player.xpBarCap() * player.experience);
        return cap - total;
    }
}

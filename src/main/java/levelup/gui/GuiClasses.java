package levelup.gui;

import levelup.LevelUp;
import levelup.SkillPacketHandler;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

public final class GuiClasses extends GuiScreen {
    private boolean closedWithButton = false;
    private byte cl = 0;

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    public void drawScreen(int i, int j, float f) {
        drawDefaultBackground();
        drawCenteredString(fontRenderer, I18n.format("class" + cl + ".tooltip"), width / 2, height / 6 + 148, 0xffffff);
        drawCenteredString(fontRenderer, I18n.format("gui.class.title", I18n.format("class" + cl + ".name")), width / 2, height / 6 + 174, 0xffffff);
        super.drawScreen(i, j, f);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void initGui() {
        closedWithButton = false;
        buttonList.clear();
        buttonList.add(new GuiButton(0, width / 2 + 96, height / 6 + 168, 96, 20, I18n.format("gui.done")));
        buttonList.add(new GuiButton(100, width / 2 - 192, height / 6 + 168, 96, 20, I18n.format("gui.cancel")));
        for (int j = 1; j < 7; j = j + 3) {
            for (int i = 0; i < 3; i++) {
                buttonList.add(new GuiButton(i + j, width / 2 - 160 + i * 112, 18 + 32 * (j - 1) / 3, 96, 20, I18n.format("class" + (i + j) + ".name")));
            }
        }
        // For now do Wizard separately since we don't have a number of classes divisible by 3
        buttonList.add(new GuiButton(7, width / 2 - 48, 82, 96, 20, I18n.format("class7.name")));
    }

    @Override
    public void onGuiClosed() {
        if (closedWithButton && cl != 0) {
            FMLProxyPacket packet = SkillPacketHandler.getPacket(Side.SERVER, 1, cl);
            LevelUp.classChannel.sendToServer(packet);
        }
    }

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
        } else {
            cl = (byte) guibutton.id;
        }
    }
}

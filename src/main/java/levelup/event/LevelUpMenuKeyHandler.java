package levelup.event;

import levelup.gui.GuiClasses;
import levelup.gui.GuiSkills;
import levelup.gui.LevelUpHUD;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import org.lwjgl.input.Keyboard;

public final class LevelUpMenuKeyHandler {
    public static final LevelUpMenuKeyHandler INSTANCE = new LevelUpMenuKeyHandler();
    private final KeyBinding keys = new KeyBinding("LvlUpGUI", Keyboard.KEY_L, "key.categories.gui");

    private LevelUpMenuKeyHandler() {
        ClientRegistry.registerKeyBinding(keys);
    }

    @SubscribeEvent
    public void keyDown(InputEvent.KeyInputEvent event) {
        if (keys.isKeyDown() && Minecraft.getMinecraft().currentScreen == null && Minecraft.getMinecraft().player != null) {
            if (LevelUpHUD.canShowSkills()) {
                Minecraft.getMinecraft().displayGuiScreen(new GuiSkills());
            } else if (LevelUpHUD.canSelectClass())
                Minecraft.getMinecraft().displayGuiScreen(new GuiClasses());
            else
                Minecraft.getMinecraft().player.sendStatusMessage(new TextComponentTranslation("level.invalid"), true);
        }
    }
}

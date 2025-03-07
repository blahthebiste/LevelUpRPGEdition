package com.lumberjacksparrow.leveluprpg.proxy;

import com.lumberjacksparrow.leveluprpg.LevelUpRPG;
import com.lumberjacksparrow.leveluprpg.event.LevelUpMenuKeyHandler;
import com.lumberjacksparrow.leveluprpg.gui.LevelUpHUD;

import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.MinecraftForge;

@SuppressWarnings("UnusedDeclaration")
public final class SkillClientProxy extends SkillProxy {
    @Override
    public void tryUseMUD() {
        try {
            Class.forName("mods.mud.ModUpdateDetector").getDeclaredMethod("registerMod", ModContainer.class, String.class, String.class).invoke(null,
                    FMLCommonHandler.instance().findContainerFor(LevelUpRPG.instance),
                    "https://raw.github.com/GotoLink/LevelUp/master/update.xml",
                    "https://raw.github.com/GotoLink/LevelUp/master/changelog.md"
            );
        } catch (Throwable ignored) {
        }
    }

    @Override
    public void registerGui() {
        MinecraftForge.EVENT_BUS.register(LevelUpHUD.INSTANCE);
        FMLCommonHandler.instance().bus().register(LevelUpMenuKeyHandler.INSTANCE);
    }

    @Override
    public EntityPlayer getPlayer() {
        return FMLClientHandler.instance().getClient().player;
    }

}

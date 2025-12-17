package com.lumberjacksparrow.leveluprpg.proxy;

import com.lumberjacksparrow.leveluprpg.event.LevelUpMenuKeyHandler;
import com.lumberjacksparrow.leveluprpg.gui.LevelUpHUD;

import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.MinecraftForge;

@SuppressWarnings("UnusedDeclaration")
public final class SkillClientProxy extends SkillProxy {

    @Override
    public void registerGui() {
        MinecraftForge.EVENT_BUS.register(LevelUpHUD.INSTANCE);
        MinecraftForge.EVENT_BUS.register(LevelUpMenuKeyHandler.INSTANCE);
    }

    @Override
    public EntityPlayer getPlayer() {
        return FMLClientHandler.instance().getClient().player;
    }

}

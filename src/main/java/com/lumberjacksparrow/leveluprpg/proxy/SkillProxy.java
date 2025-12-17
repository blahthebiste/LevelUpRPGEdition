package com.lumberjacksparrow.leveluprpg.proxy;

import net.minecraft.entity.player.EntityPlayer;

public class SkillProxy {

//    public void tryUseMUD() {
//    }

    public void registerGui() {
    }

    public EntityPlayer getPlayer() {
        System.out.println("DEBUG: LevelUpRPG, server side proxy getPlayer (null)");
        return null;
    }

}

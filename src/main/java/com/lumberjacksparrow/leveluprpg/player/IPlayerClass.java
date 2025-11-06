package com.lumberjacksparrow.leveluprpg.player;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

public interface IPlayerClass
{
    int getSkillFromIndex(String ID);

    byte getPlayerClass();

    String getClassName();

//    Map<String, int[]> getCounterMap();

    NBTTagCompound saveNBTData(NBTTagCompound tag);

    void loadNBTData(NBTTagCompound tag);

    void refundSkillPoints(boolean convert, EntityPlayer player);

    int getSkillPoints();

    boolean hasClass();

    void setPlayerClass(byte type, EntityPlayer player);

    void takeSkillFraction(float resetSkill);

    void addToSkill(String name, int experience, EntityPlayer player);

    int[] getPlayerData(boolean withClass);

    void setPlayerData(int[] data);
}

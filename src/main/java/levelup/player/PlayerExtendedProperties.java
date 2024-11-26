package levelup.player;

import levelup.ClassBonus;
import levelup.capabilities.LevelUpCapability;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

import java.util.HashMap;
import java.util.Map;

public final class PlayerExtendedProperties implements IPlayerClass
{
    private byte playerClass;
    private Map<String, Integer> skillMap = new HashMap<String, Integer>();
    //private Map<String, int[]> counterMap = new HashMap<String, int[]>();
    //public final static String[] counters = {"ore", "craft", "bonus"}; // TODO: use these for something else?

    public PlayerExtendedProperties() {
        for (String name : ClassBonus.skillNames)
            skillMap.put(name, 0);
//        counterMap.put(counters[0], new int[]{0, 0, 0, 0});
//        counterMap.put(counters[1], new int[]{0, 0, 0, 0});
//        counterMap.put(counters[2], new int[]{0, 0, 0});//ore bonus, craft bonus, kill bonus
    }

    @Override
    public NBTTagCompound saveNBTData(NBTTagCompound compound) {
        compound.setByte("Class", playerClass);
        for (String name : ClassBonus.skillNames) {
            compound.setInteger(name, skillMap.get(name));
        }
//        for (String cat : counters) {
//            compound.setIntArray(cat, counterMap.get(cat));
//        }
        return compound;
    }

    @Override
    public void loadNBTData(NBTTagCompound compound) {
        playerClass = compound.getByte("Class");
        for (String name : ClassBonus.skillNames) {
            skillMap.put(name, compound.getInteger(name));
        }
//        for (String cat : counters) {
//            counterMap.put(cat, compound.getIntArray(cat));
//        }
    }

    public static IPlayerClass from(EntityPlayer player) {
        return player.getCapability(LevelUpCapability.CAPABILITY_CLASS, null);
    }

    @Override
    public void addToSkill(String name, int value) {
        skillMap.put(name, skillMap.get(name) + value);
    }

    @Override
    public int getSkillFromIndex(String name) {
        return skillMap.get(name);
    }

    public static int getSkillFromIndex(EntityPlayer player, int id) {
        return from(player).getSkillFromIndex(ClassBonus.skillNames[id]);
    }

    @Override
    public int getSkillPoints() {
        int total = 0;
        for (String skill : ClassBonus.skillNames) {
            total += getSkillFromIndex(skill);
        }
        return total;
    }

    @Override
    public boolean hasClass() {
        return playerClass != 0;
    }

    public static byte getPlayerClass(EntityPlayer player) {
        return from(player).getPlayerClass();
    }

    @Override
    public byte getPlayerClass()
    {
        return playerClass;
    }

    @Override
    public void setPlayerClass(byte newClass) {
        if (newClass != playerClass) {
            ClassBonus.applyBonus(this, playerClass, newClass);
            capSkills();
            playerClass = newClass;
        }
    }

//    public static Map<String, int[]> getCounterMap(EntityPlayer player) {
//        return from(player).getCounterMap();
//    }

//    public Map<String, int[]> getCounterMap()
//    {
//        return this.counterMap;
//    }

    public void capSkills() {
        for (String name : ClassBonus.skillNames) {
            if (name.equals("UnspentSkillPoints"))
                continue;
            int j = skillMap.get(name);
            if (j > ClassBonus.getMaxSkillPoints()) {
                skillMap.put(name, ClassBonus.getMaxSkillPoints());
            }
        }
    }

    @Override
    public void takeSkillFraction(float ratio) {
        final byte clas = playerClass;
        if (clas != 0) {
            ClassBonus.applyBonus(this, clas, (byte) 0);
            playerClass = 0;
        }
        for (String name : ClassBonus.skillNames) {
            final int value = skillMap.get(name);
            int remove = (int) (value * ratio);
            if (remove > 0) {
                skillMap.put(name, value - remove);
            }
        }
        if (clas != 0) {
            ClassBonus.applyBonus(this, (byte) 0, clas);
            playerClass = clas;
        }
        capSkills();
    }

    @Override
    public void refundSkillPoints(boolean resetClass) {
        final byte clas = playerClass;
        setPlayerClass((byte) 0);
        skillMap.put("UnspentSkillPoints", getSkillPoints());
        setPlayerData(new int[ClassBonus.skillNames.length - 1]);
        if (!resetClass)
            setPlayerClass(clas);
    }

    @Override
    public void setPlayerData(int[] data) {
        for (int i = 0; i < ClassBonus.skillNames.length && i < data.length; i++) {
            skillMap.put(ClassBonus.skillNames[i], data[i]);
        }
    }

    @Override
    public int[] getPlayerData(boolean withClass) {
        int[] data = new int[ClassBonus.skillNames.length + (withClass ? 1 : 0)];
        for (int i = 0; i < ClassBonus.skillNames.length; i++)
            data[i] = getSkillFromIndex(ClassBonus.skillNames[i]);
        if (withClass)
            data[data.length - 1] = playerClass;
        return data;
    }
}

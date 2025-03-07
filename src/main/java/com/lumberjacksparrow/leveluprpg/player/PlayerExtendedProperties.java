package com.lumberjacksparrow.leveluprpg.player;

import com.lumberjacksparrow.leveluprpg.ClassBonus;
import com.lumberjacksparrow.leveluprpg.LevelUpRPG;
import com.lumberjacksparrow.leveluprpg.capabilities.LevelUpCapability;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class PlayerExtendedProperties implements IPlayerClass
{
    private byte playerClass;
    /**
     * Attribute modifier uuids
     */
    private static final UUID luckUID = UUID.fromString("4f7637c8-6106-40d0-96cb-e47f83bfa415");
    private static final UUID attackDamageUID = UUID.fromString("a4dc0b04-f78a-43f6-8805-5ebfaab10b18");
    private static final UUID maxHPUID = UUID.fromString("34dc0b04-f48a-43f6-8805-5ebfaab10b18");
    private static final UUID toughnessUID = UUID.fromString("24dc0b04-f48a-43f6-8805-5ebfaab10b18");
    private Map<String, Integer> skillMap = new HashMap<String, Integer>();

    public PlayerExtendedProperties() {
        for (String name : ClassBonus.skillNames)
            skillMap.put(name, 0);
    }

    @Override
    public NBTTagCompound saveNBTData(NBTTagCompound compound) {
        compound.setByte("Class", playerClass);
        for (String name : ClassBonus.skillNames) {
            compound.setInteger(name, skillMap.get(name));
        }
        return compound;
    }

    @Override
    public void loadNBTData(NBTTagCompound compound) {
        playerClass = compound.getByte("Class");
        for (String name : ClassBonus.skillNames) {
            skillMap.put(name, compound.getInteger(name));
        }
    }

    public static IPlayerClass getClassOfPlayer(EntityPlayer player) {
        return player.getCapability(LevelUpCapability.CAPABILITY_CLASS, null);
    }

    @Override
    public void addToSkill(String name, int value, EntityPlayer player) {
        skillMap.put(name, skillMap.get(name) + value);
        if(player == null || player.getEntityWorld().isRemote || player.getEntityWorld().getMinecraftServer() == null) {
            return;
        }
        AttributeModifier mod;
        switch(name) {
            case "Luck":
                // Luck modifier
                IAttributeInstance luckAttributeInstance = player.getEntityAttribute(SharedMonsterAttributes.LUCK);
                int luck = LevelUpRPG.getLuck(player);
                if (luck != 0) {
                    // Add luck at a 1-to-1 ratio
                    if (luckAttributeInstance.getModifier(luckUID) != null) {
                        luckAttributeInstance.removeModifier(luckUID);
                    }
                    mod = new AttributeModifier(luckUID, "BonusLuckFromSkill", luck, 0);
                    luckAttributeInstance.applyModifier(mod);
                }
                break;
            case "Might":
                // Melee attack damage modifier
                IAttributeInstance attackDamageAttributeInstance = player.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE);
                int might = LevelUpRPG.getMight(player);
                if (might != 0) {
                    // Add +0.25 melee damage per point of might
                    if (attackDamageAttributeInstance.getModifier(attackDamageUID) != null) {
                        attackDamageAttributeInstance.removeModifier(attackDamageUID);
                    }
                    mod = new AttributeModifier(attackDamageUID, "BonusMightFromSkill", might * 0.25F, 0);
                    attackDamageAttributeInstance.applyModifier(mod);
                }
                break;
            case "Vitality":
                // Max HP modifier
                int vitality = LevelUpRPG.getVitality(player);
                if (vitality != 0) {
                    IAttributeInstance maxHPAttributeInstance = player.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH);
                    IAttributeInstance toughnessAttributeInstance = player.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS);
                    // Add max HP at a 1-to-1 ratio
                    if (maxHPAttributeInstance.getModifier(maxHPUID) != null) {
                        maxHPAttributeInstance.removeModifier(maxHPUID);
                    }
                    mod = new AttributeModifier(maxHPUID, "BonusMaxHPFromSkill", vitality, 0);
                    maxHPAttributeInstance.applyModifier(mod);
                    // Toughness modifier
                    // Add 1 toughness per 10 Vitality
                    if (toughnessAttributeInstance.getModifier(toughnessUID) != null) {
                        toughnessAttributeInstance.removeModifier(toughnessUID);
                    }
                    mod = new AttributeModifier(toughnessUID, "BonusToughnessFromSkill", (vitality / 10.0F), 0);
                    toughnessAttributeInstance.applyModifier(mod);
                }
                break;
            case "Focus":
                int focus = LevelUpRPG.getFocus(player);
                if(focus != 0) {
                    // Max mana:
                    String maxManaCommand = "/setPlayerMaxMana "+player.getName()+" "+(10+(focus*10));
                    player.getEntityWorld().getMinecraftServer().commandManager.executeCommand(player.getEntityWorld().getMinecraftServer(), maxManaCommand);
                }
                break;
            default:
                break;
        }
    }


    @Override
    public int getSkillFromIndex(String name) {
        return skillMap.get(name);
    }

    public static int getSkillFromIndex(EntityPlayer player, int id) {
        return getClassOfPlayer(player).getSkillFromIndex(ClassBonus.skillNames[id]);
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
        return getClassOfPlayer(player).getPlayerClass();
    }

    @Override
    public byte getPlayerClass()
    {
        return playerClass;
    }

    @Override
    public String getClassName() {
        switch (playerClass) {
            case 1:
                return "BERSERKER";
            case 2:
                return "CLERIC";
            case 3:
                return "DRUID";
            case 4:
                return "WIZARD";
            case 5:
                return "ARCHER";
            case 6:
                return "ROGUE";
            default:
                return "NONE";
        }
    }

    @Override
    public void setPlayerClass(byte newClass) {
        if (newClass != playerClass) {
            ClassBonus.applyBonus(this, playerClass, newClass);
            capSkills();
            playerClass = newClass;
        }
    }

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
    public void refundSkillPoints(boolean resetClass, EntityPlayer player) {
        final byte clas = playerClass;
        setPlayerClass((byte) 0);
        skillMap.put("UnspentSkillPoints", getSkillPoints());
        setPlayerData(new int[ClassBonus.skillNames.length - 1]);
        if (!resetClass)
            setPlayerClass(clas);
        clearAllModifiers(player);
        // Reset max mana:
        String maxManaCommand = "/setPlayerMaxMana "+player.getName()+" "+10;
        player.getEntityWorld().getMinecraftServer().commandManager.executeCommand(player.getEntityWorld().getMinecraftServer(), maxManaCommand);
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

    // Remove all attribute modifiers from the player applied by this mod
    public void clearAllModifiers(EntityPlayer player) {
        IAttributeInstance luckAttributeInstance = player.getEntityAttribute(SharedMonsterAttributes.LUCK);
        IAttributeInstance attackDamageAttributeInstance = player.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE);
        IAttributeInstance maxHPAttributeInstance = player.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH);
        IAttributeInstance toughnessAttributeInstance = player.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS);
        if (luckAttributeInstance.getModifier(luckUID) != null) {
            luckAttributeInstance.removeModifier(luckUID);
        }
        if (attackDamageAttributeInstance.getModifier(attackDamageUID) != null) {
            attackDamageAttributeInstance.removeModifier(attackDamageUID);
        }
        if (maxHPAttributeInstance.getModifier(maxHPUID) != null) {
            maxHPAttributeInstance.removeModifier(maxHPUID);
        }
        if (toughnessAttributeInstance.getModifier(toughnessUID) != null) {
            toughnessAttributeInstance.removeModifier(toughnessUID);
        }
        // Max mana:
        String maxManaCommand = "/setPlayerMaxMana "+player.getName()+" 10";
        player.getEntityWorld().getMinecraftServer().commandManager.executeCommand(player.getEntityWorld().getMinecraftServer(), maxManaCommand);
    }
}

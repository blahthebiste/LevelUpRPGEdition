package com.lumberjacksparrow.leveluprpg.item;

import com.lumberjacksparrow.leveluprpg.LevelUpRPG;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

import static com.lumberjacksparrow.leveluprpg.LevelUpRPG.*;
import static com.lumberjacksparrow.leveluprpg.player.PlayerExtendedProperties.getFrom;

public class ItemClericBook extends Item {


    public ItemClericBook() {
        this.setRegistryName(ID, "cleric_book");
        this.setTranslationKey(ID+".cleric_book");
        this.setCreativeTab(CreativeTabs.TOOLS);
        this.setMaxStackSize(1);
    }

    @Override
    public EnumAction getItemUseAction(ItemStack stack) {
        return EnumAction.BOW;
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return bookOfBenedictionUseTime;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (!bookOfBenedictionRestricted || "cleric".equalsIgnoreCase(getFrom(player).getClassName())) {
            player.setActiveHand(hand);
            return new ActionResult<>(EnumActionResult.SUCCESS, stack);
        }
        return new ActionResult<>(EnumActionResult.FAIL, stack);
    }

    @Override
    public ItemStack onItemUseFinish(ItemStack stack, World world, EntityLivingBase entityLiving) {
        if (!world.isRemote && entityLiving instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer)entityLiving;
            MinecraftServer server = player.getEntityWorld().getMinecraftServer();
            // Null check for server
            if(server == null) {

                return stack;
            }
            if(!player.capabilities.isCreativeMode) {
                int luck = LevelUpRPG.getLuck(player);
                if(luck > 0) {
                    double roll = player.getRNG().nextDouble();
                    double highrollChance = 0.02*luck; // 2% chance per luck point to ignore cooldown
                    if(roll > highrollChance) { // Did not get lucky, no free cooldown
                        player.getCooldownTracker().setCooldown(this, bookOfBenedictionCooldown);
                    }
                    else {
                        player.sendStatusMessage(new TextComponentTranslation("cleric.gotlucky"), true);
                    }
                }
                else {
                    player.getCooldownTracker().setCooldown(this, bookOfBenedictionCooldown);
                }
            }
            //Create the holy nova
            String novaCommand = bookOfBenedictionCommand.replace("<player>", player.getName());
            //System.out.println("DEBUG: LevelUpRPG, executing nova command: "+novaCommand);
            server.commandManager.executeCommand(player.getEntityWorld().getMinecraftServer(), novaCommand);
        }
        return stack;
    }

    @Override
    public void onUsingTick(ItemStack stack, EntityLivingBase player, int count) {
        if (count % 10 == 0) {
            player.playSound(SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE,
                    1.25f, (float) (1.1f + 0.05f * player.getRNG().nextDouble()));
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack itemStack, World world, List<String> list, ITooltipFlag flag) {
        EntityPlayer player = Minecraft.getMinecraft().player;
        if(player == null) {
            return;
        }
        if(player.getCooldownTracker().hasCooldown(itemStack.getItem())) {
            list.add(I18n.format("clericbook.clericcooldown"));
        }
        else if(!bookOfBenedictionRestricted || "cleric".equalsIgnoreCase(getFrom(player).getClassName())) {
            list.add(I18n.format("clericbook.clericready"));
        }
        else {
            list.add(I18n.format("clericbook.notcleric"));
        }
    }
}

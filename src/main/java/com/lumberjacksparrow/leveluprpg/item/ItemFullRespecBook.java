package com.lumberjacksparrow.leveluprpg.item;

import com.lumberjacksparrow.leveluprpg.event.FMLEventHandler;
import com.lumberjacksparrow.leveluprpg.player.PlayerExtendedProperties;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

import static com.lumberjacksparrow.leveluprpg.LevelUpRPG.ID;

public class ItemFullRespecBook extends Item {
    public ItemFullRespecBook() {
        this.setRegistryName(ID, "respec_book_full");
        this.setTranslationKey(ID+".respec_book_full");
        this.setCreativeTab(CreativeTabs.TOOLS);
        this.setMaxStackSize(1);
    }

    @MethodsReturnNonnullByDefault
    @ParametersAreNonnullByDefault
    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand)
    {
        ItemStack itemstack = player.getHeldItem(hand);
        if (!world.isRemote) {
            PlayerExtendedProperties.getFrom(player).refundSkillPoints(true, player);
            FMLEventHandler.INSTANCE.loadPlayer(player);
        }
        if (!player.capabilities.isCreativeMode)
            itemstack.shrink(1);
        return new ActionResult<>(EnumActionResult.SUCCESS, itemstack);
    }


    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack itemStack, World world, List<String> list, ITooltipFlag flag) {
        list.add(I18n.format("respecbook.canresetclass"));
    }
}

package levelup.item;

import levelup.player.PlayerExtendedProperties;
import levelup.event.FMLEventHandler;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import java.util.List;

public final class ItemRespecBook extends Item {
    public ItemRespecBook() {
        super();
        setHasSubtypes(true);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(ItemStack itemstack, World world, EntityPlayer player, EnumHand hand)
    {
        if (!world.isRemote) {
            PlayerExtendedProperties.from(player).convertPointsToXp(itemstack.getItemDamage() > 0);
            FMLEventHandler.INSTANCE.loadPlayer(player);
        }
        if (!player.capabilities.isCreativeMode)
            itemstack.stackSize--;
        return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, itemstack);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getSubItems(Item item, CreativeTabs tab, List<ItemStack> list) {
        super.getSubItems(item, tab, list);
        list.add(new ItemStack(item, 1, 1));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack itemStack, EntityPlayer entityPlayer, List<String> list, boolean isAdvanced) {
        if (itemStack.getItemDamage() > 0) {
            list.add(I18n.format("respecbook.canresetclass"));
        }
    }
}

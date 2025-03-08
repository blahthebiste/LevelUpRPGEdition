package com.lumberjacksparrow.leveluprpg.item;

import com.lumberjacksparrow.leveluprpg.event.FMLEventHandler;
import com.lumberjacksparrow.leveluprpg.player.PlayerExtendedProperties;
import mcp.MethodsReturnNonnullByDefault;
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
import net.minecraft.util.ActionResult;
import net.minecraft.util.CooldownTracker;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

import static com.lumberjacksparrow.leveluprpg.LevelUpRPG.ID;
import static com.lumberjacksparrow.leveluprpg.player.PlayerExtendedProperties.getClassOfPlayer;

public class ItemClericBook extends Item {

    public static final int USE_TIME = 3 * 20; // 3 seconds
    public static final int COOLDOWN = 10 * 60 * 20; // 10 minutes
    public String holyNovaCommand = "/cast ebwizardry:healing_aura @p";

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
        return USE_TIME;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if ("cleric".equalsIgnoreCase(getClassOfPlayer(player).getClassName())) {
            player.setActiveHand(hand);
            return new ActionResult<>(EnumActionResult.SUCCESS, stack);
        }
        return new ActionResult<>(EnumActionResult.FAIL, stack);
    }

    @Override
    public ItemStack onItemUseFinish(ItemStack stack, World world, EntityLivingBase entityLiving) {
        if (!world.isRemote && entityLiving instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer)entityLiving;
            if(!player.capabilities.isCreativeMode) {
                player.getCooldownTracker().setCooldown(this, COOLDOWN);
            }
            //Create the holy nova
            player.getEntityWorld().getMinecraftServer().commandManager.executeCommand(player.getEntityWorld().getMinecraftServer(), holyNovaCommand);
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
        else if("cleric".equalsIgnoreCase(getClassOfPlayer(player).getClassName())) {
            list.add(I18n.format("clericbook.clericready"));
        }
        else {
            list.add(I18n.format("clericbook.notcleric"));
        }
    }
}

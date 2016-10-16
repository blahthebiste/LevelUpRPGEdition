package levelup.capabilities;

import levelup.api.LevelUpAPI;
import levelup.event.FMLEventHandler;
import levelup.util.SmeltingBlacklist;
import net.minecraft.block.BlockFurnace;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

public class CapabilityFurnace extends LevelUpCapability.CapabilityProcessorDefault
{
    public CapabilityFurnace(TileEntityFurnace furnace) {
        super(furnace);
    }

    @Override
    public void extraProcessing(EntityPlayer player) {

        if(tile != null && !player.worldObj.isRemote) {
            TileEntityFurnace furnace = (TileEntityFurnace)tile;
            if(furnace.isBurning()) {
                if (furnace.canSmelt()) {
                    ItemStack stack = furnace.getStackInSlot(0);
                    if (stack != null) {
                        int bonus;
                        if (stack.getItem().getItemUseAction(stack) == EnumAction.EAT) {
                            bonus = FMLEventHandler.getSkill(player, 7);
                        } else {
                            bonus = FMLEventHandler.getSkill(player, 4);
                        }
                        if (bonus > 10) {
                            int time = player.getRNG().nextInt(bonus / 10);
                            if (time != 0 && furnace.getField(2) + time < furnace.getField(3)) {//Increase burn time
                                furnace.setField(2, furnace.getField(2) + time);
                            }
                        }
                        if(furnace.getField(2) > 197) {
                            if(isDoublingValid(furnace) && player.getRNG().nextFloat() < (bonus / 200F)) {
                                ItemStack result = FurnaceRecipes.instance().getSmeltingResult(stack);
                                if(!LevelUpAPI.furnaceEjection) {
                                    if (furnace.getStackInSlot(2) == null) {
                                        furnace.setInventorySlotContents(2, result.copy());
                                    } else if (furnace.getStackInSlot(2) != null) {
                                        ItemStack product = furnace.getStackInSlot(2);
                                        if (ItemStack.areItemsEqual(result, product)) {
                                            if (product.stackSize + (result.stackSize * 2) <= product.getMaxStackSize()) {
                                                furnace.getStackInSlot(2).stackSize += result.stackSize;
                                            }
                                        }
                                    }
                                }
                                else
                                    ejectExtraItem(result.copy());
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean isDoublingValid(TileEntityFurnace tile) {
        ItemStack smeltingItem = tile.getStackInSlot(0);
        return FurnaceRecipes.instance().getSmeltingResult(smeltingItem) != null && !SmeltingBlacklist.contains(smeltingItem);
    }

    private void ejectExtraItem(ItemStack stack) {
        if(stack != null) {
            if(tile.getBlockType() == Blocks.FURNACE || tile.getBlockType() == Blocks.LIT_FURNACE) {
                IBlockState furnace = tile.getWorld().getBlockState(tile.getPos());
                EnumFacing facing = furnace.getValue(BlockFurnace.FACING);
                BlockPos offset = tile.getPos().offset(facing);
                EntityItem item = new EntityItem(tile.getWorld(), offset.getX() + 0.5D, offset.getY() + 0.5D, offset.getZ() + 0.5D, stack);
                tile.getWorld().spawnEntityInWorld(item);
            }
        }
    }
}

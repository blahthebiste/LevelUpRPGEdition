package levelup.capabilities;

import net.minecraft.tileentity.TileEntityFurnace;

public class CapabilityFurnace extends LevelUpCapability.CapabilityProcessorDefault
{
    public CapabilityFurnace(TileEntityFurnace furnace) {
        super(furnace);
    }
/*
    @Override
    public void extraProcessing(EntityPlayer player) {

        if(tile != null && !player.world.isRemote) {
            TileEntityFurnace furnace = (TileEntityFurnace)tile;
            if(furnace.isBurning()) {
                if (furnace.canSmelt()) {
                    ItemStack stack = furnace.getStackInSlot(0);
                    if (stack != ItemStack.EMPTY) {
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
                        if(furnace.getField(2) > 198) {
                            if(isDoublingValid(furnace) && player.getRNG().nextFloat() < (bonus / 200F)) {
                                ItemStack result = FurnaceRecipes.instance().getSmeltingResult(stack);
                                if(!LevelUpAPI.furnaceEjection) {
                                    if (furnace.getStackInSlot(2) == ItemStack.EMPTY) {
                                        furnace.setInventorySlotContents(2, result.copy());
                                    } else if (furnace.getStackInSlot(2) != ItemStack.EMPTY) {
                                        ItemStack product = furnace.getStackInSlot(2);
                                        if (ItemStack.areItemsEqual(result, product)) {
                                            if (product.getCount() + (result.getCount() * 2) <= product.getMaxStackSize()) {
                                                furnace.getStackInSlot(2).grow(result.getCount());
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
        return FurnaceRecipes.instance().getSmeltingResult(smeltingItem) != ItemStack.EMPTY && !SmeltingBlacklist.contains(smeltingItem);
    }

    private void ejectExtraItem(ItemStack stack) {
        if(stack != null) {
            if(tile.getBlockType() == Blocks.FURNACE || tile.getBlockType() == Blocks.LIT_FURNACE) {
                IBlockState furnace = tile.getWorld().getBlockState(tile.getPos());
                EnumFacing facing = furnace.getValue(BlockFurnace.FACING);
                BlockPos offset = tile.getPos().offset(facing);
                EntityItem item = new EntityItem(tile.getWorld(), offset.getX() + 0.5D, offset.getY() + 0.5D, offset.getZ() + 0.5D, stack);
                tile.getWorld().spawnEntity(item);
            }
        }
    }

 */
}

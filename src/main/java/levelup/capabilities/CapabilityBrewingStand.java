package levelup.capabilities;

import levelup.event.FMLEventHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntityBrewingStand;

public class CapabilityBrewingStand extends LevelUpCapability.CapabilityProcessorDefault
{
    public CapabilityBrewingStand(TileEntityBrewingStand stand) {
        super(stand);
    }

    @Override
    public void extraProcessing(EntityPlayer player) {
        if(tile instanceof TileEntityBrewingStand) {
            TileEntityBrewingStand stand = (TileEntityBrewingStand)tile;
            if(stand.getField(0) > 0) {
                int bonus = FMLEventHandler.getSkill(player, 4);
                if(bonus > 10) {
                    int time = player.getRNG().nextInt(bonus / 10);
                    if(time != 0 && stand.getField(0) - time > 0)
                        stand.setField(0, stand.getField(0) - time);
                }
            }
        }
    }
}

package levelup.capabilities;

import levelup.IPlayerClass;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

public class LevelUpCapability
{
    @CapabilityInject(IPlayerClass.class)
    public static Capability<IPlayerClass> CAPABILITY_CLASS = null;

    public static class CapabilityPlayerClass<T extends IPlayerClass> implements Capability.IStorage<IPlayerClass>
    {
        @Override
        public NBTBase writeNBT(Capability<IPlayerClass> capability, IPlayerClass player, EnumFacing side)
        {
            return player.saveNBTData(new NBTTagCompound());
        }

        @Override
        public void readNBT(Capability<IPlayerClass> capability, IPlayerClass player, EnumFacing side, NBTBase nbt)
        {
            player.loadNBTData((NBTTagCompound)nbt);
        }
    }
}

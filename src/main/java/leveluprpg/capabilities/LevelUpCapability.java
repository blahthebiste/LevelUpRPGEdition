package leveluprpg.capabilities;

import leveluprpg.api.IProcessor;
import leveluprpg.player.IPlayerClass;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

public class LevelUpCapability
{
    @CapabilityInject(IPlayerClass.class)
    public static Capability<IPlayerClass> CAPABILITY_CLASS = null;

    @CapabilityInject(IProcessor.class)
    public static Capability<IProcessor> MACHINE_PROCESSING = null;

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

    public static class CapabilityProcessorClass<T extends IProcessor> implements Capability.IStorage<IProcessor>
    {
        @Override
        public NBTBase writeNBT(Capability<IProcessor> capability, IProcessor process, EnumFacing side) {
            return new NBTTagCompound();
        }

        @Override
        public void readNBT(Capability<IProcessor> capability, IProcessor process, EnumFacing side, NBTBase nbt) {

        }
    }

    public static class CapabilityProcessorDefault implements IProcessor
    {
        protected TileEntity tile;

        public CapabilityProcessorDefault(TileEntity entity) {
            tile = entity;
        }

        @Override
        public void extraProcessing(EntityPlayer player) {

        }
    }
}

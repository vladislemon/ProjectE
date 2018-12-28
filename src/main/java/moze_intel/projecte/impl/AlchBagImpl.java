package moze_intel.projecte.impl;

import moze_intel.projecte.PECore;
import moze_intel.projecte.api.ProjectEAPI;
import moze_intel.projecte.api.capabilities.IAlchBagProvider;
import moze_intel.projecte.network.PacketHandler;
import moze_intel.projecte.network.packets.SyncBagDataPKT;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.nbt.INBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.capabilities.OptionalCapabilityInstance;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.Map;

public final class AlchBagImpl
{

    public static void init()
    {
        CapabilityManager.INSTANCE.register(IAlchBagProvider.class, new Capability.IStorage<IAlchBagProvider>()
        {
            @Override
            public NBTTagCompound writeNBT(Capability<IAlchBagProvider> capability, IAlchBagProvider instance, EnumFacing side)
            {
                return instance.serializeNBT();
            }

            @Override
            public void readNBT(Capability<IAlchBagProvider> capability, IAlchBagProvider instance, EnumFacing side, INBTBase nbt) {
                if (nbt instanceof NBTTagCompound)
                    instance.deserializeNBT(((NBTTagCompound) nbt));
            }
        }, DefaultImpl::new);
    }

    private static class DefaultImpl implements IAlchBagProvider
    {
        private final Map<EnumDyeColor, IItemHandler> inventories = new EnumMap<>(EnumDyeColor.class);

        @Nonnull
        @Override
        public IItemHandler getBag(@Nonnull EnumDyeColor color)
        {
            if (!inventories.containsKey(color))
            {
                inventories.put(color, new ItemStackHandler(104));
            }

            return inventories.get(color);
        }

        @Override
        public void sync(@Nullable EnumDyeColor color, @Nonnull EntityPlayerMP player)
        {
            PacketHandler.sendTo(new SyncBagDataPKT(writeNBT(color)), player);
        }

        private NBTTagCompound writeNBT(EnumDyeColor color)
        {
            NBTTagCompound ret = new NBTTagCompound();
            EnumDyeColor[] colors = color == null ? EnumDyeColor.values() : new EnumDyeColor[] { color };
            for (EnumDyeColor c : colors)
            {
                if (inventories.containsKey(c))
                {
                    INBTBase inv = CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.getStorage()
                            .writeNBT(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, inventories.get(c), null);
                    ret.put(c.getName(), inv);
                }
            }
            return ret;
        }

        @Override
        public NBTTagCompound serializeNBT()
        {
            return writeNBT(null);
        }

        @Override
        public void deserializeNBT(NBTTagCompound nbt)
        {
            for (EnumDyeColor e : EnumDyeColor.values())
            {
                if (nbt.contains(e.getName()))
                {
                    IItemHandler inv = new ItemStackHandler(104);
                    CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.getStorage()
                            .readNBT(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, inv, null, nbt.get(e.getName()));
                    inventories.put(e, inv);
                }
            }
        }
    }

    public static class Provider implements ICapabilitySerializable<NBTTagCompound>
    {

        public static final ResourceLocation NAME = new ResourceLocation(PECore.MODID, "alch_bags");
        private final IAlchBagProvider impl = new DefaultImpl();
        private final OptionalCapabilityInstance<IAlchBagProvider> cap = OptionalCapabilityInstance.of(() -> impl);

        @Override
        public <T> OptionalCapabilityInstance<T> getCapability(@Nonnull Capability<T> capability, EnumFacing facing)
        {
            if (capability == ProjectEAPI.ALCH_BAG_CAPABILITY)
            {
                return cap.cast();
            }

            return OptionalCapabilityInstance.empty();
        }

        @Override
        public NBTTagCompound serializeNBT()
        {
            return impl.serializeNBT();
        }

        @Override
        public void deserializeNBT(NBTTagCompound nbt)
        {
            impl.deserializeNBT(nbt);
        }
    }

    private AlchBagImpl() {}

}

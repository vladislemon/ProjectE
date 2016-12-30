package moze_intel.projecte.gameObjs.container.inventory;

import moze_intel.projecte.api.capabilities.IAlchBagProvider;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nonnull;

public class AlchBagInventory implements IItemHandlerModifiable
{
	public final ItemStack invItem;
	private final IItemHandlerModifiable compose;

	public AlchBagInventory(IAlchBagProvider provider, ItemStack stack)
	{
		this.invItem = stack;
		this.compose = (IItemHandlerModifiable) provider.getBag(EnumDyeColor.byMetadata(stack.getItemDamage()));
	}

	@Override
	public void setStackInSlot(int slot, @Nonnull ItemStack stack) {
		compose.setStackInSlot(slot, stack);
	}

	@Override
	public int getSlots() {
		return compose.getSlots();
	}

	@Nonnull
	@Override
	public ItemStack getStackInSlot(int slot) {
		return compose.getStackInSlot(slot);
	}

	@Nonnull
	@Override
	public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
		if (stack == invItem)
			return stack; // Cannot put the bag into itself
		else return compose.insertItem(slot, stack, simulate);
	}

	@Nonnull
	@Override
	public ItemStack extractItem(int slot, int amount, boolean simulate) {
		return compose.extractItem(slot, amount, simulate);
	}

	@Override
	public int getSlotLimit(int slot)
	{
		return compose.getSlotLimit(slot);
	}
}

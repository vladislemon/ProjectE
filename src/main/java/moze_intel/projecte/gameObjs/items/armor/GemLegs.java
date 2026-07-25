package moze_intel.projecte.gameObjs.items.armor;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import moze_intel.projecte.utils.ChatHelper;
import moze_intel.projecte.utils.ClientKeyHelper;
import moze_intel.projecte.utils.EnumArmorType;
import moze_intel.projecte.utils.PEKeybind;
import moze_intel.projecte.utils.WorldHelper;

public class GemLegs extends GemArmorBase {

    public static final String ACCELERATED_DESCENT_NBT_KEY_NAME = "AcceleratedDescent";

    public GemLegs() {
        super(EnumArmorType.LEGS);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, EntityPlayer player, List tooltips, boolean unused) {
        tooltips.add(StatCollector.translateToLocal("pe.gem.legs.lorename"));
        tooltips.add(
            String.format(
                StatCollector.translateToLocal("pe.gem.accelerateddescent.prompt"),
                ClientKeyHelper.getKeyName(PEKeybind.MODE)));

        boolean acceleratedDescentEnabled = isAcceleratedDescentEnabled(stack);
        EnumChatFormatting e = acceleratedDescentEnabled ? EnumChatFormatting.GREEN : EnumChatFormatting.RED;
        String s = acceleratedDescentEnabled ? "pe.gem.enabled" : "pe.gem.disabled";
        tooltips.add(
            StatCollector.translateToLocal("pe.gem.accelerateddescent_tooltip") + " "
                + e
                + StatCollector.translateToLocal(s));
    }

    @Override
    public void onArmorTick(World world, EntityPlayer player, ItemStack stack) {
        if (world.isRemote) {
            if (player.isSneaking() && !player.onGround && player.motionY <= 0 && isAcceleratedDescentEnabled(stack)) {
                player.motionY *= 2;
            }
        }

        if (player.isSneaking()) {
            AxisAlignedBB box = AxisAlignedBB.getBoundingBox(
                player.posX - 3.5,
                player.posY - 3.5,
                player.posZ - 3.5,
                player.posX + 3.5,
                player.posY + 3.5,
                player.posZ + 3.5);
            WorldHelper.repelEntitiesInAABBFromPoint(world, box, player.posX, player.posY, player.posZ, true);
        }
    }

    private boolean isAcceleratedDescentEnabled(ItemStack stack) {
        return stack.getTagCompound() != null && stack.getTagCompound()
            .hasKey(ACCELERATED_DESCENT_NBT_KEY_NAME)
            && stack.getTagCompound()
                .getBoolean(ACCELERATED_DESCENT_NBT_KEY_NAME);
    }

    public void toggleAcceleratedDescent(ItemStack legs, EntityPlayer player) {
        if (!legs.hasTagCompound()) {
            legs.setTagCompound(new NBTTagCompound());
        }

        boolean value = false;

        if (legs.stackTagCompound.hasKey(ACCELERATED_DESCENT_NBT_KEY_NAME)) {
            value = !legs.stackTagCompound.getBoolean(ACCELERATED_DESCENT_NBT_KEY_NAME);
            legs.stackTagCompound.setBoolean(ACCELERATED_DESCENT_NBT_KEY_NAME, value);
        }
        legs.stackTagCompound.setBoolean(ACCELERATED_DESCENT_NBT_KEY_NAME, value);

        EnumChatFormatting e = value ? EnumChatFormatting.GREEN : EnumChatFormatting.RED;
        String s = value ? "pe.gem.enabled" : "pe.gem.disabled";
        player.addChatMessage(
            new ChatComponentTranslation("pe.gem.accelerateddescent_tooltip").appendText(" ")
                .appendSibling(ChatHelper.modifyColor(new ChatComponentTranslation(s), e)));
    }
}

package com.mrcrayfish.device.api.print;

import com.mrcrayfish.device.init.DeviceItems;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

//printing somethings takes makes ink cartridge take damage. cartridge can only stack to one

/**
 * Author: MrCrayfish
 */
public interface IPrint
{
    /**
     * Gets the speed of the print. The higher the value, the longer it will take to print.
     * @return the speed of this print
     */
    int speed();

    /**
     * Gets whether or not this print requires coloured ink.
     * @return if print requires ink
     */
    boolean requiresColor();

    /**
     * Converts print into an NBT tag compound. Used for the renderer.
     * @return nbt form of print
     */
    NBTTagCompound toTag();

    void fromTag(NBTTagCompound tag);

    @SideOnly(Side.CLIENT)
    Class<? extends Renderer> getRenderer();

    static NBTTagCompound writeToTag(IPrint print)
    {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("type", PrintingManager.getPrintIdentifier(print));
        tag.setTag("data", print.toTag());
        return tag;
    }

    @Nullable
    static IPrint loadFromTag(NBTTagCompound tag)
    {
        IPrint print = PrintingManager.getPrint(tag.getString("type"));
        if(print != null)
        {
            print.fromTag(tag.getCompoundTag("data"));
            return print;
        }
        return null;
    }

    static ItemStack generateItem(IPrint print)
    {
        ItemStack stack = new ItemStack(DeviceItems.paper_printed);
        stack.setTagCompound(writeToTag(print));
        return stack;
    }

    interface Renderer
    {
        boolean render(NBTTagCompound data);
    }
}
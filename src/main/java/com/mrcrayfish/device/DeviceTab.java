package com.mrcrayfish.device;

import com.mrcrayfish.device.init.DeviceBlocks;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class DeviceTab extends CreativeTabs 
{
	public DeviceTab(String label) 
	{
		super(label);
	}

	@Override
	@NotNull
	public ItemStack createIcon()
	{
		return new ItemStack(DeviceBlocks.LAPTOP, 1, EnumDyeColor.RED.getMetadata());
	}
}

package com.mrcrayfish.device.event;

import com.mrcrayfish.device.api.utils.BankUtil;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class BankEvents 
{
	@SubscribeEvent
	public void load(WorldEvent.Load event)
	{
		if(event.getWorld().provider.getDimension() == 0)
		{
			try 
			{
				Path data = Paths.get(String.valueOf(DimensionManager.getCurrentSaveRootDirectory()), "bank.dat");

				if (!Files.exists(data))
				{
					return;
				}
				
				NBTTagCompound nbt = CompressedStreamTools.read(data.toFile());
				if(nbt != null)
				{
					BankUtil.INSTANCE.load(nbt);
				}
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
			}
		}
	}
	
	@SubscribeEvent
	public void save(WorldEvent.Save event)
	{
		if(event.getWorld().provider.getDimension() == 0)
		{
			try 
			{
				Path data = Paths.get(String.valueOf(DimensionManager.getCurrentSaveRootDirectory()), "bank.dat");

				if (!Files.exists(data))
				{
					Files.createDirectories(data);
				}

				NBTTagCompound nbt = new NBTTagCompound();
				BankUtil.INSTANCE.save(nbt);
				CompressedStreamTools.write(nbt, data.toFile());
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
			}
		}
	}
}

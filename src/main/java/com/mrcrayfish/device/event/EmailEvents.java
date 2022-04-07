package com.mrcrayfish.device.event;

import com.mrcrayfish.device.programs.email.EmailManager;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class EmailEvents 
{
	@SubscribeEvent
	public void load(WorldEvent.Load event)
	{
		if(event.getWorld().provider.getDimension() == 0)
		{
			try 
			{
				Path data = Paths.get(String.valueOf(DimensionManager.getCurrentSaveRootDirectory()), "emails.dat");

				if (!Files.exists(data))
				{
					return;
				}
				
				NBTTagCompound nbt = CompressedStreamTools.read(data.toFile());
				if(nbt != null)
				{
					EmailManager.INSTANCE.readFromNBT(nbt);
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
				Path data = Paths.get(String.valueOf(DimensionManager.getCurrentSaveRootDirectory()), "emails.dat");

				if (!Files.exists(data))
				{
					Files.createDirectories(data);
				}
				
				NBTTagCompound nbt = new NBTTagCompound();
				EmailManager.INSTANCE.writeToNBT(nbt);
				CompressedStreamTools.write(nbt, data.toFile());
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
			}
		}
	}
}

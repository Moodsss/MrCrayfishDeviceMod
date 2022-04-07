package com.mrcrayfish.device.init;

import com.mrcrayfish.device.Reference;
import com.mrcrayfish.device.tileentity.*;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class DeviceTileEntites 
{
	public static void register()
    {
		GameRegistry.registerTileEntity(TileEntityLaptop.class, new ResourceLocation(Reference.MOD_ID, "laptop"));
		GameRegistry.registerTileEntity(TileEntityRouter.class, new ResourceLocation(Reference.MOD_ID, "router"));
		GameRegistry.registerTileEntity(TileEntityPrinter.class, new ResourceLocation(Reference.MOD_ID, "printer"));
		GameRegistry.registerTileEntity(TileEntityPaper.class, new ResourceLocation(Reference.MOD_ID, "printed_paper"));
		GameRegistry.registerTileEntity(TileEntityOfficeChair.class, new ResourceLocation(Reference.MOD_ID, "office_chair"));
	}
}

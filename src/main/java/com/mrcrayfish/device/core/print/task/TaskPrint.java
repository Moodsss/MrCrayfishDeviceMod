package com.mrcrayfish.device.core.print.task;

import com.mrcrayfish.device.api.print.IPrint;
import com.mrcrayfish.device.api.print.PrintingManager;
import com.mrcrayfish.device.api.task.Task;
import com.mrcrayfish.device.init.DeviceItems;
import com.mrcrayfish.device.tileentity.TileEntityPrinter;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Author: MrCrayfish
 */
public class TaskPrint extends Task
{
    private BlockPos pos;
    private IPrint print;

    private TaskPrint()
    {
        super("print");
    }

    public TaskPrint(BlockPos pos, IPrint print)
    {
        this();
        this.pos = pos;
        this.print = print;
    }

    @Override
    public void prepareRequest(NBTTagCompound nbt)
    {
        nbt.setLong("pos", pos.toLong());
        nbt.setTag("print", IPrint.writeToTag(print));
    }

    @Override
    public void processRequest(NBTTagCompound nbt, World world, EntityPlayer player)
    {
        TileEntity tileEntity = world.getTileEntity(BlockPos.fromLong(nbt.getLong("pos")));
        if(tileEntity instanceof TileEntityPrinter)
        {
            TileEntityPrinter printer = (TileEntityPrinter) tileEntity;
            IPrint print = IPrint.loadFromTag(nbt.getCompoundTag("print"));
            printer.print(print);

            this.setSuccessful();
        }
    }

    @Override
    public void prepareResponse(NBTTagCompound nbt)
    {

    }

    @Override
    public void processResponse(NBTTagCompound nbt)
    {

    }
}
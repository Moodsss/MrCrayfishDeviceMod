package com.mrcrayfish.device.tileentity;

import com.mrcrayfish.device.entity.EntitySeat;
import com.mrcrayfish.device.util.IColored;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Author: MrCrayfish
 */
public class TileEntityOfficeChair extends TileEntitySync implements IColored
{
    private EnumDyeColor color = EnumDyeColor.RED;

    private float rotation;

    @Override
    public EnumDyeColor getColor()
    {
        return color;
    }

    @Override
    public void setColor(EnumDyeColor color)
    {
        this.color = color;
    }

    @Override
    public void readFromNBT(@NotNull NBTTagCompound compound)
    {
        super.readFromNBT(compound);
        if(compound.hasKey("color", Constants.NBT.TAG_BYTE))
        {
            this.color = EnumDyeColor.byMetadata(compound.getByte("color"));
        }

        if(compound.hasKey("rotation", Constants.NBT.TAG_FLOAT))
        {
            this.rotation = compound.getFloat("rotation");
        }
    }

    @Override
    @NotNull
    public NBTTagCompound writeToNBT(@NotNull NBTTagCompound compound)
    {
        super.writeToNBT(compound);
        compound.setByte("color", (byte) color.getMetadata());
        compound.setFloat("rotation", this.rotation);
        return compound;
    }

    @Override
    public NBTTagCompound writeSyncTag()
    {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setByte("color", (byte) color.getMetadata());
        return tag;
    }

    @SideOnly(Side.CLIENT)
    public float getRotation()
    {
        List<EntitySeat> seats = world.getEntitiesWithinAABB(EntitySeat.class, new AxisAlignedBB(pos));
        if(!seats.isEmpty())
        {
            EntitySeat seat = seats.get(0);
            if(seat.getControllingPassenger() != null)
            {
                if(seat.getControllingPassenger() instanceof EntityLivingBase)
                {
                    EntityLivingBase living = (EntityLivingBase) seat.getControllingPassenger();
                    living.renderYawOffset = living.rotationYaw;
                    living.prevRenderYawOffset = living.rotationYaw;
                    this.rotation = living.rotationYaw;
                }
                this.rotation = seat.getControllingPassenger().rotationYaw;
            }
        }

        return this.rotation;
    }

    @SideOnly(Side.CLIENT)
    protected static float lerp(float delta, float start, float end)
    {
        return start + delta * (end - start);
    }
}

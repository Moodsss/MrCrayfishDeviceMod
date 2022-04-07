package com.mrcrayfish.device.block;

import com.mrcrayfish.device.tileentity.TileEntityDevice;
import com.mrcrayfish.device.util.IColored;
import net.minecraft.block.BlockColored;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Random;

/**
 * Author: MrCrayfish
 */
@SuppressWarnings("deprecation")
public abstract class BlockDevice extends BlockHorizontal
{
    protected BlockDevice(Material materialIn)
    {
        super(materialIn);
        this.setHardness(0.5F);
    }

    @Override
    public boolean isOpaqueCube(@NotNull IBlockState state)
    {
        return false;
    }

    @Override
    public boolean isFullCube(@NotNull IBlockState state)
    {
        return false;
    }

    @Override
    public boolean canBeConnectedTo(@NotNull IBlockAccess world, @NotNull BlockPos pos, @NotNull EnumFacing facing)
    {
        return false;
    }

    @Override
    @NotNull
    public BlockFaceShape getBlockFaceShape(@NotNull IBlockAccess worldIn, @NotNull IBlockState state, @NotNull BlockPos pos, @NotNull EnumFacing face)
    {
        return BlockFaceShape.UNDEFINED;
    }

    @Override
    @NotNull
    public IBlockState getStateForPlacement(@NotNull World world, @NotNull BlockPos pos, @NotNull EnumFacing facing, float hitX, float hitY, float hitZ, int meta, @NotNull EntityLivingBase placer, @NotNull EnumHand hand)
    {
        IBlockState state = super.getStateForPlacement(world, pos, facing, hitX, hitY, hitZ, meta, placer, hand);
        return state.withProperty(FACING, placer.getHorizontalFacing());
    }

    @Override
    @NotNull
    public Item getItemDropped(@NotNull IBlockState state, @NotNull Random rand, int fortune)
    {
        return null;
    }

    @Override
    public void getDrops(@NotNull NonNullList<ItemStack> drops, @NotNull IBlockAccess world, @NotNull BlockPos pos, @NotNull IBlockState state, int fortune) {}

    @Override
    public void onBlockPlacedBy(@NotNull World worldIn, @NotNull BlockPos pos, @NotNull IBlockState state, @NotNull EntityLivingBase placer, @NotNull ItemStack stack)
    {
        super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
        TileEntity tileEntity = worldIn.getTileEntity(pos);
        if(tileEntity instanceof TileEntityDevice)
        {
            TileEntityDevice tileEntityDevice = (TileEntityDevice) tileEntity;
            if(stack.hasDisplayName())
            {
                tileEntityDevice.setCustomName(stack.getDisplayName());
            }
        }
    }

    @Override
    public boolean removedByPlayer(@NotNull IBlockState state, World world, @NotNull BlockPos pos, @NotNull EntityPlayer player, boolean willHarvest)
    {
        if(!world.isRemote && !player.capabilities.isCreativeMode)
        {
            TileEntity tileEntity = world.getTileEntity(pos);
            if(tileEntity instanceof TileEntityDevice)
            {
                TileEntityDevice device = (TileEntityDevice) tileEntity;

                NBTTagCompound tileEntityTag = new NBTTagCompound();
                tileEntity.writeToNBT(tileEntityTag);
                tileEntityTag.removeTag("x");
                tileEntityTag.removeTag("y");
                tileEntityTag.removeTag("z");
                tileEntityTag.removeTag("id");
                tileEntityTag.removeTag("color");

                removeTagsForDrop(tileEntityTag);

                NBTTagCompound compound = new NBTTagCompound();
                compound.setTag("BlockEntityTag", tileEntityTag);

                ItemStack drop;
                if(tileEntity instanceof IColored)
                {
                    drop = new ItemStack(Item.getItemFromBlock(this), 1, ((IColored)tileEntity).getColor().getMetadata());
                }
                else
                {
                    drop = new ItemStack(Item.getItemFromBlock(this));
                }
                drop.setTagCompound(compound);

                if(device.hasCustomName())
                {
                    drop.setStackDisplayName(device.getCustomName());
                }

                world.spawnEntity(new EntityItem(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, drop));

                return world.setBlockToAir(pos);
            }
        }
        return super.removedByPlayer(state, world, pos, player, willHarvest);
    }

    protected void removeTagsForDrop(NBTTagCompound tileEntityTag) {}

    @Override
    public boolean hasTileEntity(@NotNull IBlockState state)
    {
        return true;
    }

    @Nullable
    @Override
    public abstract TileEntity createTileEntity(@NotNull World world, @NotNull IBlockState state);

    @Override
    public boolean eventReceived(@NotNull IBlockState state, World worldIn, @NotNull BlockPos pos, int id, int param)
    {
        TileEntity tileentity = worldIn.getTileEntity(pos);
        return tileentity != null && tileentity.receiveClientEvent(id, param);
    }

    /**
     * Colored implementation of BlockDevice.
     */
    public static abstract class Colored extends BlockDevice
    {
        protected Colored(Material materialIn)
        {
            super(materialIn);
        }

        @Override
        public void getDrops(@NotNull NonNullList<ItemStack> drops, @NotNull IBlockAccess world, @NotNull BlockPos pos, @NotNull IBlockState state, int fortune)
        {
            TileEntity tileEntity = world.getTileEntity(pos);
            if(tileEntity instanceof IColored)
            {
                drops.add(new ItemStack(Item.getItemFromBlock(this), 1, ((IColored) tileEntity).getColor().getMetadata()));
            }
        }

        @Override
        @NotNull
        public ItemStack getPickBlock(@NotNull IBlockState state, @NotNull RayTraceResult target, World world, @NotNull BlockPos pos, @NotNull EntityPlayer player)
        {
            TileEntity tileEntity = world.getTileEntity(pos);
            if(tileEntity instanceof IColored)
            {
                return new ItemStack(Item.getItemFromBlock(this), 1, ((IColored) tileEntity).getColor().getMetadata());
            }
            return super.getPickBlock(state, target, world, pos, player);
        }

        @Override
        public void onBlockPlacedBy(@NotNull World worldIn, @NotNull BlockPos pos, @NotNull IBlockState state, @NotNull EntityLivingBase placer, @NotNull ItemStack stack)
        {
            super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
            TileEntity tileEntity = worldIn.getTileEntity(pos);
            if(tileEntity instanceof IColored)
            {
                IColored colored = (IColored) tileEntity;
                colored.setColor(EnumDyeColor.byMetadata(stack.getMetadata()));
            }
        }

        @Override
        @NotNull
        public IBlockState getActualState(@NotNull IBlockState state, IBlockAccess worldIn, @NotNull BlockPos pos)
        {
            TileEntity tileEntity = worldIn.getTileEntity(pos);
            if(tileEntity instanceof IColored)
            {
                IColored colored = (IColored) tileEntity;
                state = state.withProperty(BlockColored.COLOR, colored.getColor());
            }
            return state;
        }

        @Override
        public boolean removedByPlayer(@NotNull IBlockState state, World world, @NotNull BlockPos pos, @NotNull EntityPlayer player, boolean willHarvest)
        {
            if(!world.isRemote && !player.capabilities.isCreativeMode)
            {
                TileEntity tileEntity = world.getTileEntity(pos);
                if(tileEntity instanceof IColored)
                {
                    IColored colored = (IColored) tileEntity;

                    NBTTagCompound tileEntityTag = new NBTTagCompound();
                    tileEntity.writeToNBT(tileEntityTag);
                    tileEntityTag.removeTag("x");
                    tileEntityTag.removeTag("y");
                    tileEntityTag.removeTag("z");
                    tileEntityTag.removeTag("id");
                    tileEntityTag.removeTag("color");

                    removeTagsForDrop(tileEntityTag);

                    NBTTagCompound compound = new NBTTagCompound();
                    compound.setTag("BlockEntityTag", tileEntityTag);

                    ItemStack  drop = new ItemStack(Item.getItemFromBlock(this), 1, colored.getColor().getMetadata());
                    drop.setTagCompound(compound);

                    if(tileEntity instanceof TileEntityDevice)
                    {
                        TileEntityDevice device = (TileEntityDevice) tileEntity;
                        if(device.hasCustomName())
                        {
                            drop.setStackDisplayName(device.getCustomName());
                        }
                    }

                    world.spawnEntity(new EntityItem(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, drop));

                    return world.setBlockToAir(pos);
                }
            }
            return super.removedByPlayer(state, world, pos, player, willHarvest);
        }

        @Override
        public int getMetaFromState(IBlockState state)
        {
            return state.getValue(FACING).getHorizontalIndex();
        }

        @Override
        @NotNull
        public IBlockState getStateFromMeta(int meta)
        {
            return this.getDefaultState().withProperty(FACING, EnumFacing.byHorizontalIndex(meta));
        }

        @Override
        @NotNull
        protected BlockStateContainer createBlockState()
        {
            return new BlockStateContainer(this, FACING, BlockColored.COLOR);
        }
    }
}

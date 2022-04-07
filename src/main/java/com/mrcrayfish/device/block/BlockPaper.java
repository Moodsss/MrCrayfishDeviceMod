package com.mrcrayfish.device.block;

import com.mrcrayfish.device.api.print.IPrint;
import com.mrcrayfish.device.object.Bounds;
import com.mrcrayfish.device.tileentity.TileEntityPaper;
import com.mrcrayfish.device.util.CollisionHelper;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Random;

/**
 * Author: MrCrayfish & Moodss
 */
@SuppressWarnings("deprecation")
public class BlockPaper extends BlockHorizontal implements ITileEntityProvider
{
    private static final Bounds SELECTION_BOUNDS = new Bounds(15 * 0.0625, 0.0, 0.0, 16 * 0.0625, 16 * 0.0625, 16 * 0.0625);
    private static final AxisAlignedBB SELECTION_BOX_NORTH = CollisionHelper.getBlockBounds(EnumFacing.NORTH, SELECTION_BOUNDS);
    private static final AxisAlignedBB SELECTION_BOX_EAST = CollisionHelper.getBlockBounds(EnumFacing.EAST, SELECTION_BOUNDS);
    private static final AxisAlignedBB SELECTION_BOX_SOUTH = CollisionHelper.getBlockBounds(EnumFacing.SOUTH, SELECTION_BOUNDS);
    private static final AxisAlignedBB SELECTION_BOX_WEST = CollisionHelper.getBlockBounds(EnumFacing.WEST, SELECTION_BOUNDS);
    private static final AxisAlignedBB[] SELECTION_BOUNDING_BOX = { SELECTION_BOX_SOUTH, SELECTION_BOX_WEST, SELECTION_BOX_NORTH, SELECTION_BOX_EAST };
    
    public BlockPaper()
    {
        super(Material.CLOTH);
        this.setTranslationKey("paper");
        this.setRegistryName("paper");
        this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
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
    @NotNull
    public AxisAlignedBB getBoundingBox(IBlockState state, @NotNull IBlockAccess source, @NotNull BlockPos pos)
    {
        return SELECTION_BOUNDING_BOX[state.getValue(FACING).getHorizontalIndex()];
    }

    @Nullable
    @Override
    public AxisAlignedBB getCollisionBoundingBox(@NotNull IBlockState blockState, @NotNull IBlockAccess worldIn, @NotNull BlockPos pos)
    {
        return null;
    }

    @Override
    @NotNull
    public IBlockState getStateForPlacement(@NotNull World world, @NotNull BlockPos pos, @NotNull EnumFacing facing, float hitX, float hitY, float hitZ, int meta, @NotNull EntityLivingBase placer, @NotNull EnumHand hand)
    {
        IBlockState state = super.getStateForPlacement(world, pos, facing, hitX, hitY, hitZ, meta, placer, hand);
        return state.withProperty(FACING, placer.getHorizontalFacing());
    }

    @Override
    public boolean onBlockActivated(World worldIn, @NotNull BlockPos pos, @NotNull IBlockState state, @NotNull EntityPlayer playerIn, @NotNull EnumHand hand, @NotNull EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        if(!worldIn.isRemote)
        {
            TileEntity tileEntity = worldIn.getTileEntity(pos);
            if(tileEntity instanceof TileEntityPaper)
            {
                TileEntityPaper paper = (TileEntityPaper) tileEntity;
                paper.nextRotation();
            }
        }
        return true;
    }

    @Override
    @NotNull
    public Item getItemDropped(@NotNull IBlockState state, @NotNull Random rand, int fortune)
    {
        return null;
    }

    @Override
    public boolean removedByPlayer(@NotNull IBlockState state, World world, @NotNull BlockPos pos, @NotNull EntityPlayer player, boolean willHarvest)
    {
        if(!world.isRemote && !player.capabilities.isCreativeMode)
        {
            TileEntity tileEntity = world.getTileEntity(pos);
            if(tileEntity instanceof TileEntityPaper)
            {
                TileEntityPaper paper = (TileEntityPaper) tileEntity;
                ItemStack drop = IPrint.generateItem(paper.getPrint());
                world.spawnEntity(new EntityItem(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, drop));
            }
        }
        return super.removedByPlayer(state, world, pos, player, willHarvest);
    }

    @Override
    public boolean eventReceived(@NotNull IBlockState state, World worldIn, @NotNull BlockPos pos, int id, int param)
    {
        TileEntity tileentity = worldIn.getTileEntity(pos);
        return tileentity != null && tileentity.receiveClientEvent(id, param);
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
        return new BlockStateContainer(this, FACING);
    }

    @Override
    @NotNull
    public EnumBlockRenderType getRenderType(@NotNull IBlockState state)
    {
        return EnumBlockRenderType.ENTITYBLOCK_ANIMATED;
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(@NotNull World worldIn, int meta)
    {
        return new TileEntityPaper();
    }


}

package com.mrcrayfish.device.block;

import com.mrcrayfish.device.MrCrayfishDeviceMod;
import com.mrcrayfish.device.object.Bounds;
import com.mrcrayfish.device.tileentity.TileEntityPrinter;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Author: MrCrayfish & Moodss
 */
@SuppressWarnings("deprecation")
public class BlockPrinter extends BlockDevice.Colored
{
    private static final AxisAlignedBB[] BODY_BOUNDING_BOX = new Bounds(5 * 0.0625, 0.0, 1 * 0.0625, 14 * 0.0625, 5 * 0.0625, 15 * 0.0625).getRotatedBounds();
    private static final AxisAlignedBB[] TRAY_BOUNDING_BOX = new Bounds(0.5 * 0.0625, 0.0, 3.5 * 0.0625, 5 * 0.0625, 1 * 0.0625, 12.5 * 0.0625).getRotatedBounds();
    private static final AxisAlignedBB[] PAPER_BOUNDING_BOX = new Bounds(13 * 0.0625, 0.0, 4 * 0.0625, 1.0, 9 * 0.0625, 12 * 0.0625).getRotatedBounds();

    private static final AxisAlignedBB SELECTION_BOUNDING_BOX = new AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 0.5, 1.0);

    public BlockPrinter()
    {
        super(Material.ANVIL);
        this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
        this.setCreativeTab(MrCrayfishDeviceMod.TAB_DEVICE);
        this.setTranslationKey("printer");
        this.setRegistryName("printer");
    }

    @Override
    @NotNull
    public AxisAlignedBB getBoundingBox(@NotNull IBlockState state, @NotNull IBlockAccess source, @NotNull BlockPos pos)
    {
        return SELECTION_BOUNDING_BOX;
    }

    @Override
    public void addCollisionBoxToList(IBlockState state, @NotNull World worldIn, @NotNull BlockPos pos, @NotNull AxisAlignedBB entityBox, @NotNull List<AxisAlignedBB> collidingBoxes, Entity entityIn, boolean p_185477_7_)
    {
        EnumFacing facing = state.getValue(FACING);
        Block.addCollisionBoxToList(pos, entityBox, collidingBoxes, BODY_BOUNDING_BOX[facing.getHorizontalIndex()]);
        Block.addCollisionBoxToList(pos, entityBox, collidingBoxes, TRAY_BOUNDING_BOX[facing.getHorizontalIndex()]);
        Block.addCollisionBoxToList(pos, entityBox, collidingBoxes, PAPER_BOUNDING_BOX[facing.getHorizontalIndex()]);
    }

    @Override
    public boolean onBlockActivated(World worldIn, @NotNull BlockPos pos, @NotNull IBlockState state, EntityPlayer playerIn, @NotNull EnumHand hand, @NotNull EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        ItemStack heldItem = playerIn.getHeldItem(hand);
        TileEntity tileEntity = worldIn.getTileEntity(pos);
        if(tileEntity instanceof TileEntityPrinter)
        {
            return ((TileEntityPrinter) tileEntity).addPaper(heldItem, playerIn.isSneaking());
        }
        return false;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(@NotNull World world, @NotNull IBlockState state)
    {
        return new TileEntityPrinter();
    }
}

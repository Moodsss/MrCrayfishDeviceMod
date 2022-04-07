package com.mrcrayfish.device.item;

import com.mrcrayfish.device.Reference;
import net.minecraft.block.Block;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import org.apache.commons.lang3.text.WordUtils;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Author: MrCrayfish
 */
public class ItemColoredDevice extends ItemDevice implements SubItems
{
    public static TextFormatting getFromColor(EnumDyeColor color)
    {
        switch(color)
        {
            case ORANGE:
            case BROWN:
                return TextFormatting.GOLD;
            case MAGENTA:
            case PINK:
                return TextFormatting.LIGHT_PURPLE;
            case LIGHT_BLUE: return TextFormatting.BLUE;
            case YELLOW: return TextFormatting.YELLOW;
            case LIME: return TextFormatting.GREEN;
            case GRAY: return TextFormatting.DARK_GRAY;
            case SILVER: return TextFormatting.GRAY;
            case CYAN: return TextFormatting.DARK_AQUA;
            case PURPLE: return TextFormatting.DARK_PURPLE;
            case BLUE: return TextFormatting.DARK_BLUE;
            case GREEN: return TextFormatting.DARK_GREEN;
            case RED: return TextFormatting.DARK_RED;
            case BLACK: return TextFormatting.BLACK;
            default: return TextFormatting.WHITE;
        }
    }

    public ItemColoredDevice(Block block)
    {
        super(block);
        this.setMaxDamage(0);
        this.setHasSubtypes(true);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, @NotNull ITooltipFlag flagIn)
    {
        EnumDyeColor color = EnumDyeColor.byMetadata(stack.getMetadata());
        String colorName = color.getName().replace("_", " ");
        colorName = WordUtils.capitalize(colorName);
        tooltip.add("Color: " + TextFormatting.BOLD + getFromColor(color).toString() + colorName);
    }

    @Override
    public void getSubItems(@NotNull CreativeTabs tab, @NotNull NonNullList<ItemStack> items)
    {
        if(isInCreativeTab(tab))
        {
            for(EnumDyeColor color : EnumDyeColor.values())
            {
                items.add(new ItemStack(this, 1, color.getMetadata()));
            }
        }
    }

    @Override
    public NonNullList<ResourceLocation> getModels()
    {
        NonNullList<ResourceLocation> modelLocations = NonNullList.create();
        for(EnumDyeColor color : EnumDyeColor.values())
        {
            modelLocations.add(new ResourceLocation(Reference.MOD_ID, getTranslationKey().substring(5) + "/" + color.getName()));
        }
        return modelLocations;
    }
}

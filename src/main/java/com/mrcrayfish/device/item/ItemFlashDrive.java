package com.mrcrayfish.device.item;

import com.mrcrayfish.device.MrCrayfishDeviceMod;
import com.mrcrayfish.device.Reference;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import org.apache.commons.lang3.text.WordUtils;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

import static com.mrcrayfish.device.item.ItemColoredDevice.getFromColor;

/**
 * Author: MrCrayfish
 */
public class ItemFlashDrive extends Item implements SubItems
{
    public ItemFlashDrive()
    {
        this.setTranslationKey("flash_drive");
        this.setRegistryName("flash_drive");
        this.setCreativeTab(MrCrayfishDeviceMod.TAB_DEVICE);
        this.setMaxStackSize(1);
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

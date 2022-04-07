package com.mrcrayfish.device.jei;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.item.EnumDyeColor;

import java.util.List;

/**
 * Author: MrCrayfish
 */
public class LaptopRecipeMaker
{
    public static List<LaptopRecipeWrapper> getLaptopRecipes()
    {
        List<LaptopRecipeWrapper> recipes = new ObjectArrayList<>();
        for(EnumDyeColor color : EnumDyeColor.values())
        {
            recipes.add(new LaptopRecipeWrapper(color));
        }
        return recipes;
    }

    public LaptopRecipeMaker() {}
}

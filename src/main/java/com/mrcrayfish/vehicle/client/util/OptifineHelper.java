package com.mrcrayfish.vehicle.client.util;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.world.item.ItemStack;

import java.lang.reflect.InvocationTargetException;

public class OptifineHelper
{
    private static final boolean LOADED = Package.getPackage("net.optifine") != null;

    public static boolean isLoaded()
    {
        return LOADED;
    }

    public static boolean isEmissiveTexturesEnabled()
    {
        try
        {
            Class<?> clazz = Class.forName("net.optifine.EmissiveTextures");
            return isLoaded() && (boolean) clazz.getDeclaredMethod("isActive").invoke(clazz, new Object[0]);
        }
        catch(ClassNotFoundException ignored)
        {}
        catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException ex)
        {
            ex.printStackTrace();
        }

        return false;
    }

    public static boolean isCustomColorsEnabled()
    {
        try
        {
            Class<?> clazz = Class.forName("net.optifine.Config");
            return isLoaded() && (boolean) clazz.getDeclaredMethod("isCustomColors").invoke(clazz, new Object[0]);
        }
        catch(ClassNotFoundException ignored)
        {}
        catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException ex)
        {
            ex.printStackTrace();
        }

        return false;
    }

    public static int castAsCustomColor(ItemStack stack, int layer, int color)
    {
        try
        {
            Class<?> clazz = Class.forName("net.optifine.CustomColors");
            if(isLoaded())
            {
                return (int) clazz.getDeclaredMethod("getColorFromItemStack", ItemStack.class, int.class, int.class).invoke(clazz, stack, layer, color);
            }
        }
        catch(ClassNotFoundException ignored)
        {}
        catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException ex)
        {
            ex.printStackTrace();
        }
        return color;
    }

    public static BakedQuad castAsEmissive(BakedQuad quad)
    {
        try
        {
            Class<?> clazz = Class.forName("net.optifine.EmissiveTextures");
            return (BakedQuad) clazz.getDeclaredMethod("getEmissiveQuad", BakedQuad.class).invoke(clazz, quad);
        }
        catch(ClassNotFoundException ignored)
        {}
        catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException ex)
        {
            ex.printStackTrace();
        }

        return quad;
    }
}

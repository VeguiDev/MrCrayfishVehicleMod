package com.mrcrayfish.vehicle.client.util;

import net.minecraft.client.renderer.block.model.BakedQuad;

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
            return isLoaded() && (boolean) clazz.getDeclaredMethod("isActive", clazz).invoke(null, clazz);
        }
        catch(ClassNotFoundException ignored)
        {}
        catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException ex)
        {
            ex.printStackTrace();
        }

        return false;
    }

    public static BakedQuad castAsEmissive(BakedQuad quad)
    {
        try
        {
            Class<?> clazz = Class.forName("net.optifine.EmissiveTextures");
            return (BakedQuad) clazz.getDeclaredMethod("getEmissiveQuad", BakedQuad.class).invoke(null, quad);
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

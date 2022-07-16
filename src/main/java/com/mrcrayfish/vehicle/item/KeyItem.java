package com.mrcrayfish.vehicle.item;

import com.mrcrayfish.vehicle.util.RenderUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Author: MrCrayfish
 */
public class KeyItem extends Item
{
    public KeyItem(Item.Properties properties)
    {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack p_41421_, @Nullable Level p_41422_, List<Component> tooltips, TooltipFlag p_41424_)
    {
        if(Screen.hasShiftDown())
        {
            tooltips.addAll(RenderUtil.lines(new TranslatableComponent(this.getDescriptionId() + ".info"), 150));
        }
        else
        {
            tooltips.add(new TranslatableComponent("vehicle.info_help").withStyle(ChatFormatting.YELLOW));
        }
    }
}

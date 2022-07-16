package com.mrcrayfish.vehicle.item;

import com.mrcrayfish.vehicle.VehicleMod;
import com.mrcrayfish.vehicle.util.RenderUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;

/**
 * Author: MrCrayfish
 */
public class ItemTrafficCone extends BlockItem
{
    public ItemTrafficCone(Supplier<Block> block)
    {
        super(block.get(), new Item.Properties().tab(VehicleMod.CREATIVE_TAB));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltips, TooltipFlag flag)
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

    @Nullable
    @Override
    public EquipmentSlot getEquipmentSlot(ItemStack stack)
    {
        return EquipmentSlot.HEAD;
    }
}

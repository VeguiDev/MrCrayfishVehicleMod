package com.mrcrayfish.vehicle.block;

import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

@SuppressWarnings("deprecation")
public abstract class RotatedEntityObjectBlock extends RotatedObjectBlock implements EntityBlock
{
    public RotatedEntityObjectBlock(Properties properties)
    {
        super(properties);
    }

    @Override
    @NotNull
    public RenderShape getRenderShape(@NotNull BlockState state)
    {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<A> createTickerHelper(BlockEntityType<A> first, BlockEntityType<E> second, BlockEntityTicker<? super E> ticker)
    {
        return second == first ? (BlockEntityTicker<A>) ticker : null;
    }
}

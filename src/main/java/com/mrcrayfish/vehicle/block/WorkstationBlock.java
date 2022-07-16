package com.mrcrayfish.vehicle.block;

import com.mrcrayfish.vehicle.tileentity.WorkstationTileEntity;
import com.mrcrayfish.vehicle.util.VoxelShapeHelper;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * Author: MrCrayfish
 */
public class WorkstationBlock extends RotatedObjectBlock implements EntityBlock
{
    private static final VoxelShape SHAPE = Util.make(() -> {
        List<VoxelShape> shapes = new ArrayList<>();
        shapes.add(box(0, 1, 0, 16, 16, 16));
        shapes.add(box(1, 0, 1, 3, 1, 3));
        shapes.add(box(1, 0, 13, 3, 1, 15));
        shapes.add(box(13, 0, 1, 15, 1, 3));
        shapes.add(box(13, 0, 13, 15, 1, 15));
        return VoxelShapeHelper.combineAll(shapes);
    });

    public WorkstationBlock()
    {
        super(Properties.of(Material.METAL).strength(1.0F));
    }

    @Override
    public VoxelShape getShape(BlockState p_60555_, BlockGetter p_60556_, BlockPos p_60557_, CollisionContext p_60558_) {
        return SHAPE;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result)
    {
        if(!level.isClientSide)
        {
            BlockEntity tileEntity = level.getBlockEntity(pos);
            if(tileEntity instanceof MenuProvider)
            {
                NetworkHooks.openGui((ServerPlayer) player, (MenuProvider) tileEntity, pos);
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return new WorkstationTileEntity(pos, state);
    }
}

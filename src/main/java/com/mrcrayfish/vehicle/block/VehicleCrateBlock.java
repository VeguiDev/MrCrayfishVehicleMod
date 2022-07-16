package com.mrcrayfish.vehicle.block;

import com.google.common.base.Strings;
import com.mrcrayfish.vehicle.init.ModBlocks;
import com.mrcrayfish.vehicle.init.ModItems;
import com.mrcrayfish.vehicle.init.ModTileEntities;
import com.mrcrayfish.vehicle.tileentity.VehicleCrateTileEntity;
import com.mrcrayfish.vehicle.util.RenderUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.TerrainParticle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Author: MrCrayfish
 */
public class VehicleCrateBlock extends RotatedObjectBlock implements EntityBlock
{
    public static final List<ResourceLocation> REGISTERED_CRATES = new ArrayList<>();
    private static final VoxelShape PANEL = box(0, 0, 0, 16, 2, 16);

    public VehicleCrateBlock()
    {
        super(Properties.of(Material.METAL, DyeColor.LIGHT_GRAY).dynamicShape().noOcclusion().strength(1.5F, 5.0F));
    }

    @Override
    public void fillItemCategory(CreativeModeTab group, NonNullList<ItemStack> items)
    {
        REGISTERED_CRATES.forEach(resourceLocation ->
        {
            CompoundTag blockEntityTag = new CompoundTag();
            blockEntityTag.putString("Vehicle", resourceLocation.toString());
            blockEntityTag.putBoolean("Creative", true);
            CompoundTag itemTag = new CompoundTag();
            itemTag.put("BlockEntityTag", blockEntityTag);
            ItemStack stack = new ItemStack(ModBlocks.VEHICLE_CRATE.get());
            stack.setTag(itemTag);
            items.add(stack);
        });
    }

    @Override
    public boolean propagatesSkylightDown(BlockState p_49928_, BlockGetter p_49929_, BlockPos p_49930_) {
        return true;
    }

    @Override
    public VoxelShape getShape(BlockState p_60555_, BlockGetter level, BlockPos pos, CollisionContext p_60558_) {
        BlockEntity te = level.getBlockEntity(pos);
        if(te instanceof VehicleCrateTileEntity && ((VehicleCrateTileEntity)te).isOpened())
            return PANEL;
        return Shapes.block();
    }

    @Override
    public boolean canSurvive(BlockState p_60525_, LevelReader level, BlockPos pos) {
        return this.isBelowBlockTopSolid(level, pos) && this.canOpen(level, pos);
    }

    private boolean canOpen(LevelReader reader, BlockPos pos)
    {
        for(Direction side : Direction.Plane.HORIZONTAL)
        {
            BlockPos adjacentPos = pos.relative(side);
            BlockState state = reader.getBlockState(adjacentPos);
            if(state.isAir())
                continue;
            if(!state.getMaterial().isReplaceable() || this.isBelowBlockTopSolid(reader, adjacentPos))
            {
                return false;
            }
        }
        return true;
    }

    private boolean isBelowBlockTopSolid(LevelReader reader, BlockPos pos)
    {
        return reader.getBlockState(pos.below()).isFaceSturdy(reader, pos.below(), Direction.UP);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result)
    {
        if(result.getDirection() == Direction.UP && player.getItemInHand(hand).getItem() == ModItems.WRENCH.get())
        {
            this.openCrate(level, pos, state, player);
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity entity, ItemStack stack)
    {
        if(entity instanceof Player && ((Player) entity).isCreative())
        {
            this.openCrate(level, pos, state, entity);
        }
    }

    private void openCrate(LevelReader world, BlockPos pos, BlockState state, LivingEntity placer)
    {
        BlockEntity tileEntity = world.getBlockEntity(pos);
        if(tileEntity instanceof VehicleCrateTileEntity && this.canOpen(world, pos))
        {
            if(world.isClientSide())
            {
                this.spawnCrateOpeningParticles((ClientLevel) world, pos, state);
            }
            else
            {
                ((VehicleCrateTileEntity) tileEntity).open(placer.getUUID());
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void spawnCrateOpeningParticles(ClientLevel world, BlockPos pos, BlockState state)
    {
        double y = 0.875;
        double x, z;
        TerrainParticle.Provider provider = new TerrainParticle.Provider();
        for(int j = 0; j < 4; ++j)
        {
            for(int l = 0; l < 4; ++l)
            {
                x = (j + 0.5D) / 4.0D;
                z = (l + 0.5D) / 4.0D;

                Minecraft.getInstance().particleEngine.add(provider.createParticle(new BlockParticleOption(ParticleTypes.BLOCK, state), world, pos.getX() + x, pos.getY() + y, pos.getZ() + z, x - 0.5D, y - 0.5D, z - 0.5D));
            }
        }
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return new VehicleCrateTileEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type)
    {
        if(level.isClientSide)
        {
            return createTickerHelper(type, ModTileEntities.VEHICLE_CRATE.get(), VehicleCrateTileEntity::onClientTick);
        }

        return createTickerHelper(type, ModTileEntities.VEHICLE_CRATE.get(), VehicleCrateTileEntity::onServerTick);
    }

    @Nullable
    protected static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<A> createTickerHelper(BlockEntityType<A> p_152133_, BlockEntityType<E> p_152134_, BlockEntityTicker<? super E> p_152135_) {
        return p_152134_ == p_152133_ ? (BlockEntityTicker<A>)p_152135_ : null;
    }

    @Override
    public RenderShape getRenderShape(BlockState state)
    {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable BlockGetter level, @NotNull List<Component> tooltips, @NotNull TooltipFlag flag)
    {
        super.appendHoverText(stack, level, tooltips, flag);

        Component vehicleName = EntityType.PIG.getDescription();
        CompoundTag tagCompound = stack.getTag();
        if(tagCompound != null)
        {
            if(tagCompound.contains("BlockEntityTag", CompoundTag.TAG_COMPOUND))
            {
                CompoundTag blockEntityTag = tagCompound.getCompound("BlockEntityTag");
                String entityType = blockEntityTag.getString("Vehicle");
                if(!Strings.isNullOrEmpty(entityType))
                {
                    vehicleName = EntityType.byString(entityType).orElse(EntityType.PIG).getDescription();
                }
            }
        }

        if(Screen.hasShiftDown())
        {
            tooltips.addAll(RenderUtil.lines(new TranslatableComponent(this.getDescriptionId() + ".info", vehicleName), 150));
        }
        else
        {
            tooltips.add(vehicleName.copy().withStyle(ChatFormatting.BLUE));
            tooltips.add(new TranslatableComponent("vehicle.info_help").withStyle(ChatFormatting.YELLOW));
        }
    }

    public static ItemStack create(ResourceLocation entityId, int color, ItemStack engine, ItemStack wheel)
    {
        CompoundTag blockEntityTag = new CompoundTag();
        blockEntityTag.putString("Vehicle", entityId.toString());
        blockEntityTag.putInt("Color", color);
        blockEntityTag.put("EngineStack", engine.save(new CompoundTag()));
        blockEntityTag.put("WheelStack", wheel.save(new CompoundTag()));
        CompoundTag itemTag = new CompoundTag();
        itemTag.put("BlockEntityTag", blockEntityTag);
        ItemStack stack = new ItemStack(ModBlocks.VEHICLE_CRATE.get());
        stack.setTag(itemTag);
        return stack;
    }

    public static synchronized void registerVehicle(ResourceLocation id)
    {
        if(!REGISTERED_CRATES.contains(id))
        {
            REGISTERED_CRATES.add(id);
            Collections.sort(REGISTERED_CRATES);
        }
    }
}

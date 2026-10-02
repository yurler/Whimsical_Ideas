package com.whimsical.ideas;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class PlushieBlock extends Block implements EntityBlock {

    public static final IntegerProperty ROTATION = IntegerProperty.create("rotation", 0, 7);

    public PlushieBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(ROTATION, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ROTATION);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        float yaw = context.getPlayer() != null
                ? context.getPlayer().getYRot()
                : context.getHorizontalDirection().toYRot();

        int index = (int) Math.round(-yaw / 45.0) & 7;
        return this.defaultBlockState().setValue(ROTATION, index);
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        int step = rotation == Rotation.CLOCKWISE_90 ? 2
                : rotation == Rotation.CLOCKWISE_180 ? 4
                : rotation == Rotation.COUNTERCLOCKWISE_90 ? 6
                : 0;
        return state.setValue(ROTATION, (state.getValue(ROTATION) + step) & 7);
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos,
                                 Player player, InteractionHand hand, BlockHitResult hit) {
        if (hand != InteractionHand.MAIN_HAND) {
            return super.use(state, level, pos, player, hand, hit);
        }
        int current = state.getValue(ROTATION);
        int next = player.isShiftKeyDown()
                ? (current + 7) & 7
                : (current + 1) & 7;
        if (!level.isClientSide) {
            level.setBlock(pos, state.setValue(ROTATION, next), 3);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PlushieBlockEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state,
                            @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (level.isClientSide) return;

        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof PlushieBlockEntity plushie) {
            PlushieData data = PlushieData.fromItemStack(stack);
            if (data != null) {
                plushie.setData(data);
            }
        }
    }

    @Override
    public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state,
                              @Nullable BlockEntity blockEntity, ItemStack tool) {
        super.playerDestroy(level, player, pos, state, blockEntity, tool);

        if (level.isClientSide) return;

        if (blockEntity instanceof PlushieBlockEntity plushie) {
            ItemStack drop = new ItemStack(WhimsicalIdeas.PLUSHIE_ITEM.get());

            PlushieData data = plushie.getData();
            if (data != null) {
                data.writeToItemStack(drop);
            }

            Block.popResource(level, pos, drop);
        }
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {
        return (lvl, pos, st, be) -> {
            if (be instanceof PlushieBlockEntity plushie) {
                if (lvl.isClientSide) {
                    PlushieBlockEntity.clientTick(lvl, pos, st, plushie);
                } else {
                    PlushieBlockEntity.serverTick(lvl, pos, st, plushie);
                }
            }
        };
    }
}
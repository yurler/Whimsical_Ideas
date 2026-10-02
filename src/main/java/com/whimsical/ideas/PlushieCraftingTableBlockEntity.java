package com.whimsical.ideas;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class PlushieCraftingTableBlockEntity extends BlockEntity {
    public PlushieCraftingTableBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.PLUSHIE_CRAFTING_TABLE_BE.get(), pos, state);
    }
}
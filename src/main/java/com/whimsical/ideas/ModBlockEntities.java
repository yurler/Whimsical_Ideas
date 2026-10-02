package com.whimsical.ideas;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, WhimsicalIdeas.MOD_ID);

    public static final RegistryObject<BlockEntityType<PlushieBlockEntity>> PLUSHIE_BE =
            BLOCK_ENTITIES.register("plushie_be", () ->
                    BlockEntityType.Builder.of(PlushieBlockEntity::new,
                            WhimsicalIdeas.PLUSHIE_BLOCK.get()).build(null));

    public static final RegistryObject<BlockEntityType<PlushieCraftingTableBlockEntity>> PLUSHIE_CRAFTING_TABLE_BE =
            BLOCK_ENTITIES.register("plushie_crafting_table_be", () ->
                    BlockEntityType.Builder.of(PlushieCraftingTableBlockEntity::new,
                            WhimsicalIdeas.PLUSHIE_CRAFTING_TABLE_BLOCK.get()).build(null));

    public static void register(IEventBus bus) {
        BLOCK_ENTITIES.register(bus);
    }
}
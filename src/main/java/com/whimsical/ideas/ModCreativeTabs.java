package com.whimsical.ideas;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModCreativeTabs {

    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, WhimsicalIdeas.MOD_ID);

    public static final RegistryObject<CreativeModeTab> SIMPLE_MODELS_TAB =
            TABS.register("simple_models", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.whimsical_ideas.simple_models"))
                    .icon(() -> new ItemStack(WhimsicalIdeas.PLUSHIE_ITEM.get()))
                    .displayItems((params, output) -> {
                        output.accept(WhimsicalIdeas.PLUSHIE_ITEM.get());
                        output.accept(WhimsicalIdeas.BLANK_PLUSHIE_ITEM.get());
                        output.accept(WhimsicalIdeas.PLUSHIE_CRAFTING_TABLE_ITEM.get());
                    })
                    .build());
}
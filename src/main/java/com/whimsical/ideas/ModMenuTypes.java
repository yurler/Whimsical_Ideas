package com.whimsical.ideas;

import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModMenuTypes {

    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, WhimsicalIdeas.MOD_ID);

    public static final RegistryObject<MenuType<PlushieCraftingTableMenu>> PLUSHIE_CRAFTING_TABLE =
            MENUS.register("plushie_crafting_table", () ->
                    IForgeMenuType.create(PlushieCraftingTableMenu::new));
}
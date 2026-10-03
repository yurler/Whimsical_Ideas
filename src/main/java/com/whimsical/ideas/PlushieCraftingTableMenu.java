package com.whimsical.ideas;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class PlushieCraftingTableMenu extends AbstractContainerMenu {

    public static final int SLOT_INPUT = 0;
    public static final int SLOT_OUTPUT = 1;

    private final Container container;
    private final BlockPos pos;

    public PlushieCraftingTableMenu(int id, Inventory playerInv, FriendlyByteBuf buf) {
        this(id, playerInv, new SimpleContainer(2), buf.readBlockPos());
    }

    public PlushieCraftingTableMenu(int id, Inventory playerInv, Container container, BlockPos pos) {
        super(ModMenuTypes.PLUSHIE_CRAFTING_TABLE.get(), id);
        this.container = container;
        this.pos = pos;

        container.startOpen(playerInv.player);

        // 输入槽：图片 (7, 14)
        this.addSlot(new Slot(container, SLOT_INPUT, 8, 15) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.getItem() instanceof BlankPlushieItem;
            }
        });

        // 输出槽：图片 (7, 52)
        this.addSlot(new Slot(container, SLOT_OUTPUT, 8, 53) {
            @Override
            public boolean mayPlace(ItemStack stack) { return false; }
        });

        // 玩家背包
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, 86 + row * 18));
            }
        }
        // 快捷栏
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInv, col, 8 + col * 18, 144));
        }
    }

    @Nullable
    public BlockPos getPos() { return pos; }

    public Container getContainer() { return container; }

    @Override
    public boolean stillValid(Player player) {
        return container.stillValid(player);
    }

    public void craftPlushie(String ownerName, Player player) {
        if (player.level().isClientSide) return;
        if (ownerName == null || ownerName.trim().isEmpty()) return;

        ItemStack input = container.getItem(SLOT_INPUT);
        if (input.isEmpty() || !(input.getItem() instanceof BlankPlushieItem)) return;
        if (!container.getItem(SLOT_OUTPUT).isEmpty()) return;

        input.shrink(1);

        ItemStack result = new ItemStack(WhimsicalIdeas.PLUSHIE_ITEM.get());

        PlushieData data;
        if (ownerName.startsWith("local:")) {
            String fileName = ownerName.substring("local:".length());
            data = new PlushieData("", "upload");
            data.setUploadFile(fileName);
            result.setHoverName(Component.literal(fileName.replace(".png", "") + "的玩偶"));
        } else {
            data = new PlushieData(ownerName.trim(), "mojang");
            result.setHoverName(Component.literal(ownerName.trim() + "的玩偶"));
        }

        data.writeToItemStack(result);
        container.setItem(SLOT_OUTPUT, result);
        container.setChanged();
        this.broadcastChanges();
    }

    public void receiveSkinData(String key, byte[] skinData, Player player) {
        if (player.level().isClientSide) return;
        if (skinData == null || skinData.length == 0) return;

        ItemStack output = container.getItem(SLOT_OUTPUT);
        if (output.isEmpty()) return;
        if (!(output.getItem() instanceof PlushieItem)) return;

        PlushieData data = PlushieData.fromItemStack(output);
        if (data == null) return;

        if (key.startsWith("local:")) {
            String fileName = key.substring("local:".length());
            if (!fileName.equals(data.getUploadFile())) return;
        } else {
            if (!key.equals(data.getOwnerName())) return;
        }

        data.setSkinData(skinData);
        data.writeToItemStack(output);
        container.setChanged();
        this.broadcastChanges();
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.container.stopOpen(player);
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (!stack.isEmpty()) {
                if (!player.getInventory().add(stack)) {
                    player.drop(stack, false);
                }
                container.setItem(i, ItemStack.EMPTY);
            }
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }
}
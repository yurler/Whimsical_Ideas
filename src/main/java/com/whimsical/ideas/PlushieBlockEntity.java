package com.whimsical.ideas;

import com.whimsical.ideas.client.SkinFetcher;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class PlushieBlockEntity extends BlockEntity {

    private PlushieData data = new PlushieData();

    private String lastSkinKey = null;
    private boolean skinRefreshed = false;

    public PlushieBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.PLUSHIE_BE.get(), pos, state);
    }

    public PlushieData getData() { return data; }

    public void setData(PlushieData data) {
        this.data = data == null ? new PlushieData() : data;
        this.setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        data.writeTo(tag);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.data = PlushieData.load(tag);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, PlushieBlockEntity be) {
        PlushieData data = be.getData();

        if (data.hasSkinData()) return;

        String key;
        if ("upload".equals(data.getSkinSource()) && !data.getUploadFile().isEmpty()) {
            key = "local:" + data.getUploadFile();
        } else {
            key = data.getOwnerName();
        }

        if (key == null || key.isEmpty()) return;

        if (!key.equals(be.lastSkinKey)) {
            be.lastSkinKey = key;
            be.skinRefreshed = false;
        }

        if (be.skinRefreshed) return;

        boolean ready = "upload".equals(data.getSkinSource())
                ? SkinFetcher.isLocalReady(data.getUploadFile())
                : SkinFetcher.isReady(data.getOwnerName());

        if (ready) {
            be.skinRefreshed = true;
            Minecraft mc = Minecraft.getInstance();
            if (mc.levelRenderer != null) {
                mc.levelRenderer.setSectionDirty(
                        pos.getX() >> 4,
                        pos.getY() >> 4,
                        pos.getZ() >> 4
                );
            }
        }
    }

    @SuppressWarnings("unused")
    public static void serverTick(Level level, BlockPos pos, BlockState state, PlushieBlockEntity be) {
    }
}
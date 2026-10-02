package com.whimsical.ideas;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class PlushieData {

    public static final String TAG_ROOT = "PlushieData";
    public static final String TAG_OWNER = "OwnerName";
    public static final String TAG_SOURCE = "SkinSource";
    public static final String TAG_UPLOAD_FILE = "UploadFile";
    public static final String TAG_SKIN_DATA = "SkinData";

    private String ownerName = "";
    private String skinSource = "mojang";
    private String uploadFile = "";
    private byte[] skinData = null;

    public PlushieData() {}

    public PlushieData(String ownerName, String skinSource) {
        this.ownerName = ownerName == null ? "" : ownerName;
        this.skinSource = skinSource == null ? "mojang" : skinSource;
    }

    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName == null ? "" : ownerName; }

    public String getDisplayName() {
        if (ownerName == null || ownerName.isEmpty()) return "";
        return ownerName + "的玩偶";
    }

    public String getSkinSource() { return skinSource; }
    public void setSkinSource(String skinSource) { this.skinSource = skinSource == null ? "mojang" : skinSource; }

    public String getUploadFile() { return uploadFile; }
    public void setUploadFile(String uploadFile) { this.uploadFile = uploadFile == null ? "" : uploadFile; }

    public byte[] getSkinData() { return skinData; }
    public void setSkinData(byte[] skinData) { this.skinData = skinData; }
    public boolean hasSkinData() { return skinData != null && skinData.length > 0; }

    public boolean isEmpty() { return ownerName == null || ownerName.isEmpty(); }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putString(TAG_OWNER, ownerName);
        tag.putString(TAG_SOURCE, skinSource);
        tag.putString(TAG_UPLOAD_FILE, uploadFile);
        if (skinData != null && skinData.length > 0) {
            tag.putByteArray(TAG_SKIN_DATA, skinData);
        }
        return tag;
    }

    public static PlushieData load(CompoundTag parent) {
        if (parent == null || !parent.contains(TAG_ROOT, 10)) {
            return new PlushieData();
        }
        CompoundTag tag = parent.getCompound(TAG_ROOT);
        PlushieData data = new PlushieData();
        data.ownerName = tag.getString(TAG_OWNER);
        data.skinSource = tag.contains(TAG_SOURCE) ? tag.getString(TAG_SOURCE) : "mojang";
        data.uploadFile = tag.contains(TAG_UPLOAD_FILE) ? tag.getString(TAG_UPLOAD_FILE) : "";
        data.skinData = tag.contains(TAG_SKIN_DATA) ? tag.getByteArray(TAG_SKIN_DATA) : null;
        return data;
    }

    public void writeTo(CompoundTag parent) {
        parent.put(TAG_ROOT, save());
    }

    @Nullable
    public static PlushieData fromItemStack(ItemStack stack) {
        if (!stack.hasTag()) return null;
        return load(stack.getTag());
    }

    public void writeToItemStack(ItemStack stack) {
        writeTo(stack.getOrCreateTag());
    }

    public PlushieData copy() {
        PlushieData copy = new PlushieData(ownerName, skinSource);
        copy.uploadFile = this.uploadFile;
        copy.skinData = this.skinData == null ? null : this.skinData.clone();
        return copy;
    }
}
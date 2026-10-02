package com.whimsical.ideas.network;

import com.whimsical.ideas.PlushieCraftingTableMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SkinUploadPacket {

    private final String ownerName;
    private final byte[] skinData;

    public SkinUploadPacket(String ownerName, byte[] skinData) {
        this.ownerName = ownerName;
        this.skinData = skinData;
    }

    public static void encode(SkinUploadPacket msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.ownerName, 64);
        buf.writeByteArray(msg.skinData);
    }

    public static SkinUploadPacket decode(FriendlyByteBuf buf) {
        String name = buf.readUtf(64);
        byte[] data = buf.readByteArray();
        return new SkinUploadPacket(name, data);
    }

    public static void handle(SkinUploadPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;
            if (player.containerMenu instanceof PlushieCraftingTableMenu menu) {
                menu.receiveSkinData(msg.ownerName, msg.skinData, player);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
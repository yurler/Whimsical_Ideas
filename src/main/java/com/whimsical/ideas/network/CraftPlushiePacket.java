package com.whimsical.ideas.network;

import com.whimsical.ideas.PlushieCraftingTableMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class CraftPlushiePacket {

    private final String ownerName;

    public CraftPlushiePacket(String ownerName) {
        this.ownerName = ownerName;
    }

    public static void encode(CraftPlushiePacket msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.ownerName, 32);
    }

    public static CraftPlushiePacket decode(FriendlyByteBuf buf) {
        return new CraftPlushiePacket(buf.readUtf(32));
    }

    public static void handle(CraftPlushiePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;
            if (player.containerMenu instanceof PlushieCraftingTableMenu menu) {
                menu.craftPlushie(msg.ownerName, player);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
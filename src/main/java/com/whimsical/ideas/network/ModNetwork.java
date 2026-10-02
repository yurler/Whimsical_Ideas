package com.whimsical.ideas.network;

import com.whimsical.ideas.WhimsicalIdeas;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class ModNetwork {

    public static final String PROTOCOL = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(WhimsicalIdeas.MOD_ID, "main"),
            () -> PROTOCOL,
            PROTOCOL::equals,
            PROTOCOL::equals
    );

    public static void register() {
        int id = 0;

        CHANNEL.registerMessage(
                id++,
                CraftPlushiePacket.class,
                CraftPlushiePacket::encode,
                CraftPlushiePacket::decode,
                CraftPlushiePacket::handle
        );

        CHANNEL.registerMessage(
                id++,
                SkinUploadPacket.class,
                SkinUploadPacket::encode,
                SkinUploadPacket::decode,
                SkinUploadPacket::handle
        );
    }
}
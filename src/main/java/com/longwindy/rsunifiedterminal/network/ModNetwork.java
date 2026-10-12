package com.longwindy.rsunifiedterminal.network;

import com.longwindy.rsunifiedterminal.RSUnifiedTerminal;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public final class ModNetwork {
    private static final String PROTOCOL_VERSION = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
        new ResourceLocation(RSUnifiedTerminal.MOD_ID, "main"),
        () -> PROTOCOL_VERSION,
        PROTOCOL_VERSION::equals,
        PROTOCOL_VERSION::equals
    );

    private ModNetwork() {
    }

    public static void register() {
        int id = 0;

        CHANNEL.messageBuilder(OpenTerminalMessage.class, id++, NetworkDirection.PLAY_TO_SERVER)
            .encoder(OpenTerminalMessage::encode).decoder(OpenTerminalMessage::decode)
            .consumerMainThread(OpenTerminalMessage::handle).add();
        CHANNEL.messageBuilder(ToggleFluidViewMessage.class, id++, NetworkDirection.PLAY_TO_SERVER)
            .encoder(ToggleFluidViewMessage::encode).decoder(ToggleFluidViewMessage::decode)
            .consumerMainThread(ToggleFluidViewMessage::handle).add();
        CHANNEL.messageBuilder(GridSettingsMessage.class, id++, NetworkDirection.PLAY_TO_SERVER)
            .encoder(GridSettingsMessage::encode).decoder(GridSettingsMessage::decode)
            .consumerMainThread(GridSettingsMessage::handle).add();
        CHANNEL.messageBuilder(GridParameterMessage.class, id++, NetworkDirection.PLAY_TO_SERVER)
            .encoder(GridParameterMessage::encode).decoder(GridParameterMessage::decode)
            .consumerMainThread(GridParameterMessage::handle).add();
        CHANNEL.messageBuilder(GridParameterSyncMessage.class, id++, NetworkDirection.PLAY_TO_CLIENT)
            .encoder(GridParameterSyncMessage::encode).decoder(GridParameterSyncMessage::decode)
            .consumerMainThread(GridParameterSyncMessage::handle).add();
        CHANNEL.messageBuilder(CreatePatternMessage.class, id++, NetworkDirection.PLAY_TO_SERVER)
            .encoder(CreatePatternMessage::encode).decoder(CreatePatternMessage::decode)
            .consumerMainThread(CreatePatternMessage::handle).add();
        CHANNEL.messageBuilder(CrafterManagerSettingsMessage.class, id++, NetworkDirection.PLAY_TO_SERVER)
            .encoder(CrafterManagerSettingsMessage::encode).decoder(CrafterManagerSettingsMessage::decode)
            .consumerMainThread(CrafterManagerSettingsMessage::handle).add();
        CHANNEL.messageBuilder(ScalePatternMessage.class, id++, NetworkDirection.PLAY_TO_SERVER)
            .encoder(ScalePatternMessage::encode).decoder(ScalePatternMessage::decode)
            .consumerMainThread(ScalePatternMessage::handle).add();
    }

    public static void sendTo(ServerPlayer player, Object message) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), message);
    }
}

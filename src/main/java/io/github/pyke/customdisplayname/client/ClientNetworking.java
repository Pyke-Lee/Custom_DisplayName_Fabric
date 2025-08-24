package io.github.pyke.customdisplayname.client;

import io.github.pyke.customdisplayname.handler.NetworkHandler;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;

@Environment(EnvType.CLIENT)
public final class ClientNetworking {
    public static void sendChangeDisplayName(String text) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeUtf(text, 64);
        ClientPlayNetworking.send(NetworkHandler.CHANGE_DISPLAYNAME, buf);
    }

    private ClientNetworking() { }
}

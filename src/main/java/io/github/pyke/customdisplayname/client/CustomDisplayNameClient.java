package io.github.pyke.customdisplayname.client;

import io.github.pyke.customdisplayname.handler.NetworkHandler;
import io.github.pyke.customdisplayname.item.ChangeDisplayNameUI;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;

public class CustomDisplayNameClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientHooks.OPEN_CHANGE_NAME_SCREEN = () -> Minecraft.getInstance().setScreen(new ChangeDisplayNameUI());
        ClientHooks.READY = true;

        ClientPlayNetworking.registerGlobalReceiver(NetworkHandler.CHANGE_DISPLAYNAME_RESULT, (client, handler, buf, responseSender) -> {
            boolean ok = buf.readBoolean();
            String message = buf.readUtf(128);
            String sanitized = buf.readUtf(64);

            client.execute(() -> ChangeDisplayNameUI.onNetworkResult(ok, message, sanitized));
        });
    }
}

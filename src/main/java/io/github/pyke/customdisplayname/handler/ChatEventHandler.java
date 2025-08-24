package io.github.pyke.customdisplayname.handler;

import io.github.pyke.customdisplayname.CustomDisplayName;
import io.github.pyke.customdisplayname.manager.AliasManager;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;

public class ChatEventHandler {
    public static void register() {
        ServerMessageEvents.ALLOW_CHAT_MESSAGE.register((message, sender, params) -> {
            MinecraftServer server = CustomDisplayName.getServer();
            if (null == server || null == sender) { return true; }

            String raw = message.signedContent().strip();
            Component displayName = AliasManager.getDisplayName(sender.getUUID());
            String left = (null != displayName ? displayName.getString() : sender.getGameProfile().getName());

            Component formatted = Component.literal(left + "§f: " + raw);

            server.getPlayerList().broadcastSystemMessage(formatted, false);
            return false;
        });
    }
}

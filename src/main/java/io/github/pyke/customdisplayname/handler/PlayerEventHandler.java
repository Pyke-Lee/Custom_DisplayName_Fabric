package io.github.pyke.customdisplayname.handler;

import io.github.pyke.customdisplayname.manager.AliasManager;
import io.github.pyke.customdisplayname.util.TabUtil;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class PlayerEventHandler {
    public static void register() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            applyCustomNameTag(handler.getPlayer());
        });

        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            applyCustomNameTag(newPlayer);
        });
    }

    private static void applyCustomNameTag(ServerPlayer player) {
        Component customName = AliasManager.getDisplayName(player.getUUID());
        if (null != customName) {
            player.setCustomName(customName);
            player.setCustomNameVisible(true);
        }
        TabUtil.refreshTabListName(player);
    }
}

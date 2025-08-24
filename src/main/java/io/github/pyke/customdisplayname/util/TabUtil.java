package io.github.pyke.customdisplayname.util;

import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.server.level.ServerPlayer;

import java.util.EnumSet;
import java.util.List;

public class TabUtil {
    private TabUtil() { }

    public static void refreshTabListName(ServerPlayer player) {
        var actions = EnumSet.of(ClientboundPlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME);
        var pkt = new ClientboundPlayerInfoUpdatePacket(actions, List.of(player));
        player.server.getPlayerList().broadcastAll(pkt);
    }
}

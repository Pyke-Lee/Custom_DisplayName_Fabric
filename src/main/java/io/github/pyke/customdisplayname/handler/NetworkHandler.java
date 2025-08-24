package io.github.pyke.customdisplayname.handler;

import io.github.pyke.customdisplayname.CustomDisplayName;
import io.github.pyke.customdisplayname.item.IDCard;
import io.github.pyke.customdisplayname.manager.AliasManager;
import io.github.pyke.customdisplayname.util.TabUtil;
import io.netty.buffer.Unpooled;
import io.netty.buffer.UnpooledDirectByteBuf;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class NetworkHandler {
    public static final ResourceLocation CHANGE_DISPLAYNAME = new ResourceLocation(CustomDisplayName.MOD_ID, "change_displayname");
    public static final ResourceLocation CHANGE_DISPLAYNAME_RESULT = new ResourceLocation(CustomDisplayName.MOD_ID, "change_displayname_result");

    public static void registerServer() {
        ServerPlayNetworking.registerGlobalReceiver(CHANGE_DISPLAYNAME, (server, player, handler, buf, responseSender) -> {
            final String raw = buf.readUtf(64);
            server.execute(() -> handleChangeDisplayName(player, raw));
        });
    }

    private static void handleChangeDisplayName(ServerPlayer player, String clientText) {
        String sanitized = sanitize(clientText, 8);

        if (sanitized.isBlank()) {
            sendResult(player, false, "닉네임이 비어 있습니다.", "");
            return;
        }

        boolean success = AliasManager.setDisplayName(player, sanitized);
        if (success) {
            TabUtil.refreshTabListName(player);

            if (!player.getAbilities().instabuild) {
                boolean removed = consumeIDCard(player);
                CustomDisplayName.LOGGER.debug("ID Card Consume: {}", removed);
            }

            player.sendSystemMessage(Component.literal(CustomDisplayName.SYSTEM_PREFIX + "§f당신의 닉네임이 '" + AliasManager.stripColor(sanitized) + "§f' (으)로 변경되었습니다."));
            sendResult(player, true, "변경되었습니다.", sanitized);
        }
        else {
            player.sendSystemMessage(Component.literal(CustomDisplayName.SYSTEM_PREFIX + "§f이미 사용 중인 닉네임입니다."));
            sendResult(player, false, "이미 사용 중인 닉네임입니다.", "");
        }
    }

    private static String sanitize(String input, int maxCodePoints) {
        if (null == input) { return ""; }

        String filtered = input.codePoints().collect(StringBuilder::new, (sb, cp) -> { if (isAllowed(cp)) sb.appendCodePoint(cp); }, StringBuilder::append).toString();

        int count = filtered.codePointCount(0, filtered.length());
        if (count <= maxCodePoints) { return filtered; }

        int end = filtered.offsetByCodePoints(0, maxCodePoints);
        return filtered.substring(0, end);
    }

    private static boolean isAllowed(int c) {
        if (Character.isWhitespace(c)) { return true; }
        if (c == '_' || c == '-' || c == '&' || c == '§') { return true; }
        if (c >= '0' && c <= '9') { return true; }
        if (c >= 'A' && c <= 'Z') { return true; }
        if (c >= 'a' && c <= 'z') { return true; }

        if (c >= 0xAC00 && c <= 0xD7A3) { return true; } // Hangul Syllables
        if (c >= 0x1100 && c <= 0x11FF) { return true; } // Hangul Jamo
        if (c >= 0x3130 && c <= 0x318F) { return true; } // Compatibility Jamo
        if (c >= 0xA960 && c <= 0xA97F) { return true; } // Jamo Extended-A
        if (c >= 0xD7B0 && c <= 0xD7FF) { return true; } // Jamo Extended-B

        return false;
    }

    private static boolean consumeIDCard(ServerPlayer player) {
        ItemStack mainhand = player.getMainHandItem();
        if (!mainhand.isEmpty() && mainhand.getItem() == IDCard.ID_CARD) {
            mainhand.shrink(1);
            player.containerMenu.broadcastChanges();
            return true;
        }

        ItemStack offhand = player.getOffhandItem();
        if (!offhand.isEmpty() && offhand.getItem() == IDCard.ID_CARD) {
            offhand.shrink(1);
            player.containerMenu.broadcastChanges();
            return true;
        }

        var inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize(); ++i) {
            ItemStack stack = inv.getItem(i);
            if (!stack.isEmpty() && stack.getItem() == IDCard.ID_CARD) {
                stack.shrink(1);
                player.containerMenu.broadcastChanges();
                return true;
            }
        }

        return false;
    }

    private static void sendResult(ServerPlayer player, boolean success, String message, String sanitized) {
        FriendlyByteBuf out = new FriendlyByteBuf(Unpooled.buffer());
        out.writeBoolean(success);
        out.writeUtf(message, 128);
        out.writeUtf(sanitized, 64);
        ServerPlayNetworking.send(player, CHANGE_DISPLAYNAME_RESULT, out);
    }

    private NetworkHandler() { }
}

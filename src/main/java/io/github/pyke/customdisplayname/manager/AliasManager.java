package io.github.pyke.customdisplayname.manager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.github.pyke.customdisplayname.CustomDisplayName;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AliasManager {
    private static final Map<UUID, String> uuidToDisplayName = new HashMap<>();
    private static final Map<String, UUID> displayNameToUuid = new HashMap<>();
    private static final Gson GSON = new Gson();
    private static File saveFile;

    public static void initSaveFile(File worldDir) {
        saveFile = new File(worldDir, "DisplayNames.json");
        load();
    }

    public static void save() {
        if (null == saveFile) { return; }
        try (Writer writer = new FileWriter(saveFile)) {
            Map<String, String> data = new HashMap<>();
            for (Map.Entry<UUID, String> entry : uuidToDisplayName.entrySet()) {
                data.put(entry.getKey().toString(), entry.getValue());
            }
            GSON.toJson(data, writer);
        } catch (IOException error) {
            CustomDisplayName.LOGGER.error("닉네임 데이터 저장 중 오류 발생", error);
        }
    }

    public static void load() {
        if (null == saveFile || !saveFile.exists()) { return; }
        try (Reader reader = Files.newBufferedReader(saveFile.toPath())) {
            Type type = new TypeToken<Map<String, String>>(){}.getType();
            Map<String, String> data = GSON.fromJson(reader, type);

            uuidToDisplayName.clear();
            displayNameToUuid.clear();
            if (null != data) {
                for (Map.Entry<String, String> entry : data.entrySet()) {
                    UUID uuid = UUID.fromString(entry.getKey());
                    String nickname = entry.getValue();
                    uuidToDisplayName.put(uuid, nickname);
                    displayNameToUuid.put(stripColor(nickname), uuid);
                }
            }
        } catch (IOException error) {
            CustomDisplayName.LOGGER.error("닉네임 데이터 불러오기 실패", error);
        }
    }

    public static boolean setDisplayName(ServerPlayer player, String displayName) {
        String plain = stripColor(displayName);
        if (displayNameToUuid.containsKey(plain)) { return false; }

        UUID uuid = player.getUUID();
        String oldDisplayName = uuidToDisplayName.remove(uuid);
        if (null != oldDisplayName) { displayNameToUuid.remove(stripColor(oldDisplayName)); }

        uuidToDisplayName.put(uuid, plain);
        displayNameToUuid.put(plain, uuid);
        save();
        return true;
    }

    public static boolean resetDisplayName(ServerPlayer player) {
        UUID uuid = player.getUUID();

        if (uuidToDisplayName.containsKey(uuid)) {
            String oldDisplayName = uuidToDisplayName.get(uuid);

            uuidToDisplayName.remove(uuid);
            displayNameToUuid.remove(stripColor(oldDisplayName));

            save();
            return true;
        }

        return false;
    }

    public static String getUuidToDisplayName(UUID uuid) {
        return uuidToDisplayName.get(uuid);
    }

    public static String getRealDisplayName(String displayName) {
        UUID uuid = displayNameToUuid.get(stripColor(displayName));
        if (null == uuid) { return null; }

        MinecraftServer server = CustomDisplayName.getServer();
        if (null == server) { return null; }

        ServerPlayer player = server.getPlayerList().getPlayer(uuid);
        return (null != player) ? player.getGameProfile().getName() : null;
    }

    public static String stripColor(String displayName) {
        return displayName.replaceAll("(?i)&[0-9A-FK-OR]", "");
    }

    public static net.minecraft.network.chat.Component getDisplayName(UUID uuid) {
        String displayName = uuidToDisplayName.get(uuid);
        if (null == displayName) { return null; }
        return net.minecraft.network.chat.Component.literal(stripColor(displayName));
    }
}

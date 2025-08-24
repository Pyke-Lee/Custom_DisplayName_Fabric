package io.github.pyke.customdisplayname;

import com.google.common.graph.Network;
import io.github.pyke.customdisplayname.command.DisplayNameCommand;
import io.github.pyke.customdisplayname.handler.ChatEventHandler;
import io.github.pyke.customdisplayname.handler.NetworkHandler;
import io.github.pyke.customdisplayname.handler.PlayerEventHandler;
import io.github.pyke.customdisplayname.manager.AliasManager;
import io.github.pyke.customdisplayname.item.IDCard;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class CustomDisplayName implements ModInitializer {
	public static final String MOD_ID = "custom_displayname";
    public static final String SYSTEM_PREFIX = "§6[SYSTEM] §r";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static MinecraftServer server;

	@Override
	public void onInitialize() {
        IDCard.register();

        NetworkHandler.registerServer();

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, dedicated) -> {
            DisplayNameCommand.register(dispatcher);
        });

        ServerLifecycleEvents.SERVER_STARTED.register(s -> {
            server = s;
            File saveDir = s.getWorldPath(LevelResource.ROOT).toFile();
            AliasManager.initSaveFile(saveDir);
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(s -> {
            AliasManager.save();
        });

        PlayerEventHandler.register();
        ChatEventHandler.register();
	}

    public static MinecraftServer getServer() { return server; }
}
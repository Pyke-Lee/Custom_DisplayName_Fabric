package io.github.pyke.customdisplayname.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.client.multiplayer.PlayerInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Environment(EnvType.CLIENT)
@Mixin(PlayerRenderer.class)
public abstract class PlayerRendererMixin {
    @ModifyVariable(method = "renderNameTag(Lnet/minecraft/client/player/AbstractClientPlayer;Lnet/minecraft/network/chat/Component;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At("HEAD"), argsOnly = true)
    private Component cdn$useTabListName(Component name, AbstractClientPlayer player, Component originalName, PoseStack poseStack, MultiBufferSource buffers, int light) {
        var mc = Minecraft.getInstance();
        var conn = mc.getConnection();
        if (conn == null) return name;

        PlayerInfo info = conn.getPlayerInfo(player.getUUID());
        if (info == null) return name;

        Component tab = info.getTabListDisplayName();
        return (tab == null) ? name : name.copy().append(Component.literal(" (")).append(tab).append(Component.literal(")"));
    }
}
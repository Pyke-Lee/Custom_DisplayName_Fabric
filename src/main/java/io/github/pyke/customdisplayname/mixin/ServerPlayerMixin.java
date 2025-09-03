package io.github.pyke.customdisplayname.mixin;

import io.github.pyke.customdisplayname.manager.AliasManager;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayer.class)
public class ServerPlayerMixin {
	@Inject(method = "getTabListDisplayName", at = @At("HEAD"), cancellable = true)
    private void cdn$overrideTabListName(CallbackInfoReturnable<Component> cir) {
        ServerPlayer self = (ServerPlayer)(Object)this;
        Component displayName = AliasManager.getDisplayName(self.getUUID());
        if (null != displayName) {
            cir.setReturnValue(displayName);
        }
    }
}
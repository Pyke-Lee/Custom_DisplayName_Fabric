package io.github.pyke.customdisplayname.mixin;

import io.github.pyke.customdisplayname.manager.AliasManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.damagesource.CombatTracker;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CombatTracker.class)
public class DeathMsgMixin {
    @Unique private LivingEntity cdn$owner;

    @Inject(method= "<init>", at = @At("TAIL"))
    private void cdn$storeOwner(LivingEntity owner, CallbackInfo ci) {
        if (null == owner) { return; }
        this.cdn$owner = owner;
    }

    @Inject(method = "getDeathMessage", at = @At("RETURN"), cancellable = true)
    private void cdn$replaceNames(CallbackInfoReturnable<Component> cir) {
        Component origin = cir.getReturnValue();
        if (null == origin) { return; }

        ComponentContents contents = origin.getContents();
        if (!(contents instanceof TranslatableContents ttc)) { return; }

        String key = ttc.getKey();
        Object[] args = ttc.getArgs().clone();

        if (this.cdn$owner instanceof Player victim) {
            Component victimName = AliasManager.getDisplayName(victim.getUUID());
            if (null != victimName && 0 < args.length) { args[0] = victimName; }
        }

        LivingEntity attacker = null;
        try { attacker = this.cdn$owner.getKillCredit(); } catch (Throwable ignored) { }
        if (null != attacker) { try { attacker = this.cdn$owner.getLastAttacker(); } catch (Throwable ignored) { } }
        if (attacker instanceof Player killer) {
            Component killerName = AliasManager.getDisplayName(killer.getUUID());
            if (null != killerName && 2 <= args.length) { args[1] = killerName; }
        }

        MutableComponent rebuilt = Component.translatable(key, args).withStyle(origin.getStyle());
        for (Component sib : origin.getSiblings()) { if (null != sib) { rebuilt.append(sib.copy()); } }
        cir.setReturnValue(rebuilt);
    }
}

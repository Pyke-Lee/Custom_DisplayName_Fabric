package io.github.pyke.customdisplayname.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import io.github.pyke.customdisplayname.CustomDisplayName;
import io.github.pyke.customdisplayname.manager.AliasManager;
import io.github.pyke.customdisplayname.util.TabUtil;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class DisplayNameCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("이름변경")
            .requires(cs -> cs.hasPermission(2))
            .then(Commands.argument("target", EntityArgument.player())
                .executes(ctx -> {
                    ServerPlayer target = EntityArgument.getPlayer(ctx, "target");

                    boolean success = AliasManager.resetDisplayName(target);
                    if (success) {
                        ctx.getSource().sendSuccess(() -> Component.literal(CustomDisplayName.SYSTEM_PREFIX + target.getGameProfile().getName() + "§f의 닉네임을 초기화했습니다."), false);
                        target.sendSystemMessage(Component.literal(CustomDisplayName.SYSTEM_PREFIX + "§f당신의 닉네임이 초기화되었습니다."));
                        TabUtil.refreshTabListName(target);
                        return 1;
                    }
                    else {
                        ctx.getSource().sendFailure(Component.literal(CustomDisplayName.SYSTEM_PREFIX + "§f초기화할 닉네임이 없습니다."));
                        return 0;
                    }
                })
                .then(Commands.argument("displayname", StringArgumentType.greedyString())
                    .executes(ctx -> {
                        ServerPlayer target = EntityArgument.getPlayer(ctx, "target");
                        String displayName = StringArgumentType.getString(ctx, "displayname");

                        boolean success = AliasManager.setDisplayName(target, displayName);
                        if (success) {
                            ctx.getSource().sendSuccess(() -> Component.literal(CustomDisplayName.SYSTEM_PREFIX + target.getGameProfile().getName() + "§f의 닉네임을 '" + AliasManager.stripColor(displayName) + "§f' (으)로 변경했습니다."), false);
                            target.sendSystemMessage(Component.literal(CustomDisplayName.SYSTEM_PREFIX + "§f당신의 닉네임이 '" + AliasManager.stripColor(displayName) + "§f' (으)로 변경되었습니다."));
                            TabUtil.refreshTabListName(target);
                            return 1;
                        } else {
                            ctx.getSource().sendFailure(Component.literal(CustomDisplayName.SYSTEM_PREFIX + "§f이미 사용 중인 닉네임입니다."));
                            return 0;
                        }
                    })
                )
            )
        );
    }
}

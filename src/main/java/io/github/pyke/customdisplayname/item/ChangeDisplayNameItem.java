package io.github.pyke.customdisplayname.item;

import io.github.pyke.customdisplayname.CustomDisplayName;
import io.github.pyke.customdisplayname.client.ClientHooks;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class ChangeDisplayNameItem extends Item {
    public ChangeDisplayNameItem(Properties props) { super(props); }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack item = player.getItemInHand(hand);
        if (level.isClientSide) { openUIOrWarn(player); }
        return InteractionResultHolder.sidedSuccess(item, level.isClientSide);
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        if (context.getLevel().isClientSide) { openUIOrWarn(context.getPlayer()); }
        return InteractionResult.sidedSuccess(context.getLevel().isClientSide);
    }

    private void openUIOrWarn(Player player) {
        if (ClientHooks.READY) {
            ClientHooks.OPEN_CHANGE_NAME_SCREEN.run();
        }
        else if (null != player) {
            player.sendSystemMessage(Component.literal(CustomDisplayName.SYSTEM_PREFIX + "§f클라이언트 초기화가 되지 않아 UI를 열 수 없습니다. fabric.mod.json의 client entrypoint를 확인하세요."));
        }
    }
}

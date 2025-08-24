package io.github.pyke.customdisplayname.item;

import io.github.pyke.customdisplayname.client.ClientNetworking;
import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.component.TextBoxComponent;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

@Environment(EnvType.CLIENT)
public class ChangeDisplayNameUI extends BaseOwoScreen<FlowLayout> {
    private TextBoxComponent nameField;
    private LabelComponent errorLabel;
    private static ChangeDisplayNameUI CURRENT;

    public static void onNetworkResult(boolean success, String message, String sanitized) {
        if (!(Minecraft.getInstance().screen instanceof ChangeDisplayNameUI ui)) {
            if (!success && Minecraft.getInstance().player != null) { Minecraft.getInstance().player.sendSystemMessage(Component.literal("§f" + message)); }
            return;
        }

        if (success) { ui.onClose(); }
        else {
            ui.errorLabel.text(Component.literal("§f" + message));
            if (!sanitized.isEmpty()) { ui.nameField.setValue(sanitized); }
        }
    }

    @Override
    protected @NotNull OwoUIAdapter<FlowLayout> createAdapter() {
        return OwoUIAdapter.create(this, Containers::verticalFlow);
    }

    @Override
    protected void build(FlowLayout flowLayout) {
        flowLayout.sizing(Sizing.fill(100), Sizing.fill(100));
        flowLayout.horizontalAlignment(HorizontalAlignment.CENTER);
        flowLayout.verticalAlignment(VerticalAlignment.CENTER);

        var panel = Containers.verticalFlow(Sizing.fixed(240), Sizing.content());
        panel.surface(Surface.PANEL).padding(Insets.of(12));

        var titleRow = Containers.horizontalFlow(Sizing.fill(100), Sizing.content());
        titleRow.horizontalAlignment(HorizontalAlignment.CENTER);
        titleRow.child(Components.label(Component.literal("닉네임 변경"))
            .shadow(true).margins(Insets.bottom(8)));
        panel.child(titleRow);

        var inputRow = Containers.horizontalFlow(Sizing.fill(100), Sizing.content());
        inputRow.horizontalAlignment(HorizontalAlignment.CENTER);
        var nameField = Components.textBox(Sizing.fixed(180), "");
        nameField.setMaxLength(8);

        final boolean[] reentry = { false };
        nameField.onChanged().subscribe(text -> {
            if (reentry[0]) { return; }

            String sanitized = sanitizeDisplayName(text);
            if (sanitized.length() > 8) { sanitized = sanitized.substring(0, 8); }

            if (!text.equals(sanitized)) {
                reentry[0] = true;
                nameField.setValue(sanitized);
                reentry[0] = false;
            }

            if (null != errorLabel) { errorLabel.text(Component.empty()); }
        });

        inputRow.child(nameField);
        panel.child(inputRow);

        int line = Minecraft.getInstance().font.lineHeight;
        var msgRow = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(line + 6));
        msgRow.horizontalAlignment(HorizontalAlignment.CENTER);

        errorLabel = (LabelComponent) Components.label(Component.empty()).margins(Insets.top(6));
        msgRow.child(errorLabel);
        panel.child(msgRow);

        var footerRow = Containers.horizontalFlow(Sizing.fill(100), Sizing.content());
        footerRow.horizontalAlignment(HorizontalAlignment.CENTER);
        footerRow.child(Components.button(Component.literal("변경하기"), b -> {
            String value = nameField.getValue().trim();
            if (value.isEmpty()) { return; }

            ClientNetworking.sendChangeDisplayName(value);
        }).margins(Insets.top(10)).sizing(Sizing.fill(35), Sizing.content()));
        panel.child(footerRow);

        flowLayout.child(panel);
    }

    private static String sanitizeDisplayName(String text) {
        StringBuilder out = new StringBuilder();
        text.codePoints().forEach(c -> { if (isAllowed(c)) { out.appendCodePoint(c); } });
        return out.toString();
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

    @Override
    public void removed() {
        super.removed();
        if (this == CURRENT) { CURRENT = null; }
    }
}

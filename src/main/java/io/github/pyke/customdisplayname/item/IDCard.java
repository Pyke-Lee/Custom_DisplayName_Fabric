package io.github.pyke.customdisplayname.item;

import io.github.pyke.customdisplayname.CustomDisplayName;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;

public final class IDCard {
    private static boolean REGISTERED = false;

    public static final Item ID_CARD = new ChangeDisplayNameItem(new FabricItemSettings().maxCount(1));

    public static void register() {
        if (REGISTERED) { return; }
        REGISTERED = true;

        Registry.register(BuiltInRegistries.ITEM, new ResourceLocation(CustomDisplayName.MOD_ID, "id_card"), ID_CARD);

        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.TOOLS_AND_UTILITIES)
            .register(entries -> entries.accept(ID_CARD));
    }

    private IDCard() { }
}

package com.mchorror.watcherprotocol.registry;

import com.mchorror.watcherprotocol.Watcher_protocol;
import com.mchorror.watcherprotocol.item.AnomalyJournalItem;
import com.mchorror.watcherprotocol.item.WatcherGuideBookItem;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public final class WatcherItems {
    public static final Item WATCHER_GUIDE = register("watcher_guide", new WatcherGuideBookItem(new Item.Settings().maxCount(1)));
    public static final Item ANOMALY_JOURNAL = register("anomaly_journal", new AnomalyJournalItem(new Item.Settings().maxCount(1)));

    private WatcherItems() {
    }

    public static void register() {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(entries -> {
            entries.add(WATCHER_GUIDE);
            entries.add(ANOMALY_JOURNAL);
        });
    }

    private static Item register(String id, Item item) {
        return Registry.register(Registries.ITEM, new Identifier(Watcher_protocol.MOD_ID, id), item);
    }
}
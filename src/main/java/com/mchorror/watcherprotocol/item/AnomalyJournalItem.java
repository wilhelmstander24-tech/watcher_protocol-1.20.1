package com.mchorror.watcherprotocol.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class AnomalyJournalItem extends Item {
    public AnomalyJournalItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (!world.isClient && user instanceof ServerPlayerEntity serverPlayer) {
            NbtCompound nbt = stack.getOrCreateNbt();
            int anomalies = nbt.getInt("anomalies_logged") + 1;
            nbt.putInt("anomalies_logged", anomalies);
            nbt.putLong("last_log_time", world.getTime());

            NbtList entries = nbt.getList("anomaly_entries", 8);
            entries.add(NbtString.of("t=" + world.getTime() + " dim=" + world.getRegistryKey().getValue() + " pos="
                    + serverPlayer.getBlockX() + "," + serverPlayer.getBlockY() + "," + serverPlayer.getBlockZ()));
            nbt.put("anomaly_entries", entries);

            serverPlayer.sendMessage(Text.translatable("watcher_protocol.journal.logged", anomalies), true);
            serverPlayer.sendMessage(Text.translatable("watcher_protocol.journal.storage_hint"), false);
        }
        return TypedActionResult.success(stack, world.isClient());
    }
}
package com.mchorror.watcherprotocol.item;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import vazkii.patchouli.api.PatchouliAPI;

public class WatcherGuideBookItem extends Item {
    private static final Identifier BOOK_ID = new Identifier("watcher_protocol", "watcher_survival_guide");

    public WatcherGuideBookItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (!world.isClient && user instanceof ServerPlayerEntity serverPlayer) {
            if (!FabricLoader.getInstance().isModLoaded("patchouli")) {
                serverPlayer.sendMessage(Text.translatable("watcher_protocol.guide.patchouli_missing"), true);
                return TypedActionResult.success(stack, world.isClient());
            }

            PatchouliAPI.get().openBookGUI(serverPlayer, BOOK_ID);
            if (stack.hasNbt() && stack.getNbt().contains("phase6_script", 8)) {
                serverPlayer.sendMessage(
                        Text.translatable("watcher_protocol.guide.script_shift", stack.getNbt().getString("phase6_script")),
                        false);
            }
        }
        return TypedActionResult.success(stack, world.isClient());
    }
}
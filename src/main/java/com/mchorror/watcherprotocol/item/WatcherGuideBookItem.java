package com.mchorror.watcherprotocol.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class WatcherGuideBookItem extends Item {
    public WatcherGuideBookItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (!world.isClient) {
            user.sendMessage(Text.literal("Guide uses bundled KubeJS-style JSON pages and is auto-delivered on join."), true);}
        return TypedActionResult.success(stack, world.isClient());
    }
}
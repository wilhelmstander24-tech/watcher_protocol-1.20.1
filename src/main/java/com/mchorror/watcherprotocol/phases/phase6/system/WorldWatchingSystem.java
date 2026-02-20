package com.mchorror.watcherprotocol.phases.phase6.system;

import java.util.List;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Box;

public class WorldWatchingSystem implements PhaseSixSystem {
    private int cooldown = 80;

    @Override
    public void tick(ServerWorld world, double corruptionLevel) {
        List<ServerPlayerEntity> players = world.getPlayers();
        if (players.isEmpty() || --cooldown > 0) {
            return;
        }
        cooldown = Math.max(20, (int) Math.round((120 + world.getRandom().nextBetween(0, 120)) / corruptionLevel));

        for (ServerPlayerEntity player : players) {
            Box box = player.getBoundingBox().expand(20.0);
            for (ArmorStandEntity stand : world.getEntitiesByClass(ArmorStandEntity.class, box, e -> true)) {
                if (!player.canSee(stand)) {
                    stand.setBodyYaw(stand.getBodyYaw() + world.getRandom().nextBetween(-35, 35));
                    stand.setHeadYaw(stand.getHeadYaw() + world.getRandom().nextBetween(-45, 45));
                }
            }
        }
    }
}

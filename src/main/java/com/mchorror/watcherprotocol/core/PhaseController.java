package com.mchorror.watcherprotocol.core;

import com.mchorror.watcherprotocol.config.WatcherConfigManager;
import com.mchorror.watcherprotocol.phases.Phase;
import com.mchorror.watcherprotocol.phases.PhaseType;
import com.mchorror.watcherprotocol.phases.phase1.DesynchronizationPhase;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;

public class PhaseController {
    private final Map<PhaseType, Phase> phases = new EnumMap<>(PhaseType.class);
    private Phase activePhase;
    private final Set<net.minecraft.registry.RegistryKey<World>> startedWorlds = new HashSet<>();

    public PhaseController() {
        registerPhase(new DesynchronizationPhase());
        setActivePhase(PhaseType.PHASE_1, null);
    }

    public void registerPhase(Phase phase) {
        phases.put(phase.getType(), phase);
    }

    public void setActivePhase(PhaseType type, ServerWorld world) {
        if (activePhase != null && activePhase.getType() == type) {
            return;
        }

        if (activePhase != null && world != null) {
            activePhase.onStop(world);
        }

        activePhase = phases.get(type);
        startedWorlds.clear();

        if (activePhase != null && world != null) {
            activePhase.onStart(world);
            startedWorlds.add(world.getRegistryKey());
        }
    }

    public PhaseType getActivePhaseType() {
        return activePhase == null ? null : activePhase.getType();
    }

    public void tick(ServerWorld world) {
        if (activePhase == null) {
            return;
        }

        if (!WatcherConfigManager.getConfig().isModEnabled()) {
            return;
        }

        if (activePhase.getType() == PhaseType.PHASE_1
                && !WatcherConfigManager.getConfig().isPhaseOneEnabled()) {
            return;
        }

        if (startedWorlds.add(world.getRegistryKey())) {
            activePhase.onStart(world);
        }

        activePhase.tick(world);
    }
}
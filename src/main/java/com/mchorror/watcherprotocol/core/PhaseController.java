package com.mchorror.watcherprotocol.core;

import com.mchorror.watcherprotocol.config.WatcherConfigManager;
import com.mchorror.watcherprotocol.phases.Phase;
import com.mchorror.watcherprotocol.phases.PhaseType;
import com.mchorror.watcherprotocol.phases.phase1.DesynchronizationPhase;
import com.mchorror.watcherprotocol.phases.phase2.PerceptualCorruptionPhase;
import com.mchorror.watcherprotocol.phases.phase3.ObserverPhase;
import com.mchorror.watcherprotocol.phases.phase4.InterferencePhase;
import com.mchorror.watcherprotocol.phases.phase5.MockeryPhase;
import com.mchorror.watcherprotocol.phases.phase6.ConfrontationPhase;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.minecraft.registry.RegistryKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;

public class PhaseController {
    private final Map<PhaseType, Phase> phases = new EnumMap<>(PhaseType.class);
    private Phase activePhase;
    private static final long PHASE_TWO_START_TICKS = 24000L * 10L;
    private static final long PHASE_THREE_START_TICKS = 24000L * 20L;
    private static final long PHASE_FOUR_START_TICKS = 24000L * 30L;
    private static final long PHASE_FIVE_START_TICKS = 24000L * 40L;
    private static final long PHASE_SIX_START_TICKS = 24000L * 50L;

    private final Set<RegistryKey<World>> startedWorlds = new HashSet<>();

    public PhaseController() {
        registerPhase(new DesynchronizationPhase());
        registerPhase(new PerceptualCorruptionPhase());
        registerPhase(new ObserverPhase());
        registerPhase(new InterferencePhase());
        registerPhase(new MockeryPhase());
        registerPhase(new ConfrontationPhase());
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

        if (!WatcherConfigManager.getConfig().isPhaseEnabled(activePhase.getType())) {
            moveToNextEnabledPhase(world);
            if (activePhase == null || !WatcherConfigManager.getConfig().isPhaseEnabled(activePhase.getType())) {
                return;
            }
        }

        if (startedWorlds.add(world.getRegistryKey())) {
            activePhase.onStart(world);
        }

        if (activePhase.getType() == PhaseType.PHASE_1 && world.getTimeOfDay() >= PHASE_TWO_START_TICKS) {
            setActivePhase(PhaseType.PHASE_2, world);
        } else if (activePhase.getType() == PhaseType.PHASE_2 && world.getTimeOfDay() >= PHASE_THREE_START_TICKS) {
            setActivePhase(PhaseType.PHASE_3, world);
        } else if (activePhase.getType() == PhaseType.PHASE_3 && world.getTimeOfDay() >= PHASE_FOUR_START_TICKS) {
            setActivePhase(PhaseType.PHASE_4, world);
        } else if (activePhase.getType() == PhaseType.PHASE_4 && world.getTimeOfDay() >= PHASE_FIVE_START_TICKS) {
            setActivePhase(PhaseType.PHASE_5, world);
        } else if (activePhase.getType() == PhaseType.PHASE_5 && world.getTimeOfDay() >= PHASE_SIX_START_TICKS) {
            setActivePhase(PhaseType.PHASE_6, world);
        }

        activePhase.tick(world);
    }

    private void moveToNextEnabledPhase(ServerWorld world) {
        PhaseType next = switch (activePhase.getType()) {
            case PHASE_1 -> PhaseType.PHASE_2;
            case PHASE_2 -> PhaseType.PHASE_3;
            case PHASE_3 -> PhaseType.PHASE_4;
            case PHASE_4 -> PhaseType.PHASE_5;
            case PHASE_5 -> PhaseType.PHASE_6;
            case PHASE_6 -> null;
            case PHASE_7 -> null;
        };
        while (next != null && !WatcherConfigManager.getConfig().isPhaseEnabled(next)) {
            next = switch (next) {
                case PHASE_1 -> PhaseType.PHASE_2;
                case PHASE_2 -> PhaseType.PHASE_3;
                case PHASE_3 -> PhaseType.PHASE_4;
                case PHASE_4 -> PhaseType.PHASE_5;
                case PHASE_5 -> PhaseType.PHASE_6;
                case PHASE_6 -> null;
                case PHASE_7 -> null;
            };
        }
        if (next != null) {
            setActivePhase(next, world);
        }
    }
}
package com.mchorror.watcherprotocol.core.memory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public final class PlayerPatternMemory {
	private static final int MAX_PATH_POINTS = 80;
	private static final int MAX_MOVEMENT_INTERVALS = 40;
	private static final int MAX_MINED_BLOCK_TYPES = 12;
	private static final int MAX_PLACEMENT_PATTERNS = 20;
	private static final int MAX_SCREEN_EVENTS = 10;

	private final List<Vec3d> recentPath = new ArrayList<>();
	private final List<Integer> movementIntervals = new ArrayList<>();
	private final List<Integer> miningIntervals = new ArrayList<>();
	private final Map<Block, Integer> minedBlocks = new HashMap<>();
	private final List<BlockPos> placedBlockPattern = new ArrayList<>();
	private final Map<String, Integer> interactionTypes = new HashMap<>();
	private int screenOpenEvents;

	private long lastMoveTick = -1L;
	private long lastMineTick = -1L;

	public void recordMovement(Vec3d pos, long worldTick) {
		recentPath.add(pos);
		trimList(recentPath, MAX_PATH_POINTS);

		if (lastMoveTick >= 0L) {
			movementIntervals.add((int) (worldTick - lastMoveTick));
			trimList(movementIntervals, MAX_MOVEMENT_INTERVALS);
		}
		lastMoveTick = worldTick;
	}

	public void recordMining(Block minedBlock, long worldTick) {
		if (lastMineTick >= 0L) {
			miningIntervals.add((int) (worldTick - lastMineTick));
			trimList(miningIntervals, MAX_MOVEMENT_INTERVALS);
		}
		lastMineTick = worldTick;

		if (minedBlocks.containsKey(minedBlock) || minedBlocks.size() < MAX_MINED_BLOCK_TYPES) {
			minedBlocks.merge(minedBlock, 1, Integer::sum);
		}
	}

	public void recordPlacement(BlockPos pos) {
		placedBlockPattern.add(pos.toImmutable());
		trimList(placedBlockPattern, MAX_PLACEMENT_PATTERNS);
	}

	public void recordInteraction(String kind) {
		interactionTypes.merge(kind, 1, Integer::sum);
	}

	public void recordScreenOpen() {
		screenOpenEvents = Math.min(MAX_SCREEN_EVENTS, screenOpenEvents + 1);
	}

	public List<Vec3d> getRecentPath() {
		return recentPath;
	}

	public List<Integer> getMovementIntervals() {
		return movementIntervals;
	}

	public List<Integer> getMiningIntervals() {
		return miningIntervals;
	}

	public Map<Block, Integer> getMinedBlocks() {
		return minedBlocks;
	}

	public List<BlockPos> getPlacedBlockPattern() {
		return placedBlockPattern;
	}

	public Map<String, Integer> getInteractionTypes() {
		return interactionTypes;
	}

	public int getScreenOpenEvents() {
		return screenOpenEvents;
	}

	private static <T> void trimList(List<T> list, int maxSize) {
		while (list.size() > maxSize) {
			list.remove(0);
		}
	}
}

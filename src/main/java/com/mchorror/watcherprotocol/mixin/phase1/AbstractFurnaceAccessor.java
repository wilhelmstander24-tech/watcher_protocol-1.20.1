package com.mchorror.watcherprotocol.mixin.phase1;

import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractFurnaceBlockEntity.class)
public interface AbstractFurnaceAccessor {
	@Accessor("cookTime")
	int watcherProtocol$getCookTime();

	@Accessor("cookTime")
	void watcherProtocol$setCookTime(int cookTime);
}

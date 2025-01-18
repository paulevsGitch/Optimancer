package paulevs.optimancer.helper;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.io.CompoundTag;

public interface OptimancerSection {
	BlockEntity optimancer_getBlockEntity(int x, int y, int z);
	void optimancer_setBlockEntity(int x, int y, int z, BlockEntity entity);
	void optimancer_tickEntities();
	void optimancer_tickBlockEntities();
	void optimancer_readNBT(CompoundTag tag);
	void optimancer_writeNBT(CompoundTag tag);
}

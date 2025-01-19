package paulevs.optimancer.mixin.client;

import net.minecraft.level.Level;
import net.minecraft.level.chunk.Chunk;
import net.minecraft.level.chunk.ChunkCache;
import net.modificationstation.stationapi.impl.world.chunk.FlattenedChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import paulevs.optimancer.world.NullChunk;
import paulevs.optimancer.world.OptimancerLevelSource;

// No instances of parent, most likely redundant
@Mixin(ChunkCache.class)
public abstract class ChunkCacheMixin implements OptimancerLevelSource {
	@Shadow public abstract boolean isInBounds(int x, int z);
	@Shadow private Chunk[] chunkCache;
	@Shadow private Chunk emptyChunk;
	@Shadow private Chunk lastChunk;
	@Shadow private Level level;
	@Shadow int lastX;
	@Shadow int lastZ;
	
	@Override
	public void optimancer_setChunk(FlattenedChunk chunk) {
		if (isInBounds(chunk.x, chunk.z)) {
			int index = (chunk.x & 31) | (chunk.z & 31) << 5;
			chunkCache[index] = chunk;
			int x = chunk.x << 4 | 8;
			int z = chunk.z << 4 | 8;
			level.updateArea(x, level.getBottomY(), z, x, level.getTopY(), z);
		}
	}
	
	@Inject(method = "isChunkLoaded", at = @At("HEAD"), cancellable = true)
	private void optimancer_isChunkLoaded(int x, int z, CallbackInfoReturnable<Boolean> info) {
		if (!this.isInBounds(x, z)) info.setReturnValue(false);
		else if (x == lastX && z == lastZ && lastChunk != null && !(lastChunk instanceof NullChunk)) {
			info.setReturnValue(true);
		}
		else {
			int index = (x & 31) | (z & 31) << 5;
			Chunk chunk = chunkCache[index];
			info.setReturnValue(
				chunk != null && !(chunk instanceof NullChunk) && (chunk == emptyChunk || chunk.equalPosition(x, z))
			);
		}
	}
}

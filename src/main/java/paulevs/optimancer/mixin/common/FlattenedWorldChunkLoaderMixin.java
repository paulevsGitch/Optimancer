package paulevs.optimancer.mixin.common;

import net.minecraft.level.Level;
import net.minecraft.level.chunk.Chunk;
import net.modificationstation.stationapi.impl.world.chunk.FlattenedChunk;
import net.modificationstation.stationapi.impl.world.chunk.FlattenedWorldChunkLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import paulevs.optimancer.thread.ChunkManagerThread;
import paulevs.optimancer.thread.ThreadManager;

@Mixin(value = FlattenedWorldChunkLoader.class, remap = false)
public class FlattenedWorldChunkLoaderMixin {
	@Inject(method = "saveChunk", at = @At(
		value = "INVOKE",
		target = "Lnet/minecraft/level/Level;checkSessionLock()V",
		shift = Shift.AFTER
	), cancellable = true)
	private void optimancer_saveChunk(Level level, Chunk chunk, CallbackInfo info) {
		ChunkManagerThread loader = ThreadManager.getChunkLoader(level);
		if (loader != null) {
			loader.saveChunk((FlattenedChunk) chunk);
			info.cancel();
		}
	}
	
	/*@Inject(method = "loadChunk", at = @At("HEAD"), cancellable = true)
	private void optimancer_loadChunk(Level level, int x, int z, CallbackInfoReturnable<Chunk> info) {
		ChunkManagerThread loader = ThreadManager.getChunkLoader(level);
		if (loader != null) {
			info.setReturnValue(ThreadManager.getChunkLoader(level).loadChunk(x, z));
		}
	}*/
}

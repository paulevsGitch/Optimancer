package paulevs.optimancer.mixin.common;

import net.minecraft.level.Level;
import net.minecraft.level.chunk.Chunk;
import net.minecraft.level.chunk.ChunkIO;
import net.modificationstation.stationapi.impl.world.chunk.FlattenedChunk;
import net.modificationstation.stationapi.impl.world.chunk.FlattenedWorldChunkLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import paulevs.optimancer.thread.ChunkManagerThread;
import paulevs.optimancer.thread.ThreadManager;
import paulevs.optimancer.world.NullChunk;
import paulevs.optimancer.world.PromiseChunk;

@Mixin(value = FlattenedWorldChunkLoader.class, remap = false)
public abstract class FlattenedWorldChunkLoaderMixin implements ChunkIO {
	@Inject(method = "saveChunk", at = @At(
		value = "INVOKE",
		target = "Lnet/minecraft/level/Level;checkSessionLock()V",
		shift = Shift.AFTER
	), cancellable = true, remap = true)
	private void optimancer_saveChunk(Level level, Chunk chunk, CallbackInfo info) {
		if (chunk instanceof NullChunk || chunk instanceof PromiseChunk) {
			info.cancel();
			return;
		}
		ChunkManagerThread loader = ThreadManager.getChunkLoader(level);
		if (loader != null) {
			loader.saveChunk((FlattenedChunk) chunk);
			info.cancel();
		}
	}
	
	@Inject(method = "loadChunk", at = @At("HEAD"), cancellable = true, remap = true)
	private void optimancer_loadChunk(Level level, int x, int z, CallbackInfoReturnable<Chunk> info) {
		ChunkManagerThread loader = ThreadManager.getChunkLoader(level);
		if (loader != null) {
			info.setReturnValue(ThreadManager.getChunkLoader(level).loadChunk(x, z));
		}
	}
}

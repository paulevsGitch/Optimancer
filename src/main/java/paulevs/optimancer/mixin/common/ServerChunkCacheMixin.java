package paulevs.optimancer.mixin.common;

import net.minecraft.level.Level;
import net.minecraft.level.chunk.Chunk;
import net.minecraft.level.chunk.ChunkIO;
import net.minecraft.level.chunk.ServerChunkCache;
import net.minecraft.level.source.LevelSource;
import net.minecraft.util.maths.Vec2I;
import net.modificationstation.stationapi.impl.world.chunk.FlattenedChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import paulevs.optimancer.collection.ConcurrentInt2ReferenceMap;
import paulevs.optimancer.world.NullChunk;
import paulevs.optimancer.world.OptimancerLevelSource;
import paulevs.optimancer.world.PromiseChunk;

import java.util.List;
import java.util.Map;

@Mixin(ServerChunkCache.class)
public abstract class ServerChunkCacheMixin implements OptimancerLevelSource {
	@SuppressWarnings("rawtypes")
	@Shadow private Map serverChunkCache;
	@Shadow private Level level;
	@Shadow private Chunk dummyChunk;
	@Shadow private LevelSource levelSource;
	@SuppressWarnings("rawtypes")
	@Shadow private List loadedChunks;
	
	@Shadow public abstract void decorate(LevelSource levelSource, int chunkX, int chunkZ);
	@Shadow public abstract Chunk getChunk(int chunkX, int chunkZ);
	
	@Inject(method = "<init>", at = @At("TAIL"))
	private void optimancer_isChunkLoaded(Level level, ChunkIO io, LevelSource chunkGenerator, CallbackInfo info) {
		serverChunkCache = new ConcurrentInt2ReferenceMap<Chunk>();
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public void optimancer_setChunk(FlattenedChunk chunk) {
		int index = Vec2I.hash(chunk.x, chunk.z);
		if (chunk instanceof NullChunk) {
			Chunk newChunk = dummyChunk;
			if (levelSource != null) newChunk = levelSource.getChunk(chunk.x, chunk.z);
			
			optimancer_getStorage().put(index, newChunk);
			loadedChunks.add(newChunk);
			
			newChunk.onChunkLoaded();
			newChunk.addEntitiesToLevel();
			
			ServerChunkCache cache = ServerChunkCache.class.cast(this);
			
			if (!newChunk.decorated &&
				optimancer_isLoadedAndNotPromise(chunk.x + 1, chunk.z + 1) &&
				optimancer_isLoadedAndNotPromise(chunk.x, chunk.z + 1) &&
				optimancer_isLoadedAndNotPromise(chunk.x + 1, chunk.z)
			) {
				decorate(cache, chunk.x, chunk.z);
			}
			
			if (
				optimancer_isLoadedAndNotPromise(chunk.x - 1, chunk.z) &&
				optimancer_isLoadedAndNotPromise(chunk.x - 1, chunk.z + 1) &&
				optimancer_isLoadedAndNotPromise(chunk.x, chunk.z + 1) &&
				optimancer_isLoadedAndNotPromise(chunk.x - 1, chunk.z)
			) {
				decorate(cache, chunk.x - 1, chunk.z);
			}
			
			if (
				optimancer_isLoadedAndNotPromise(chunk.x, chunk.z - 1) &&
				!getChunk(chunk.x, chunk.z - 1).decorated &&
				optimancer_isLoadedAndNotPromise(chunk.x + 1, chunk.z - 1) &&
				optimancer_isLoadedAndNotPromise(chunk.x, chunk.z - 1) &&
				optimancer_isLoadedAndNotPromise(chunk.x + 1, chunk.z)
			) {
				decorate(cache, chunk.x, chunk.z - 1);
			}
			
			if (
				optimancer_isLoadedAndNotPromise(chunk.x - 1, chunk.z - 1) &&
				!getChunk(chunk.x - 1, chunk.z - 1).decorated &&
				optimancer_isLoadedAndNotPromise(chunk.x - 1, chunk.z - 1) &&
				optimancer_isLoadedAndNotPromise(chunk.x, chunk.z - 1) &&
				optimancer_isLoadedAndNotPromise(chunk.x - 1, chunk.z)
			) {
				decorate(cache, chunk.x - 1, chunk.z - 1);
			}
		}
		else {
			optimancer_getStorage().put(index, chunk);
			int x = chunk.x << 4 | 8;
			int z = chunk.z << 4 | 8;
			level.updateArea(x, level.getBottomY(), z, x, level.getTopY(), z);
		}
	}
	
	@Inject(method = "isChunkLoaded", at = @At("HEAD"), cancellable = true)
	private void optimancer_isChunkLoaded(int x, int z, CallbackInfoReturnable<Boolean> info) {
		int index = Vec2I.hash(x, z);
		Chunk chunk = optimancer_getStorage().get(index);
		info.setReturnValue(chunk != null && !(chunk instanceof NullChunk));
	}
	
	@Unique
	@SuppressWarnings("unchecked")
	private ConcurrentInt2ReferenceMap<Chunk> optimancer_getStorage() {
		return (ConcurrentInt2ReferenceMap<Chunk>) serverChunkCache;
	}
	
	@Unique
	private boolean optimancer_isLoadedAndNotPromise(int x, int z) {
		int index = Vec2I.hash(x, z);
		Chunk chunk = optimancer_getStorage().get(index);
		return chunk != null && !(chunk instanceof PromiseChunk);
	}
}

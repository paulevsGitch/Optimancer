package paulevs.optimancer.mixin.server;

import it.unimi.dsi.fastutil.ints.Int2ReferenceOpenHashMap;
import net.minecraft.level.chunk.Chunk;
import net.minecraft.level.chunk.ChunkIO;
import net.minecraft.level.source.LevelSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerLevelSource;
import net.minecraft.util.maths.Vec2I;
import net.modificationstation.stationapi.impl.world.chunk.FlattenedChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import paulevs.optimancer.world.NullChunk;
import paulevs.optimancer.world.OptimancerLevelSource;
import paulevs.optimancer.world.PromiseChunk;

import java.util.List;
import java.util.Map;

@Mixin(ServerLevelSource.class)
public abstract class ServerLevelSourceMixin implements OptimancerLevelSource {
	@Shadow private LevelSource parentLevelSource;
	@Shadow private Chunk chunk;
	@SuppressWarnings("rawtypes")
	@Shadow private List field_939;
	@SuppressWarnings("rawtypes")
	@Shadow private Map chunks;
	
	@Shadow public abstract Chunk getChunk(int chunkX, int chunkZ);
	@Shadow public abstract void decorate(LevelSource levelSource, int chunkX, int chunkZ);
	
	@Inject(method = "<init>", at = @At("TAIL"))
	private void optimancer_isChunkLoaded(ServerLevel level, ChunkIO chunkIO, LevelSource levelSource, CallbackInfo info) {
		chunks = new Int2ReferenceOpenHashMap<FlattenedChunk>();
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public void optimancer_setChunk(FlattenedChunk chunk) {
		int index = Vec2I.hash(chunk.x, chunk.z);
		if (chunk instanceof NullChunk) {
			Chunk newChunk = this.chunk;
			if (parentLevelSource != null) newChunk = parentLevelSource.getChunk(chunk.x, chunk.z);
			
			optimancer_getStorage().put(index, newChunk);
			field_939.add(newChunk);
			
			newChunk.onChunkLoaded();
			newChunk.addEntitiesToLevel();
			
			ServerLevelSource source = ServerLevelSource.class.cast(this);
			
			if (
				!newChunk.decorated &&
				optimancer_isLoadedAndNotPromise(chunk.x + 1, chunk.z + 1) &&
				optimancer_isLoadedAndNotPromise(chunk.x, chunk.z + 1) &&
				optimancer_isLoadedAndNotPromise(chunk.x + 1, chunk.z)
			) {
				decorate(source, chunk.x, chunk.z);
			}
			
			if (
				optimancer_isLoadedAndNotPromise(chunk.x - 1, chunk.z) &&
				!getChunk(chunk.x - 1, chunk.z).decorated &&
				optimancer_isLoadedAndNotPromise(chunk.x - 1, chunk.z + 1) &&
				optimancer_isLoadedAndNotPromise(chunk.x, chunk.z + 1) &&
				optimancer_isLoadedAndNotPromise(chunk.x - 1, chunk.z)
			) {
				decorate(source, chunk.x - 1, chunk.z);
			}
			
			if (
				optimancer_isLoadedAndNotPromise(chunk.x, chunk.z - 1) &&
				!getChunk(chunk.x, chunk.z - 1).decorated &&
				optimancer_isLoadedAndNotPromise(chunk.x + 1, chunk.z - 1) &&
				optimancer_isLoadedAndNotPromise(chunk.x, chunk.z - 1) &&
				optimancer_isLoadedAndNotPromise(chunk.x + 1, chunk.z)
			) {
				decorate(source, chunk.x, chunk.z - 1);
			}
			
			if (
				optimancer_isLoadedAndNotPromise(chunk.x - 1, chunk.z - 1) &&
				!getChunk(chunk.x - 1, chunk.z - 1).decorated &&
				optimancer_isLoadedAndNotPromise(chunk.x - 1, chunk.z - 1) &&
				optimancer_isLoadedAndNotPromise(chunk.x, chunk.z - 1) &&
				optimancer_isLoadedAndNotPromise(chunk.x - 1, chunk.z)
			) {
				decorate(source, chunk.x - 1, chunk.z - 1);
			}
		}
		else {
			optimancer_getStorage().put(index, chunk);
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
	private Int2ReferenceOpenHashMap<Chunk> optimancer_getStorage() {
		return (Int2ReferenceOpenHashMap<Chunk>) chunks;
	}
	
	@Unique
	private boolean optimancer_isLoadedAndNotPromise(int x, int z) {
		int index = Vec2I.hash(x, z);
		Chunk chunk = optimancer_getStorage().get(index);
		return chunk != null && !(chunk instanceof PromiseChunk);
	}
}

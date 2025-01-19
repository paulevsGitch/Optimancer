package paulevs.optimancer.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.level.Level;
import net.minecraft.level.chunk.Chunk;
import net.minecraft.level.chunk.MultiplayerChunkCache;
import net.minecraft.util.maths.Vec2I;
import net.modificationstation.stationapi.impl.world.chunk.FlattenedChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import paulevs.optimancer.helper.OptimancerMathHelper;
import paulevs.optimancer.util.ConcurrentLong2ReferenceMap;
import paulevs.optimancer.world.NullChunk;
import paulevs.optimancer.world.OptimancerLevelSource;

import java.util.List;
import java.util.Map;

@Mixin(MultiplayerChunkCache.class)
public abstract class MultiplayerChunkCacheMixin implements OptimancerLevelSource {
	@SuppressWarnings("rawtypes")
	@Shadow private Map multiplayerChunkCache;
	@SuppressWarnings("rawtypes")
	@Shadow private List chunkList;
	@Shadow private Level level;
	
	@Inject(method = "<init>", at = @At("TAIL"))
	private void optimancer_isChunkLoaded(Level level, CallbackInfo info) {
		multiplayerChunkCache = new ConcurrentLong2ReferenceMap<Chunk>();
		chunkList = null;
	}
	
	@Override
	public void optimancer_setChunk(FlattenedChunk chunk) {
		long index = OptimancerMathHelper.pack(chunk.x, chunk.z);
		optimancer_getStorage().put(index, chunk);
		int x = chunk.x << 4 | 8;
		int z = chunk.z << 4 | 8;
		level.updateArea(x, level.getBottomY(), z, x, level.getTopY(), z);
	}
	
	@Inject(method = "isChunkLoaded", at = @At("HEAD"), cancellable = true)
	private void optimancer_isChunkLoaded(int x, int z, CallbackInfoReturnable<Boolean> info) {
		long index = OptimancerMathHelper.pack(x, z);
		Chunk chunk = optimancer_getStorage().get(index);
		info.setReturnValue(chunk != null && !(chunk instanceof NullChunk));
	}
	
	@Inject(method = "getAndRemoveChunk", at = @At("HEAD"), cancellable = true)
	private void optimancer_getAndRemoveChunk(int x, int z, CallbackInfo info) {
		info.cancel();
		long index = OptimancerMathHelper.pack(x, z);
		Chunk chunk = optimancer_getStorage().get(index);
		if (chunk == null) return;
		if (chunk.isClient()) chunk.removeEntitiesFromLevel();
		optimancer_getStorage().remove(index);
	}
	
	@Redirect(method = "loadChunk", at = @At(
		value = "INVOKE_ASSIGN",
		target = "Lnet/minecraft/util/maths/Vec2I;<init>(II)V"
	))
	private void optimancer_disableVectorCreation(Vec2I instance, int x, int z) {}
	
	@Inject(method = "loadChunk", at = @At(
		value = "INVOKE",
		target = "Ljava/util/Arrays;fill([BB)V",
		shift = Shift.AFTER
	), cancellable = true)
	private void optimancer_loadChunk(int x, int z, CallbackInfoReturnable<Chunk> info, @Local Chunk chunk) {
		long index = OptimancerMathHelper.pack(x, z);
		optimancer_getStorage().put(index, chunk);
		chunk.canHaveBlockEntities = true;
		info.setReturnValue(chunk);
	}
	
	@Unique
	@SuppressWarnings("unchecked")
	private ConcurrentLong2ReferenceMap<Chunk> optimancer_getStorage() {
		return (ConcurrentLong2ReferenceMap<Chunk>) multiplayerChunkCache;
	}
}

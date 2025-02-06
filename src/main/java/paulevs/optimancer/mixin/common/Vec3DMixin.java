package paulevs.optimancer.mixin.common;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.maths.Vec3D;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import paulevs.optimancer.collection.VectorCache;

@Mixin(Vec3D.class)
public class Vec3DMixin {
	@Unique
	private static final ThreadLocal<VectorCache> OPTIMANCER_CACHE = ThreadLocal.withInitial(VectorCache::new);
	
	@Environment(EnvType.CLIENT)
	@Inject(method = "cleanCache", at = @At("HEAD"), cancellable = true)
	private static void optimancer_cleanCache(CallbackInfo info) {
		OPTIMANCER_CACHE.get().clear();
		info.cancel();
	}
	
	@Inject(method = "resetCache", at = @At("HEAD"), cancellable = true)
	private static void optimancer_resetCache(CallbackInfo info) {
		OPTIMANCER_CACHE.get().reset();
		info.cancel();
	}
	
	@Inject(method = "getFromCacheAndSet", at = @At("HEAD"), cancellable = true)
	private static void optimancer_getFromCacheAndSet(double x, double y, double z, CallbackInfoReturnable<Vec3D> info) {
		info.setReturnValue(OPTIMANCER_CACHE.get().get(x, y, z));
	}
}

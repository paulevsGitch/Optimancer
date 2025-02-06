package paulevs.optimancer.mixin.common;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.entity.Entity;
import net.minecraft.level.Level;
import net.minecraft.level.LightType;
import net.minecraft.level.LightUpdateArea;
import net.minecraft.level.dimension.Dimension;
import net.minecraft.util.maths.Vec2I;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import paulevs.optimancer.collection.ConcurrentFIFOQueue;
import paulevs.optimancer.helper.GameHelper;
import paulevs.optimancer.world.OptimancerLevel;

import java.util.Set;

@Mixin(Level.class)
public abstract class LevelMixin implements OptimancerLevel {
	@Unique private final ConcurrentFIFOQueue<LightUpdateArea> optimancer_lightUpdateQueue = new ConcurrentFIFOQueue<>(8192);
	
	@Shadow @Final public Dimension dimension;
	@Shadow protected int saveTicks;
	
	@Inject(method = "processLevel", at = @At("HEAD"))
	private void optimancer_changeSaveRate(CallbackInfo info) {
		saveTicks = 1200;
	}
	
	@Inject(method = "updateLight()Z", at = @At("HEAD"), cancellable = true)
	private void optimancer_disableLightUpdates(CallbackInfoReturnable<Boolean> info) {
		info.setReturnValue(false);
	}
	
	@Inject(method = "updateLight(Lnet/minecraft/level/LightType;IIIIIIZ)V", at = @At("HEAD"), cancellable = true)
	private void optimancer_addLightUpdate(LightType type, int x1, int y1, int z1, int x2, int y2, int z2, boolean cascadeUpdate, CallbackInfo info) {
		if (type == LightType.SKY && dimension.noSkyLight) return;
		optimancer_lightUpdateQueue.add(new LightUpdateArea(type, x1, y1, z1, x2, y2, z2));
		info.cancel();
	}
	
	@Override
	public void optimancer_processLights() {
		byte count = (byte) Math.min(64, optimancer_lightUpdateQueue.size());
		Level level = Level.class.cast(this);
		for (byte i = 0; i < count; i++) {
			LightUpdateArea area = optimancer_lightUpdateQueue.get();
			area.process(level);
		}
	}
	
	@SuppressWarnings("rawtypes")
	@WrapOperation(method = "processBlockTicks", at = @At(value = "INVOKE", target = "Ljava/util/Set;size()I"))
	private int optimancer_changeSize(Set set, Operation<Integer> original, @Local int lastSize) {
		return lastSize;
	}
	
	@SuppressWarnings("rawtypes")
	@WrapOperation(method = "processLoadedChunks", at = @At(
		value = "INVOKE",
		target = "Ljava/util/Set;add(Ljava/lang/Object;)Z"
	))
	private boolean optimancer_removeUnloaded(Set loadedChunkPositions, Object objVector, Operation<Boolean> original) {
		Vec2I pos = (Vec2I) objVector;
		if (GameHelper.isInvalidChunk(Level.class.cast(this), pos.x, pos.z)) return false;
		return original.call(loadedChunkPositions, objVector);
	}
	
	@WrapOperation(method = "processEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;tick()V"))
	private void optimancer_skipTick(Entity entity, Operation<Void> original) {
		if (GameHelper.isInvalidChunk(entity.level, entity.chunkX, entity.chunkZ)) return;
		original.call(entity);
	}
	
	@Inject(method = "addEntityWithChecks(Lnet/minecraft/entity/Entity;Z)V", at = @At("HEAD"), cancellable = true)
	private void optimancer_checkChunk(Entity entity, boolean applyChecks, CallbackInfo info) {
		if (GameHelper.isInvalidChunk(entity.level, entity.chunkX, entity.chunkZ)) info.cancel();
	}
	
	@Inject(method = "processLevel", at = @At("HEAD"), cancellable = true)
	private void optimancer_skipProcessing(CallbackInfo info) {
		info.cancel();
	}
}

package paulevs.optimancer.mixin.common;

import net.minecraft.level.Level;
import net.minecraft.level.LightType;
import net.minecraft.level.LightUpdateArea;
import net.minecraft.level.dimension.Dimension;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import paulevs.optimancer.util.ConcurrentFIFOQueue;
import paulevs.optimancer.world.OptimancerLevel;

@Mixin(Level.class)
public class LevelMixin implements OptimancerLevel {
	@Unique private final ConcurrentFIFOQueue<LightUpdateArea> optimancer_lightUpdateQueue = new ConcurrentFIFOQueue<>();
	
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
}

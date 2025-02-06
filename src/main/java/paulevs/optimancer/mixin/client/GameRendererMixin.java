package paulevs.optimancer.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.LevelRenderer;
import net.minecraft.entity.living.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import paulevs.optimancer.render.OptimancerLevelRenderer;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
	@WrapOperation(method = "renderScreen", at = @At(value = "INVOKE", target = "Ljava/lang/Thread;sleep(J)V"))
	private void optimancer_disableThreadSleep(long time, Operation<Void> original) {}
	
	@WrapOperation(method = "delta", at = @At(
		value = "INVOKE",
		target = "Lnet/minecraft/client/render/LevelRenderer;updateOcclusion(Lnet/minecraft/entity/living/LivingEntity;ID)I",
		ordinal = 0
	))
	private int optimancer_renderLevel(LevelRenderer renderer, LivingEntity entity, int layerIndex, double delta, Operation<Integer> original) {
		OptimancerLevelRenderer.render(entity, delta);
		return 0;
	}
	
	@WrapOperation(method = "delta", at = @At(
		value = "INVOKE",
		target = "Lnet/minecraft/client/render/LevelRenderer;updateOcclusion(Lnet/minecraft/entity/living/LivingEntity;ID)I",
		ordinal = 1
	))
	private int optimancer_disableRender(LevelRenderer renderer, LivingEntity entity, int layerIndex, double delta, Operation<Integer> original) {
		return 0;
	}
}

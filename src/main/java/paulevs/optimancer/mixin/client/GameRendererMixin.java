package paulevs.optimancer.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
	@WrapOperation(method = "renderScreen", at = @At(value = "INVOKE", target = "Ljava/lang/Thread;sleep(J)V"))
	private void optimancer_disableThreadSleep(long time, Operation<Void> original) {}
	
	/*@ModifyConstant(method = "delta", constant = @Constant(longValue = 1000000000L))
	private long optimancer_changeMaxChunkUpdateTime(long original) {
		return 10000000000L;
	}*/
}

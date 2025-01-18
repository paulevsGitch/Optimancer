package paulevs.optimancer.mixin.client;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.TimerHackThread.class)
public class MinecraftTimeHackThreadMixin {
	@Inject(method = "run", at = @At("HEAD"), cancellable = true)
	private void optimancer_stopThread(CallbackInfo info) {
		info.cancel();
	}
}

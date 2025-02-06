package paulevs.optimancer.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.options.GameOptions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameOptions.class)
public class GameOptionsMixin {
	@Shadow protected Minecraft minecraft;
	
	@Inject(method = "saveOptions", at = @At("TAIL"))
	private void optimancer_updateFromOptions(CallbackInfo info) {
		minecraft.levelRenderer.updateFromOptions();
	}
}

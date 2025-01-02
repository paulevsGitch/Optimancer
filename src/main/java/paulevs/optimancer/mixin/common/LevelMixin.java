package paulevs.optimancer.mixin.common;

import net.minecraft.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Level.class)
public class LevelMixin {
	@Shadow protected int saveTicks;
	
	@Inject(method = "processLevel", at = @At("HEAD"))
	private void optimancer_changeSaveRate(CallbackInfo info) {
		saveTicks = 1200;
	}
}

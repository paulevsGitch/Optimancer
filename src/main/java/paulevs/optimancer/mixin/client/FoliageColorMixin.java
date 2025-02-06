package paulevs.optimancer.mixin.client;

import net.minecraft.client.render.block.FoliageColor;
import net.modificationstation.stationapi.api.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(FoliageColor.class)
public class FoliageColorMixin {
	@ModifyVariable(method = "getFoliageColor", at = @At("STORE"), ordinal = 0)
	private static int optimancer_clampTemperature(int original) {
		return MathHelper.clamp(original, 0, 63);
	}
	
	@ModifyVariable(method = "getFoliageColor", at = @At("STORE"), ordinal = 1)
	private static int optimancer_clampWetness(int original) {
		return MathHelper.clamp(original, 0, 63);
	}
}

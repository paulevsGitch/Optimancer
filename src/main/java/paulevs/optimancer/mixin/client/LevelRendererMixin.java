package paulevs.optimancer.mixin.client;

import net.minecraft.client.render.AreaRenderer;
import net.minecraft.client.render.LevelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {
	@Redirect(method = "updateAreasAround", at = @At(
		value = "FIELD",
		target = "Lnet/minecraft/client/render/AreaRenderer;canUpdate:Z"
	))
	private void optimancer_disableSetUpdate(AreaRenderer renderer, boolean value) {}
}

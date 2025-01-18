package paulevs.optimancer.mixin.client;

import net.minecraft.client.render.AreaRenderer;
import net.minecraft.util.maths.Box;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = AreaRenderer.class, priority = 100)
public class AreaRendererMixin {
	@Shadow public boolean canUpdate;
	@Shadow public boolean isVisible;
	@Shadow public Box box;
	
	@Inject(method = "update", at = @At("HEAD"), cancellable = true)
	private void optimancer_disableInvisibleUpdate(CallbackInfo info) {
		if (!this.isVisible) info.cancel();
	}
	
	@Inject(method = "update", at = @At("TAIL"), cancellable = true)
	private void optimancer_disableUpdate(CallbackInfo info) {
		canUpdate = false;
	}
	
	/**
	 * @author paulevs
	 * @reason Changes default check with frustum culling.
	 */
	/*@Overwrite
	public void checkVisibility(BoxCollider collider) {
		isVisible = FrustumHelper.isAreaVisible(box);
	}*/
}

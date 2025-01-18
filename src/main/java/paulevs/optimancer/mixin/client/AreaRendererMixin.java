package paulevs.optimancer.mixin.client;

import net.minecraft.client.render.AreaRenderer;
import net.minecraft.entity.living.player.PlayerEntity;
import net.minecraft.util.maths.BoxCollider;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import paulevs.optimancer.helper.GameHelper;

@Mixin(value = AreaRenderer.class, priority = 100)
public class AreaRendererMixin {
	@Shadow public boolean canUpdate;
	@Shadow public boolean isVisible;
	@Shadow public int centerX;
	@Shadow public int centerY;
	@Shadow public int centerZ;
	@Shadow public int deltaX;
	@Shadow public int deltaY;
	@Shadow public int deltaZ;
	@Shadow public int wrappedX;
	@Shadow public int wrappedY;
	@Shadow public int wrappedZ;
	
	@Inject(method = "update", at = @At("HEAD"), cancellable = true)
	private void optimancer_disableInvisibleUpdate(CallbackInfo info) {
		if (!this.isVisible) info.cancel();
	}
	
	@Inject(method = "update", at = @At(value = "INVOKE", target = "Ljava/util/List;removeAll(Ljava/util/Collection;)Z"))
	private void optimancer_disableUpdate(CallbackInfo info) {
		canUpdate = false;
	}
	
	@Inject(method = "setPosition", at = @At(
		value = "INVOKE",
		target = "Lnet/minecraft/util/maths/Box;create(DDDDDD)Lnet/minecraft/util/maths/Box;",
		shift = Shift.BEFORE
	))
	private void optimancer_setWorldPosition(int x, int y, int z, CallbackInfo info) {
		wrappedX = x;
		wrappedY = y;
		wrappedZ = z;
		deltaX = 0;
		deltaY = 0;
		deltaZ = 0;
	}
	
	@Inject(method = "checkVisibility", at = @At("TAIL"))
	private void optimancer_fogCulling(BoxCollider frustum, CallbackInfo info) {
		if (!isVisible) return;
		float distance = GL11.glGetFloat(GL11.GL_FOG_END) + 11.3F;
		PlayerEntity player = GameHelper.getClient().player;
		float dx = (float) (centerX - player.x);
		float dy = (float) (centerY - player.y);
		float dz = (float) (centerZ - player.z);
		isVisible = dx * dx + dy * dy + dz * dz < distance * distance;
	}
}

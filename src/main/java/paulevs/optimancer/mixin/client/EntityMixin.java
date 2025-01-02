package paulevs.optimancer.mixin.client;

import net.minecraft.entity.Entity;
import net.minecraft.util.maths.Box;
import net.minecraft.util.maths.Vec3D;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import paulevs.optimancer.helper.FrustumHelper;

@Mixin(Entity.class)
public class EntityMixin {
	@Shadow @Final public Box boundingBox;
	
	@Inject(method = "canRenderFrom", at = @At("HEAD"), cancellable = true)
	private void optimancer_cullEntity(Vec3D pos, CallbackInfoReturnable<Boolean> info) {
		if (!FrustumHelper.isAreaVisible(
			boundingBox.minX,
			boundingBox.minY,
			boundingBox.minZ,
			boundingBox.maxX,
			boundingBox.maxY,
			boundingBox.maxZ
		)) info.setReturnValue(false);
	}
}

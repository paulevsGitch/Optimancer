package paulevs.optimancer.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.entity.Entity;
import net.minecraft.level.Level;
import net.minecraft.util.maths.Box;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import paulevs.optimancer.helper.FrustumHelper;
import paulevs.optimancer.helper.GameHelper;

@Mixin(Entity.class)
public class EntityMixin {
	@Shadow @Final public Box boundingBox;
	@Shadow public Level level;
	@Shadow public int chunkX;
	@Shadow public int chunkZ;
	@Shadow public int ticks;
	
	@Unique private boolean optimancer_isVisible = true;
	
	@ModifyReturnValue(method = "canRenderFrom", at = @At("RETURN"))
	private boolean optimancer_cullEntity(boolean original) {
		if (!original && (ticks & 7) == 0) {
			optimancer_isVisible = FrustumHelper.isAreaVisible(boundingBox);
		}
		return original && optimancer_isVisible;
	}
	
	@Inject(method = "move", at = @At("HEAD"), cancellable = true)
	private void optimancer_checkChunkMove(double x, double y, double z, CallbackInfo info) {
		if (GameHelper.isInvalidChunk(level, chunkX, chunkZ)) info.cancel();
	}
}

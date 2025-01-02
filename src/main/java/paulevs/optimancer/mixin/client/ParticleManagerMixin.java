package paulevs.optimancer.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.render.Tessellator;
import net.minecraft.entity.technical.ParticleEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import paulevs.optimancer.helper.FrustumHelper;

@Mixin(ParticleManager.class)
public class ParticleManagerMixin {
	@WrapOperation(method = "renderAll", at = @At(
		value = "INVOKE",
		target = "Lnet/minecraft/entity/technical/ParticleEntity;render(Lnet/minecraft/client/render/Tessellator;FFFFFF)V"
	))
	private void optimancer_cullParticle(ParticleEntity entity, Tessellator tessellator, float delta, float x, float y, float z, float width, float height, Operation<Void> original) {
		if (FrustumHelper.isCubeVisible(entity.x, entity.y, entity.z, 0.25F)) {
			original.call(entity, tessellator, delta, x, y, z, width, height);
		}
	}
}

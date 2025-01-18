package paulevs.optimancer.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.AreaRenderer;
import net.minecraft.client.render.LevelRenderer;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.entity.living.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import paulevs.optimancer.render.OptimancerLevelRenderer;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {
	@Shadow int[] unusedArray;
	
	@Shadow private AreaRenderer[] areaRenderersArray;
	
	@Shadow private Minecraft minecraft;
	
	@Inject(method = "<init>", at = @At("TAIL"))
	private void optimancer_onLevelRendererInit(Minecraft minecraft, TextureManager textureManager, CallbackInfo info) {
		unusedArray = null;
	}
	
	@Redirect(method = "updateAreasAround", at = @At(
		value = "FIELD",
		target = "Lnet/minecraft/client/render/AreaRenderer;canUpdate:Z"
	))
	private void optimancer_disableSetUpdate(AreaRenderer renderer, boolean value) {}
	
	@Inject(method = "updateOcclusion(Lnet/minecraft/entity/living/LivingEntity;ID)I", at = @At(
		value = "INVOKE",
		target = "Lnet/minecraft/client/render/RenderHelper;disableLighting()V",
		shift = Shift.AFTER
	), cancellable = true)
	private void optimancer_customRender(
		LivingEntity entity, int layerIndex, double delta, CallbackInfoReturnable<Integer> info,
		@Local(index = 5) double playerX,
		@Local(index = 7) double playerY,
		@Local(index = 9) double playerZ
	) {
		info.setReturnValue(0);
		if (layerIndex == 0) {
			OptimancerLevelRenderer.renderLevel(areaRenderersArray, playerX, playerY, playerZ, minecraft.options.ao);
		}
	}
	
	/*@ModifyConstant(method = "updateOcclusion(Lnet/minecraft/entity/living/LivingEntity;ID)I", constant = @Constant(intValue = 10))
	private int optimancer_disableCycle(int original) {
		return 0;
	}*/
	
	/*@Inject(method = "updateAreasAround", at = @At("HEAD"), cancellable = true)
	private void optimancer_customUpdates(LivingEntity entity, boolean useDistanceLimit, CallbackInfoReturnable<Boolean> info) {
		info.setReturnValue(false);
	}*/
}

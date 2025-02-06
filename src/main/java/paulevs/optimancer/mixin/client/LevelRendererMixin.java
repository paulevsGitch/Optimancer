package paulevs.optimancer.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.render.AreaRenderer;
import net.minecraft.client.render.LevelRenderer;
import net.minecraft.client.render.RenderList;
import net.minecraft.client.render.block.BlockRenderer;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.entity.living.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import paulevs.optimancer.render.OptimancerLevelRenderer;

import java.nio.IntBuffer;

@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {
	@Shadow private AreaRenderer[] areaRenderersArray;
	@Shadow private AreaRenderer[] areaRenderersCache;
	@Shadow private RenderList[] areaRenderLists;
	@Shadow private BlockRenderer blockRenderer;
	@Shadow private IntBuffer occludersBuffer;
	@Shadow int[] unusedArray;
	@Shadow private int sectionCounX;
	
	@Inject(method = "<init>", at = @At("TAIL"))
	private void optimancer_onLevelRendererInit(Minecraft minecraft, TextureManager textureManager, CallbackInfo info) {
		areaRenderersCache = null;
		areaRenderersArray = null;
		areaRenderLists = null;
		occludersBuffer = null;
		blockRenderer = null;
		unusedArray = null;
	}
	
	@Inject(method = "updateAllAreas", at = @At("HEAD"), cancellable = true)
	private void optimancer_disableAllAreasUpdate(CallbackInfo info) {
		info.cancel();
	}
	
	@Inject(method = "updateAreasAround", at = @At("HEAD"), cancellable = true)
	private void optimancer_disableAreasUpdate(LivingEntity entity, boolean useLimit, CallbackInfoReturnable<Boolean> info) {
		info.setReturnValue(true);
	}
	
	@Inject(method = "updateFromOptions", at = @At(
		value = "FIELD",
		target = "Lnet/minecraft/client/render/LevelRenderer;sectionCounZ:I",
		ordinal = 0
	), cancellable = true)
	private void optimancer_updateFromOptions(CallbackInfo info) {
		info.cancel();
		OptimancerLevelRenderer.update(sectionCounX >> 1);
	}
	
	@Inject(method = "checkVisibility", at = @At("HEAD"), cancellable = true)
	private void optimancer_disableCheckVisibility(CallbackInfo info) {
		info.cancel();
	}
	
	@Inject(method = "updateArea", at = @At("HEAD"), cancellable = true)
	private void optimancer_updateArea(int x1, int y1, int z1, int x2, int y2, int z2, CallbackInfo info) {
		OptimancerLevelRenderer.requestUpdate(x1, y1, z1, x2, y2, z2);
		info.cancel();
	}
	
	@Inject(method = "updateOcclusion(Lnet/minecraft/entity/living/LivingEntity;ID)I", at = @At("HEAD"), cancellable = true)
	private void optimancer_disableUpdateOcclusion(LivingEntity entity, int layerIndex, double delta, CallbackInfoReturnable<Integer> info) {
		info.setReturnValue(0);
	}
}

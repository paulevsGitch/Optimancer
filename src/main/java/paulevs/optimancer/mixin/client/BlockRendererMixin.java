package paulevs.optimancer.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.block.BlockRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import paulevs.optimancer.render.OptimancerLevelRenderer;

@Mixin(BlockRenderer.class)
public class BlockRendererMixin {
	@ModifyExpressionValue(method = {
		"renderBed",
		"renderTorch",
		"renderRedstoneRepeater",
		"renderPistonHeadY",
		"renderPistonHeadZ",
		"renderPistonHeadX",
		"renderLever",
		"renderFire",
		"renderRedstoneDust",
		"renderRails",
		"renderLadder",
		"renderCross(Lnet/minecraft/block/Block;III)Z",
		"renderCrops",
		"renderTorchSkewed",
		"renderCross(Lnet/minecraft/block/Block;IDDD)V",
		"renderCrop",
		"renderFluid",
		"renderBlock",
		"renderFast",
		"renderCactus(Lnet/minecraft/block/Block;IIIFFF)Z",
		"renderDoor",
		"renderBottomFace",
		"renderTopFace",
		"renderEastFace",
		"renderWestFace",
		"renderNorthFace",
		"renderSouthFace",
		"renderBlockItem"
	}, at = @At(
		value = "FIELD",
		target = "Lnet/minecraft/client/render/Tessellator;INSTANCE:Lnet/minecraft/client/render/Tessellator;"
	))
	private Tessellator optimancer_getTessellator(Tessellator original) {
		return OptimancerLevelRenderer.getTessellator();
	}
}

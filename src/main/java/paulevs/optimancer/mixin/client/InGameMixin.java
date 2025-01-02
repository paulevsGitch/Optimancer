package paulevs.optimancer.mixin.client;

import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.InGame;
import net.minecraft.client.render.Tessellator;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGame.class)
public abstract class InGameMixin extends DrawableHelper {
	@Inject(method = "renderHud", at = @At(
		value = "INVOKE",
		target = "Lorg/lwjgl/opengl/GL11;glColor4f(FFFF)V",
		ordinal = 0
	))
	private void optimancer_renderHud(float delta, boolean i, int j, int par4, CallbackInfo info) {
		Tessellator tessellator = Tessellator.INSTANCE;
		tessellator.color(1.0F, 1.0F, 1.0F, 1.0F);
		
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
		
		tessellator.start();
		tessellator.color(1.0F, 0.0F, 0.0F, 1.0F);
		tessellator.vertex(0F, 0F, 0F, 0F, 0F);
		tessellator.vertex(0.0F, 100.0F, 0F, 0F, 0F);
		tessellator.vertex(100.0F, 100.0F, 0F, 0F, 0F);
		tessellator.vertex(100.0F, 0.0F, 0.0F, 0F, 0F);
		tessellator.render();
	}
}

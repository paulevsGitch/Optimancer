package paulevs.optimancer.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.AreaRenderer;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import java.nio.IntBuffer;

@Environment(EnvType.CLIENT)
public class OptimancerLevelRenderer {
	private static final int MAX_COUNT = 4096;
	private static final int LAST_INDEX = MAX_COUNT - 1;
	private static final IntBuffer SOLID_CHUNKS = BufferUtils.createIntBuffer(MAX_COUNT);
	private static final IntBuffer TRANSPARENT_CHUNKS = BufferUtils.createIntBuffer(MAX_COUNT);
	
	public static void renderLevel(AreaRenderer[] areaRenderersArray, double playerX, double playerY, double playerZ, boolean hasAO) {
		AreaRenderer area = areaRenderersArray[0];
		int deltaX = area.deltaX;
		int deltaY = area.deltaY;
		int deltaZ = area.deltaZ;
		
		SOLID_CHUNKS.limit(MAX_COUNT);
		TRANSPARENT_CHUNKS.limit(MAX_COUNT);
		SOLID_CHUNKS.position(0);
		TRANSPARENT_CHUNKS.position(0);
		
		int target;
		for (AreaRenderer render : areaRenderersArray) {
			if (!render.isVisible) continue;
			if (SOLID_CHUNKS.position() < LAST_INDEX) {
				target = render.getLayerTarget(0);
				if (target > -1) SOLID_CHUNKS.put(target);
			}
			if (TRANSPARENT_CHUNKS.position() < LAST_INDEX) {
				target = render.getLayerTarget(1);
				if (target > -1) TRANSPARENT_CHUNKS.put(target);
			}
		}
		
		int countSolid = SOLID_CHUNKS.position();
		int countTransparent = TRANSPARENT_CHUNKS.position();
		
		if (countSolid > 0) {
			SOLID_CHUNKS.limit(countSolid);
			SOLID_CHUNKS.position(0);
		}
		if (countTransparent > 0) {
			TRANSPARENT_CHUNKS.limit(countTransparent);
			TRANSPARENT_CHUNKS.position(0);
		}
		
		GL11.glPushMatrix();
		GL11.glTranslatef((float) (deltaX - playerX), (float) (deltaY - playerY), (float) (deltaZ - playerZ));
		if (countSolid > 0) GL11.glCallLists(SOLID_CHUNKS);
		if (countTransparent > 0) {
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			GL11.glDisable(GL11.GL_CULL_FACE);
			GL11.glCallLists(TRANSPARENT_CHUNKS);
			GL11.glDisable(GL11.GL_BLEND);
		}
		GL11.glPopMatrix();
	}
}

package paulevs.optimancer.render;

import net.minecraft.util.maths.BlockPos;
import net.modificationstation.stationapi.api.util.math.MutableBlockPos;
import org.lwjgl.opengl.GL11;
import paulevs.optimancer.helper.FrustumHelper;

public class OptimancerClientChunk {
	private final MutableBlockPos pos1 = new MutableBlockPos();
	private final MutableBlockPos pos2 = new MutableBlockPos();
	private final MutableBlockPos center = new MutableBlockPos();
	private final VBO solid = new VBO();
	private final VBO translucent = new VBO();
	public boolean needUpdate;
	private boolean visible;
	
	public void setPosition(BlockPos pos) {
		pos1.set(pos.x << 4, pos.y << 4, pos.z << 4);
		pos2.set(pos1.x, pos1.y, pos1.z).move(16, 16, 16);
		center.set(pos1.x, pos1.y, pos1.z).move(8, 8, 8);
	}
	
	public void setVisible(boolean visible) {
		this.visible = visible;
	}
	
	public void updateVisibility(double cameraX, double cameraY, double cameraZ) {
		visible = FrustumHelper.isAreaVisible(pos1, pos2);
		if (visible) {
			float distance = GL11.glGetFloat(GL11.GL_FOG_END) + 11.3F;
			float dx = (float) (center.x - cameraX);
			float dy = (float) (center.y - cameraY);
			float dz = (float) (center.z - cameraZ);
			visible = dx * dx + dy * dy + dz * dz < distance * distance;
		}
	}
	
	public void renderSolid(double cameraX, double cameraY, double cameraZ) {
		if (!visible || solid.isEmpty()) return;
		GL11.glPushMatrix();
		GL11.glTranslatef(
			(float) (pos1.x - cameraX),
			(float) (pos1.y - cameraY),
			(float) (pos1.z - cameraZ)
		);
		solid.render();
		GL11.glPopMatrix();
	}
	
	public void renderTranslucent(double cameraX, double cameraY, double cameraZ) {
		if (!visible || translucent.isEmpty()) return;
		GL11.glPushMatrix();
		GL11.glTranslatef(
			(float) (pos1.x - cameraX),
			(float) (pos1.y - cameraY),
			(float) (pos1.z - cameraZ)
		);
		translucent.render();
		GL11.glPopMatrix();
	}
	
	public void dispose() {
		solid.dispose();
	}
	
	public VBO getSolid() {
		return solid;
	}
	
	public VBO getTranslucent() {
		return translucent;
	}
	
	public boolean isVisible() {
		return visible;
	}
}

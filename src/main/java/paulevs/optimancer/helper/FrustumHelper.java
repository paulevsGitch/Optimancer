package paulevs.optimancer.helper;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.Frustum;
import net.minecraft.entity.Entity;
import net.minecraft.util.maths.Box;

@Environment(EnvType.CLIENT)
public class FrustumHelper {
	private static final Frustum FRUSTUM = (Frustum) Frustum.getInstance();
	
	public static boolean isAreaVisible(Box box) {
		return isAreaVisible(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ);
	}
	
	public static boolean isAreaVisible(double x1, double y1, double z1, double x2, double y2, double z2) {
		Entity viewEntity = GameHelper.getMinecraft().viewEntity;
		return FRUSTUM.isInside(
			x1 - viewEntity.x,
			y1 - viewEntity.y,
			z1 - viewEntity.z,
			x2 - viewEntity.x,
			y2 - viewEntity.y,
			z2 - viewEntity.z
		);
	}
	
	public static boolean isCubeVisible(double x, double y, double z, double side) {
		return isAreaVisible(x - side, y - side, z - side, x + side, y + side, z + side);
	}
}

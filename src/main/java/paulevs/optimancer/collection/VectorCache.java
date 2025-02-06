package paulevs.optimancer.collection;

import net.minecraft.util.maths.Vec3D;

import java.util.ArrayList;
import java.util.List;

public class VectorCache {
	final List<Vec3D> data = new ArrayList<>();
	int size;
	
	public Vec3D get(double x, double y, double z) {
		Vec3D result;
		if (size == data.size()) {
			result = Vec3D.make(x, y, z);
			data.add(result);
		}
		else result = data.get(size);
		size++;
		return result;
	}
	
	public void reset() {
		data.clear();
		size = 0;
	}
	
	public void clear() {
		size = 0;
	}
}

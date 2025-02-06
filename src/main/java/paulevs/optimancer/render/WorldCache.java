package paulevs.optimancer.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.maths.BlockPos;
import net.modificationstation.stationapi.api.util.math.MutableBlockPos;

import java.util.Arrays;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Environment(EnvType.CLIENT)
public class WorldCache<T> {
	private final MutableBlockPos[] positions;
	private final BlockPos[] offsets;
	private final T[] data;
	private final int radius;
	private final int side;
	private final int sideSqr;
	private volatile int centerX;
	private volatile int centerY;
	private volatile int centerZ;
	
	@SuppressWarnings("unchecked")
	public WorldCache(Supplier<T> constructor, int radius) {
		this.radius = radius;
		side = radius << 1 | 1;
		sideSqr = side * side;
		data = (T[]) new Object[sideSqr * side];
		
		int index = 0;
		offsets = new BlockPos[data.length];
		for (int x = -radius; x <= radius; x++) {
			for (int z = -radius; z <= radius; z++) {
				for (int y = -radius; y <= radius; y++) {
					offsets[index++] = new BlockPos(x, y, z);
				}
			}
		}
		
		Arrays.sort(offsets, (pos1, pos2) -> {
			int l1 = pos1.x * pos1.x + pos1.y * pos1.y + pos1.z * pos1.z;
			int l2 = pos2.x * pos2.x + pos2.y * pos2.y + pos2.z * pos2.z;
			return Integer.compare(l1, l2);
		});
		
		positions = new MutableBlockPos[data.length];
		for (int i = 0; i < data.length; i++) {
			positions[i] = new MutableBlockPos(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
			data[i] = constructor.get();
		}
	}
	
	public void setCenter(int x, int y, int z) {
		centerX = x;
		centerY = y;
		centerZ = z;
	}
	
	public void forEach(BiConsumer<BlockPos, T> action, BiConsumer<BlockPos, T> update, boolean forward) {
		for (int i = 0; i < offsets.length; i++) {
			BlockPos offset = forward ? offsets[i] : offsets[offsets.length - i - 1];
			int wx = centerX + offset.x;
			int wy = centerY + offset.y;
			int wz = centerZ + offset.z;
			int index = getIndex(wx, wy, wz);
			T value = data[index];
			MutableBlockPos pos = positions[index];
			if (pos.x == wx && pos.y == wy && pos.z == wz) {
				action.accept(pos, value);
			}
			else {
				pos.set(wx, wy, wz);
				update.accept(new BlockPos(wx, wy, wz), value);
			}
		}
	}
	
	public void forEach(BiConsumer<BlockPos, T> action) {
		for (BlockPos offset : offsets) {
			int wx = centerX + offset.x;
			int wy = centerY + offset.y;
			int wz = centerZ + offset.z;
			int index = getIndex(wx, wy, wz);
			T value = data[index];
			positions[index].set(wx, wy, wz);
			action.accept(new BlockPos(wx, wy, wz), value);
		}
	}
	
	public void forEach(Consumer<T> action) {
		for (T value : data) action.accept(value);
	}
	
	public T get(int x, int y, int z) {
		if (Math.abs(x - centerX) > radius) return null;
		if (Math.abs(y - centerY) > radius) return null;
		if (Math.abs(z - centerZ) > radius) return null;
		int index = getIndex(x, y, z);
		//BlockPos pos = positions[index];
		//if (pos.x != x || pos.y != y || pos.z != z) return null;
		return data[index];
	}
	
	private int getIndex(int x, int y, int z) {
		x = Math.floorMod(x, side);
		y = Math.floorMod(y, side);
		z = Math.floorMod(z, side);
		return x * sideSqr + y * side + z;
	}
}

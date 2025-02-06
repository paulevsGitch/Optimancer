package paulevs.optimancer.world;

import net.minecraft.level.Level;
import net.modificationstation.stationapi.impl.world.chunk.FlattenedChunk;

public class PromiseChunk extends FlattenedChunk {
	public PromiseChunk(Level level, int x, int z) {
		super(level, x, z);
		decorated = true;
	}
}

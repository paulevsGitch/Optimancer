package paulevs.optimancer.world;

import net.modificationstation.stationapi.impl.world.chunk.FlattenedChunk;

public interface OptimancerLevelSource {
	default void optimancer_setChunk(FlattenedChunk chunk) {}
}

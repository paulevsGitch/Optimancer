package paulevs.optimancer.world;

import it.unimi.dsi.fastutil.longs.Long2ReferenceMap;
import it.unimi.dsi.fastutil.longs.Long2ReferenceOpenHashMap;
import net.minecraft.level.Level;
import net.minecraft.level.LightType;
import net.minecraft.level.LightUpdateArea;
import net.minecraft.level.chunk.Chunk;
import net.minecraft.level.source.LevelSource;
import net.modificationstation.stationapi.impl.world.chunk.ChunkSection;
import net.modificationstation.stationapi.impl.world.chunk.FlattenedChunk;
import paulevs.optimancer.mixin.common.LevelAccessor;
import paulevs.optimancer.util.ConcurrentFIFOQueue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LightUpdateLevel extends Level {
	private final Long2ReferenceMap<FlattenedChunk> chunks = new Long2ReferenceOpenHashMap<>();
	private final ConcurrentFIFOQueue<LightUpdateArea> lightRequests = new ConcurrentFIFOQueue<>();
	private final ConcurrentFIFOQueue<LightUpdateArea> areasToUpdate = new ConcurrentFIFOQueue<>();
	private final List<LightUpdateArea> lightUpdates = Collections.synchronizedList(new ArrayList<>());
	private final LevelSource source;
	private final Level level;
	
	public LightUpdateLevel(Level source) {
		super(
			((LevelAccessor) source).optimancer_getDimData(),
			source.getProperties().getName(),
			source.getSeed(),
			source.dimension
		);
		this.source = source.getCache();
		this.level = source;
		//((OptimancerLevel) source).optimancer_setLightUpdate(this);
	}
	
	@Override
	public int getHeight() {
		return level.getHeight();
	}
	
	@Override
	public int getBottomY() {
		return level.getBottomY();
	}
	
	@Override
	public int getTopY() {
		return level.getTopY();
	}
	
	@Override
	public Chunk getChunkFromCache(int x, int z) {
		long index = pack(x, z);
		FlattenedChunk chunk = chunks.get(index);
		if (chunk == null) {
			synchronized (source) {
				if (source.isChunkLoaded(x, z)) chunk = (FlattenedChunk) source.getChunk(x, z);
			}
			if (chunk == null) return null;
			for (int i = 0; i < chunk.sections.length; i++) {
				if (chunk.sections[i] != null) continue;
				chunk.sections[i] = new ChunkSection(level.sectionCoordToIndex(i));
			}
			chunks.put(index, copyFromSource(chunk));
		}
		return chunk;
	}
	
	@Override
	public boolean isBlockLoaded(int x, int y, int z) {
		return chunks.containsKey(pack(x >> 4, z >> 4));
	}
	
	@Override
	public boolean isAreaLoaded(int x1, int y1, int z1, int x2, int y2, int z2) {
		x1 >>= 4;
		z1 >>= 4;
		x2 >>= 4;
		z2 >>= 4;
		for (int x = x1; x <= x2; x++) {
			for (int z = z1; z <= z2; z++) {
				if (!chunks.containsKey(pack(x >> 4, z >> 4))) return false;
			}
		}
		return true;
	}
	
	@Override
	public boolean updateLight() {
		int count = Math.min(lightRequests.size(), 64);
		for (int i = 0; i < count; i++) {
			LightUpdateArea light = lightRequests.get();
			light.process(this);
			//areasToUpdate.add(light);
		}
		return false;
	}
	
	@Override
	public void updateLight(LightType type, int x1, int y1, int z1, int x2, int y2, int z2, boolean cascadeUpdate) {
		int centerX = (x2 + x1) >> 1;
		int centerZ = (z2 + z1) >> 1;
		Chunk chunk = getChunkFromCache(centerX >> 4, centerZ >> 4);
		if (chunk == null || chunk.isClient()) return;
		/*if (cascadeUpdate) {
			int count = Math.min(5, lightUpdates.size());
			for (int i = 0; i < count; i++) {
				LightUpdateArea area = lightUpdates.get(lightUpdates.size() - i - 1);
				if (area == null) continue;
				if (area.lightType != type || !area.checkAndUpdate(x1, y1, z1, x2, y2, z2)) continue;
				return;
			}
		}*/
		lightRequests.add(new LightUpdateArea(type, x1, y1, z1, x2, y2, z2));
	}
	
	private FlattenedChunk copyFromSource(FlattenedChunk source) {
		FlattenedChunk target = new FlattenedChunk(this, source.x, source.z);
		System.arraycopy(source.sections, 0, target.sections, 0, source.sections.length);
		return target;
	}
	
	public void updateAreas() {
		int count = Math.min(areasToUpdate.size(), 64);
		for (int i = 0; i < count; i++) {
			LightUpdateArea area = areasToUpdate.get();
			level.updateArea(
				area.x1, area.y1, area.z1,
				area.x2, area.y2, area.z2
			);
		}
		if (count > 0) {
			System.out.println("Updated " + count + " areas");
		}
	}
	
	public void process() {
		/*if (lightUpdates.isEmpty()) System.out.println(lightUpdates.size());
		int count = Math.min(lightUpdates.size(), 256);
		for (int i = 0; i < count; i++) {
			LightUpdateArea area = lightUpdates.get();
			if (area == null) continue;
			area.process(this);
			areasToUpdate.add(area);
		}*/
		/*for (FlattenedChunk chunk : chunks.values()) {
			copyBack(chunk);
		}*/
		for (byte i = 0; i < 8; i++) updateLight();
		for (FlattenedChunk chunk : chunks.values()) {
			FlattenedChunk target = null;
			synchronized (source) {
				if (source.isChunkLoaded(chunk.x, chunk.z)) {
					target = (FlattenedChunk) source.getChunk(chunk.x, chunk.z);
				}
			}
			if (target == null) continue;
			System.arraycopy(chunk.sections, 0, target.sections, 0, chunk.sections.length);
		}
		chunks.clear();
	}
	
	/*private void copyBack(FlattenedChunk source) {
		FlattenedChunk target = null;
		synchronized (this.source) {
			if (this.source.isChunkLoaded(source.x, source.z)) {
				target = (FlattenedChunk) this.source.getChunk(source.x, source.z);
			}
		}
		if (target == null) return;
		CopySections(source, target);
		target.needUpdate = true;
	}
	
	private void CopySection(ChunkSection source, ChunkSection target) {
		for (short n = 0; n < 4096; n++) {
			byte x = (byte) (n & 15);
			byte y = (byte) ((n >> 4) & 15);
			byte z = (byte) (n >> 8);
			target.setLight(LightType.BLOCK, x, y, z, source.getLight(LightType.BLOCK, x, y, z));
		}
	}
	
	private void CopySections(FlattenedChunk source, FlattenedChunk target) {
		for (int i = 0; i < source.sections.length; i++) {
			if (source.sections[i] == null || source.sections[i] == target.sections[i]) continue;
			if (target.sections[i] == null) {
				target.sections[i] = new ChunkSection(i);
			}
			CopySection(source.sections[i], target.sections[i]);
		}
	}*/
	
	private static long pack(int x, int z) {
		return (long) x << 32L | (long) z & 0xFFFFFFFFL;
	}
	
	private static int getX(long index) {
		return (int) (index >> 32);
	}
	
	private static int getZ(long index) {
		return (int) index;
	}
}

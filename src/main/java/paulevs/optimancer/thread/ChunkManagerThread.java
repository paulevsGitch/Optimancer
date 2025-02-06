package paulevs.optimancer.thread;

import net.minecraft.level.Level;
import net.minecraft.level.LevelProperties;
import net.minecraft.level.storage.RegionLoader;
import net.minecraft.util.io.CompoundTag;
import net.minecraft.util.io.NBTIO;
import net.modificationstation.stationapi.api.datafixer.TypeReferences;
import net.modificationstation.stationapi.api.nbt.NbtHelper;
import net.modificationstation.stationapi.impl.world.FlattenedWorldManager;
import net.modificationstation.stationapi.impl.world.chunk.FlattenedChunk;
import paulevs.optimancer.Optimancer;
import paulevs.optimancer.collection.ConcurrentFIFOQueue;
import paulevs.optimancer.collection.ConcurrentLongQueue;
import paulevs.optimancer.collection.ConcurrentLongSet;
import paulevs.optimancer.helper.OptimancerMathHelper;
import paulevs.optimancer.mixin.common.DimensionDataHandlerAccessor;
import paulevs.optimancer.world.NullChunk;
import paulevs.optimancer.world.PromiseChunk;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;

import static net.modificationstation.stationapi.impl.world.FlattenedWorldManager.SECTIONS;

public class ChunkManagerThread extends OptimancerThread {
	private final ConcurrentFIFOQueue<FlattenedChunk> chunksToSave = new ConcurrentFIFOQueue<>(512);
	private final ConcurrentFIFOQueue<FlattenedChunk> chunksToInsert = new ConcurrentFIFOQueue<>(512);
	private final ConcurrentLongQueue chunksToLoad = new ConcurrentLongQueue(512);
	private final ConcurrentLongSet loadingChunks = new ConcurrentLongSet();
	private final File dimFolder;
	private final Level level;
	
	public ChunkManagerThread(String name, Level level) {
		super(name);
		File root = ((DimensionDataHandlerAccessor) level.dimDataHandler).optimancer_getDimensionData().getFile("");
		root = root.getParentFile().getParentFile();
		int id = level.dimension.id;
		if (level.dimension.id != 0) root = new File(root, "DIM" + id);
		this.dimFolder = root;
		this.level = level;
	}
	
	@Override
	protected void process() {
		int count = Math.min(chunksToSave.size(), 64);
		for (int i = 0; i < count; i++) {
			FlattenedChunk chunk = chunksToSave.get();
			level.checkSessionLock();
			DataOutputStream stream = RegionLoader.getOutputStream(dimFolder, chunk.x, chunk.z);
			CompoundTag rootTag = new CompoundTag();
			CompoundTag levelTag = new CompoundTag();
			rootTag.put("Level", levelTag);
			FlattenedWorldManager.saveChunk(chunk, level, levelTag);
			rootTag = NbtHelper.addDataVersions(rootTag);
			NBTIO.writeTag(rootTag, stream);
			
			try {
				stream.close();
			}
			catch (IOException e) {
				//noinspection CallToPrintStackTrace
				e.printStackTrace();
			}
			
			LevelProperties properties = level.getProperties();
			int delta = RegionLoader.getSizeDelta(dimFolder, chunk.x, chunk.z);
			properties.setSizeOnDisk(properties.getSizeOnDisk() + delta);
		}
		
		count = Math.min(chunksToLoad.size(), 64);
		for (int i = 0; i < count; i++) {
			long index = chunksToLoad.get();
			
			int x = OptimancerMathHelper.getX(index);
			int z = OptimancerMathHelper.getZ(index);
			
			DataInputStream stream = RegionLoader.getInputStream(dimFolder, x, z);
			if (stream == null) {
				chunksToInsert.add(new NullChunk(level, x, z));
				continue;
			}
			
			CompoundTag tag = NBTIO.readTag(stream);
			if (!tag.containsKey("Level")) {
				Optimancer.LOGGER.error("Chunk file at " + x + "," + z + " is missing level data, skipping");
				chunksToInsert.add(new NullChunk(level, x, z));
				loadingChunks.remove(index);
				continue;
			}
			
			tag = NbtHelper.update(TypeReferences.CHUNK, tag);
			if (!tag.getCompoundTag("Level").containsKey(SECTIONS)) {
				Optimancer.LOGGER.error("Chunk file at " + x + "," + z + " is missing section data, skipping");
				chunksToInsert.add(new NullChunk(level, x, z));
				loadingChunks.remove(index);
				continue;
			}
			
			FlattenedChunk chunk = (FlattenedChunk) FlattenedWorldManager.loadChunk(level, tag.getCompoundTag("Level"));
			if (!chunk.equalPosition(x, z)) {
				Optimancer.LOGGER.error("Chunk file at " + x + "," + z + " is in the wrong location; relocating. (Expected " + x + ", " + z + ", got " + chunk.x + ", " + chunk.z + ")");
				tag.put("xPos", x);
				tag.put("zPos", z);
				chunk = (FlattenedChunk) FlattenedWorldManager.loadChunk(level, tag.getCompoundTag("Level"));
			}
			chunk.setBlockMask();
			
			chunksToInsert.add(chunk);
			loadingChunks.remove(index);
		}
	}
	
	@Override
	protected void onFinish() {
		int count = chunksToSave.size();
		for (int i = 0; i < count; i++) {
			FlattenedChunk chunk = chunksToSave.get();
			level.checkSessionLock();
			DataOutputStream stream = RegionLoader.getOutputStream(dimFolder, chunk.x, chunk.z);
			CompoundTag rootTag = new CompoundTag();
			CompoundTag levelTag = new CompoundTag();
			rootTag.put("Level", levelTag);
			FlattenedWorldManager.saveChunk(chunk, level, levelTag);
			rootTag = NbtHelper.addDataVersions(rootTag);
			NBTIO.writeTag(rootTag, stream);
			
			try {
				stream.close();
			}
			catch (IOException e) {
				//noinspection CallToPrintStackTrace
				e.printStackTrace();
			}
			
			LevelProperties properties = level.getProperties();
			int delta = RegionLoader.getSizeDelta(dimFolder, chunk.x, chunk.z);
			properties.setSizeOnDisk(properties.getSizeOnDisk() + delta);
		}
	}
	
	public void processMain() {
		int count = Math.min(chunksToInsert.size(), 64);
		for (int i = 0; i < count; i++) {
			FlattenedChunk chunk = chunksToInsert.get();
			level.getCache().optimancer_setChunk(chunk);
		}
	}
	
	public void saveChunk(FlattenedChunk chunk) {
		chunksToSave.add(chunk);
	}
	
	public FlattenedChunk loadChunk(int x, int z) {
		long index = OptimancerMathHelper.pack(x, z);
		if (!loadingChunks.contains(index)) {
			loadingChunks.add(index);
			chunksToLoad.add(index);
		}
		return new PromiseChunk(level, x, z);
	}
}

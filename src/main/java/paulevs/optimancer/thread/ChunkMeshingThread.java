package paulevs.optimancer.thread;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.block.BlockRenderer;
import net.minecraft.level.Level;
import net.minecraft.level.source.LevelSource;
import net.minecraft.util.maths.BlockPos;
import net.modificationstation.stationapi.api.block.BlockState;
import net.modificationstation.stationapi.api.client.StationRenderAPI;
import net.modificationstation.stationapi.api.client.model.block.BlockWithWorldRenderer;
import net.modificationstation.stationapi.api.client.render.RendererAccess;
import net.modificationstation.stationapi.api.client.render.model.BakedModel;
import net.modificationstation.stationapi.api.client.render.model.BakedModelRenderer;
import net.modificationstation.stationapi.api.util.math.MutableBlockPos;
import net.modificationstation.stationapi.impl.world.chunk.ChunkSection;
import net.modificationstation.stationapi.impl.world.chunk.FlattenedChunk;
import paulevs.optimancer.collection.ConcurrentFIFOQueue;
import paulevs.optimancer.helper.GameHelper;
import paulevs.optimancer.render.OptimancerClientChunk;
import paulevs.optimancer.render.OptimancerLevelRenderer;
import paulevs.optimancer.render.OptimancerTessellator;
import paulevs.optimancer.render.VBO;
import paulevs.optimancer.render.WorldCache;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Random;

@Environment(EnvType.CLIENT)
public class ChunkMeshingThread extends OptimancerThread {
	private static final BakedModelRenderer MODEL_RENDERER = Objects.requireNonNull(RendererAccess.INSTANCE.getRenderer()).bakedModelRenderer();
	private final ConcurrentFIFOQueue<BlockPos> updateQueue = new ConcurrentFIFOQueue<>(4096);
	private final ConcurrentFIFOQueue<BlockPos> updateFast = new ConcurrentFIFOQueue<>(256);
	private final HashSet<BlockPos> updateSet = new HashSet<>();
	private final List<BlockPos> updateOrder = new ArrayList<>(512);
	private final MutableBlockPos modelPos = new MutableBlockPos();
	private final Random random = new Random();
	private final BlockRenderer renderer;
	private final WorldCache<OptimancerClientChunk> cache;
	private final Level level;
	private volatile int centerX;
	private volatile int centerY;
	private volatile int centerZ;
	private int sortCenterX;
	private int sortCenterY;
	private int sortCenterZ;
	
	public ChunkMeshingThread(String name, WorldCache<OptimancerClientChunk> cache, Level level) {
		super(name);
		this.cache = cache;
		this.level = level;
		renderer = new BlockRenderer(level);
	}
	
	public void setCenter(int x, int y, int z) {
		centerX = x;
		centerY = y;
		centerZ = z;
	}
	
	public void requestUpdate(BlockPos pos) {
		if (distanceToCenter(pos) < 4) updateFast.add(pos);
		else updateQueue.add(pos);
	}
	
	@Override
	protected void process() {
		int count = Math.min(512, updateFast.size());
		
		for (short i = 0; i < count; i++) {
			updateSet.add(updateFast.get());
		}
		
		count = Math.min(64, updateQueue.size());
		
		for (short i = 0; i < count; i++) {
			updateSet.add(updateQueue.get());
		}
		
		if (updateSet.isEmpty()) return;
		
		sortCenterX = centerX;
		sortCenterY = centerY;
		sortCenterZ = centerZ;
		updateOrder.addAll(updateSet);
		updateOrder.sort(this::comparePositions);
		
		OptimancerTessellator tessellator = (OptimancerTessellator) OptimancerLevelRenderer.getTessellator();
		LevelSource source = level.getCache();
		
		for (BlockPos pos : updateOrder) {
			OptimancerClientChunk clientChunk = cache.get(pos.x, pos.y, pos.z);
			if (clientChunk == null) continue;
			
			int y = level.sectionCoordToIndex(pos.y);
			if (y < 0) {
				clientChunk.setVisible(false);
				continue;
			}
			
			if (!source.isChunkLoaded(pos.x, pos.z)) continue;
			FlattenedChunk chunk = (FlattenedChunk) source.getChunk(pos.x, pos.z);
			if (GameHelper.isInvalidChunk(chunk)) continue;
			if (y >= chunk.sections.length) {
				clientChunk.setVisible(false);
				continue;
			}
			
			ChunkSection section = chunk.sections[y];
			
			if (section == null) {
				clientChunk.setVisible(false);
				continue;
			}
			
			clientChunk.needUpdate = false;
			
			/*if (!clientChunk.isVisible()) {
				requestUpdate(pos);
				continue;
			}*/
			
			int wx = pos.x << 4;
			int wy = pos.y << 4;
			int wz = pos.z << 4;
			
			tessellator.setOffset(-wx, -wy, -wz);
			
			VBO vbo = clientChunk.getSolid();
			tessellator.start();
			for (short i = 0; i < 4096; i++) {
				modelPos.set(
					wx | (i & 15),
					wy | ((i >> 4) & 15),
					wz | (i >> 8)
				);
				BlockState state = section.getBlockState(modelPos.x & 15, modelPos.y & 15, modelPos.z & 15);
				BakedModel model = StationRenderAPI.getBakedModelManager().getBlockModels().getModel(state);
				if (model.isBuiltin()) {
					//noinspection deprecation
					if (state.getBlock() instanceof BlockWithWorldRenderer blockRenderer) {
						//noinspection deprecation
						blockRenderer.renderWorld(renderer, level, modelPos.x, modelPos.y, modelPos.z);
					}
					else renderer.render(state.getBlock(), modelPos.x, modelPos.y, modelPos.z);
				}
				else {
					MODEL_RENDERER.renderBlock(state, modelPos, level, true, random);
				}
			}
			tessellator.build(vbo);
			
			vbo = clientChunk.getTranslucent();
			tessellator.start();
			for (short i = 0; i < 4096; i++) {
				modelPos.set(
					wx | (i & 15),
					wy | ((i >> 4) & 15),
					wz | (i >> 8)
				);
				BlockState state = section.getBlockState(modelPos.x & 15, modelPos.y & 15, modelPos.z & 15);
				BakedModel model = StationRenderAPI.getBakedModelManager().getBlockModels().getModel(state);
				if (model.isBuiltin()) {
					//noinspection deprecation
					if (state.getBlock() instanceof BlockWithWorldRenderer blockRenderer) {
						//noinspection deprecation
						blockRenderer.renderWorld(renderer, level, modelPos.x, modelPos.y, modelPos.z);
					}
					else renderer.render(state.getBlock(), modelPos.x, modelPos.y, modelPos.z);
				}
				else {
					MODEL_RENDERER.renderBlock(state, modelPos, level, true, random);
				}
			}
			tessellator.build(vbo);
		}
		
		System.out.println("Updated " + updateOrder.size());
		
		updateOrder.clear();
		updateSet.clear();
	}
	
	private int distanceToCenter(BlockPos pos) {
		int dx = pos.x - sortCenterX;
		int dy = pos.y - sortCenterY;
		int dz = pos.z - sortCenterZ;
		return dx * dx + dy * dy + dz * dz;
	}
	
	private int comparePositions(BlockPos pos1, BlockPos pos2) {
		int l1 = distanceToCenter(pos1);
		int l2 = distanceToCenter(pos2);
		return Integer.compare(l1, l2);
	}
}

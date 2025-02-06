package paulevs.optimancer.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.Tessellator;
import net.minecraft.entity.living.LivingEntity;
import net.minecraft.level.Level;
import net.minecraft.util.maths.BlockPos;
import net.modificationstation.stationapi.api.client.StationRenderAPI;
import net.modificationstation.stationapi.api.client.texture.atlas.Atlases;
import net.modificationstation.stationapi.api.util.math.MathHelper;
import org.lwjgl.opengl.GL11;
import paulevs.optimancer.helper.GameHelper;
import paulevs.optimancer.thread.ChunkMeshingThread;

@Environment(EnvType.CLIENT)
public class OptimancerLevelRenderer {
	private static final ChunkMeshingThread[] THREADS = new ChunkMeshingThread[16];
	private static final Thread MAIN = Thread.currentThread();
	private static final ThreadLocal<Tessellator> TESSELLATORS = ThreadLocal.withInitial(() ->
		Thread.currentThread() == MAIN ? Tessellator.INSTANCE : new OptimancerTessellator()
	);
	private static WorldCache<OptimancerClientChunk> clientChunks;
	private static int radius = -1;
	private static double cameraX;
	private static double cameraY;
	private static double cameraZ;
	private static byte updateFrame;
	
	public static void update(int radius) {
		if (OptimancerLevelRenderer.radius == radius) return;
		OptimancerLevelRenderer.radius = radius;
		
		if (clientChunks != null) clientChunks.forEach(OptimancerClientChunk::dispose);
		clientChunks = new WorldCache<>(OptimancerClientChunk::new, radius);
		
		LivingEntity viewEntity = GameHelper.getClient().player;
		Level level = GameHelper.getClient().level;
		
		int cx = viewEntity.chunkX;
		int cy = (int) viewEntity.y >> 4;
		int cz = viewEntity.chunkZ;
		
		for (byte i = 0; i < THREADS.length; i++) {
			if (THREADS[i] != null) THREADS[i].stopThread();
			if (level != null) {
				THREADS[i] = new ChunkMeshingThread("Optimancer Chunk Mesher " + i, clientChunks, level);
				THREADS[i].setCenter(cx, cy, cz);
				THREADS[i].start();
			}
		}
		
		clientChunks.setCenter(cx, cy, cz);
		clientChunks.forEach(OptimancerLevelRenderer::updatePosition);
	}
	
	public static void render(LivingEntity viewEntity, double delta) {
		if (clientChunks == null || viewEntity == null) return;
		
		cameraX = MathHelper.lerp(delta, viewEntity.prevRenderX, viewEntity.x);
		cameraY = MathHelper.lerp(delta, viewEntity.prevRenderY, viewEntity.y);
		cameraZ = MathHelper.lerp(delta, viewEntity.prevRenderZ, viewEntity.z);
		
		int cx = viewEntity.chunkX;
		int cy = (int) viewEntity.y >> 4;
		int cz = viewEntity.chunkZ;
		
		clientChunks.setCenter(cx, cy, cz);
		for (ChunkMeshingThread thread : THREADS) {
			thread.setCenter(cx, cy, cz);
		}
		
		StationRenderAPI.getBakedModelManager().getAtlas(Atlases.GAME_ATLAS_TEXTURE).bindTexture();
		GL11.glEnable(GL11.GL_ALPHA_TEST);
		//GL11.glEnable(GL11.GL_DEPTH_TEST);
		//GL11.glEnable(GL11.GL_BLEND);
		VBO.updateTime();
		clientChunks.forEach(OptimancerLevelRenderer::renderSolid, OptimancerLevelRenderer::updatePosition, true);
		VBO.unbind();
		
		if (++updateFrame > 60) {
			updateFrame = 0;
		}
		
		if (updateFrame == 0) {
			for (byte i = 0; i < THREADS.length; i++) {
				if (THREADS[i].isAlive()) continue;
				Level level = GameHelper.getClient().level;
				THREADS[i] = new ChunkMeshingThread("Optimancer Chunk Mesher " + i, clientChunks, level);
				THREADS[i].start();
			}
		}
	}
	
	private static ChunkMeshingThread getThread(BlockPos pos) {
		int index = (pos.x + pos.y + pos.z) & 15;
		return THREADS[index];
	}
	
	private static void updatePosition(BlockPos pos, OptimancerClientChunk chunk) {
		chunk.setVisible(false);
		chunk.setPosition(pos);
		chunk.needUpdate = true;
		//getThread(pos).requestUpdate(pos);
	}
	
	private static void renderSolid(BlockPos pos, OptimancerClientChunk chunk) {
		if (chunk.needUpdate && chunk.isVisible()) {
			getThread(pos).requestUpdate(pos);
			chunk.needUpdate = false;
		}
		if (updateFrame == 0) {
			chunk.updateVisibility(cameraX, cameraY, cameraZ);
		}
		chunk.renderSolid(cameraX, cameraY, cameraZ);
	}
	
	public static void requestUpdate(int x1, int y1, int z1, int x2, int y2, int z2) {
		x1 = x1 >> 4;
		y1 = y1 >> 4;
		z1 = z1 >> 4;
		x2 = x2 >> 4;
		y2 = y2 >> 4;
		z2 = z2 >> 4;
		
		for (int x = x1; x <= x2; x++) {
			for (int z = z1; z <= z2; z++) {
				for (int y = y1; y <= y2; y++) {
					OptimancerClientChunk chunk = clientChunks.get(x, y, z);
					if (chunk == null) continue;
					chunk.needUpdate = true;
					//BlockPos pos = new BlockPos(x, y, z);
					//getThread(pos).requestUpdate(pos);
				}
			}
		}
	}
	
	public static Tessellator getTessellator() {
		return TESSELLATORS.get();
	}
}

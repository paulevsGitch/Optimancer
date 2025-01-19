package paulevs.optimancer.thread;

import it.unimi.dsi.fastutil.objects.Reference2ReferenceMap;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.level.Level;
import paulevs.optimancer.helper.GameHelper;

public class ThreadManager {
	private static final Reference2ReferenceMap<Level, ChunkManagerThread> CHUNK_LOADERS = new Reference2ReferenceOpenHashMap<>();
	private static boolean running = true;
	
	@Environment(EnvType.CLIENT)
	private static Level lastLevel;
	@Environment(EnvType.CLIENT)
	private static LightUpdateThread lightUpdaterClient;
	
	public static void init() {
		EnvType environment = FabricLoader.getInstance().getEnvironmentType();
		if (environment == EnvType.CLIENT) initClient();
		if (environment == EnvType.SERVER) initServer();
	}
	
	@Environment(EnvType.CLIENT)
	private static void initClient() {
		new ScreenshotThread().start();
	}
	
	@Environment(EnvType.SERVER)
	private static void initServer() {
		Level[] levels = GameHelper.getServer().levels;
		for (Level level : levels) {
			new LightUpdateThread("Optimancer Light Updater (" + level.dimension.id + ")", level).start();
		}
	}
	
	@Environment(EnvType.CLIENT)
	public static void tickClient(Minecraft minecraft) {
		if (minecraft.level != lastLevel) {
			if (minecraft.level == null) {
				lightUpdaterClient.stopThread();
				CHUNK_LOADERS.get(lastLevel).stopThread();
			}
			else {
				lightUpdaterClient = new LightUpdateThread("Optimancer Light Updater", minecraft.level);
				lightUpdaterClient.start();
				ChunkManagerThread loader = new ChunkManagerThread("Optimancer Chunk Manager", minecraft.level);
				CHUNK_LOADERS.put(minecraft.level, loader);
				loader.start();
			}
			lastLevel = minecraft.level;
		}
		if (lastLevel != null) CHUNK_LOADERS.values().forEach(ChunkManagerThread::processMain);
	}
	
	@Environment(EnvType.SERVER)
	public static void tickServer() {
		CHUNK_LOADERS.values().forEach(ChunkManagerThread::processMain);
	}
	
	public static void stop() {
		running = false;
	}
	
	public static boolean canRun() {
		return running;
	}
	
	public static ChunkManagerThread getChunkLoader(Level level) {
		return CHUNK_LOADERS.get(level);
	}
}

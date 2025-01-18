package paulevs.optimancer.thread;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.level.Level;
import net.minecraft.server.MinecraftServer;
import paulevs.optimancer.helper.GameHelper;

public class ThreadManager {
	private static boolean running = true;
	
	@Environment(EnvType.CLIENT)
	private static Level lastLevel;
	@Environment(EnvType.CLIENT)
	private static LightUpdateThread lightUpdaterClient;
	
	@Environment(EnvType.SERVER)
	private static LightUpdateThread[] lightUpdatersServer;
	
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
		lightUpdatersServer = new LightUpdateThread[levels.length];
		for (int i = 0; i < lightUpdatersServer.length; i++) {
			Level level = levels[i];
			lightUpdatersServer[i] = new LightUpdateThread(
				"Optimancer Light Updater (" + level.dimension.id + ")",
				level
			);
			lightUpdatersServer[i].start();
		}
	}
	
	@Environment(EnvType.CLIENT)
	public static void tickClient(Minecraft minecraft) {
		if (minecraft.level != lastLevel) {
			lastLevel = minecraft.level;
			if (lastLevel == null && lightUpdaterClient.isAlive()) {
				lightUpdaterClient.stopThread();
			}
			else {
				lightUpdaterClient = new LightUpdateThread("Optimancer Light Updater", lastLevel);
				lightUpdaterClient.start();
			}
		}
	}
	
	@Environment(EnvType.SERVER)
	public static void tickServer(MinecraftServer server) {
		for (int i = 0; i < lightUpdatersServer.length; i++) {
			if (!lightUpdatersServer[i].isAlive()) {
				Level level = GameHelper.getServer().levels[i];
				lightUpdatersServer[i] = new LightUpdateThread(
					"Optimancer Light Updater (" + level.dimension.id + ")",
					level
				);
			}
		}
	}
	
	public static void stop() {
		running = false;
	}
	
	public static boolean canRun() {
		return running;
	}
}

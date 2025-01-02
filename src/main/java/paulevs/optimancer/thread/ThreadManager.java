package paulevs.optimancer.thread;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;

public class ThreadManager {
	private static boolean running = true;
	
	public static void init() {
		if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
			initClient();
		}
	}
	
	@Environment(EnvType.CLIENT)
	private static void initClient() {
		new ScreenshotThread().start();
	}
	
	public static void stop() {
		running = false;
	}
	
	public static boolean canRun() {
		return running;
	}
}

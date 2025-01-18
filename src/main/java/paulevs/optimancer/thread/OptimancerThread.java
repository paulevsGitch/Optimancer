package paulevs.optimancer.thread;

import net.fabricmc.loader.api.FabricLoader;
import paulevs.optimancer.Optimancer;

public abstract class OptimancerThread extends Thread {
	private boolean run = true;
	
	public OptimancerThread(String name) {
		setName(name);
	}
	
	protected abstract void process();
	
	protected void onFinish() {}
	
	public void stopThread() {
		run = false;
	}
	
	@Override
	public void start() {
		if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
			Optimancer.LOGGER.info("Started thread: " + getName());
		}
		super.start();
	}
	
	@Override
	public void run() {
		while (run && ThreadManager.canRun()) {
			process();
		}
		onFinish();
		if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
			Optimancer.LOGGER.info("Stopped thread: " + getName());
		}
	}
}

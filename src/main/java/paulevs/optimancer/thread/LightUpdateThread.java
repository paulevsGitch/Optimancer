package paulevs.optimancer.thread;

import net.minecraft.level.Level;

public class LightUpdateThread extends OptimancerThread {
	private final Level level;
	
	public LightUpdateThread(String name, Level level) {
		super(name);
		this.level = level;
	}
	
	@Override
	protected void process() {
		if (level != null) {
			level.optimancer_processLights();
		}
	}
}

package paulevs.optimancer.collection;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;

public class ConcurrentLongSet {
	private final LongSet set = new LongOpenHashSet();
	
	public synchronized boolean contains(long value) {
		return set.contains(value);
	}
	
	public synchronized void add(long value) {
		set.add(value);
	}
	
	public synchronized void remove(long value) {
		set.remove(value);
	}
}

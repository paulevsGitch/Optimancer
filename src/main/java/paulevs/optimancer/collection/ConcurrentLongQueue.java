package paulevs.optimancer.collection;

import it.unimi.dsi.fastutil.longs.LongArrayFIFOQueue;

public class ConcurrentLongQueue {
	private final LongArrayFIFOQueue queue;
	
	public ConcurrentLongQueue() {
		queue = new LongArrayFIFOQueue();
	}
	
	public ConcurrentLongQueue(int initialCapacity) {
		queue = new LongArrayFIFOQueue(initialCapacity);
	}
	
	public synchronized long get() {
		return queue.dequeueLong();
	}
	
	public synchronized void add(long value) {
		queue.enqueue(value);
	}
	
	public synchronized boolean isEmpty() {
		return queue.isEmpty();
	}
	
	public synchronized int size() {
		return queue.size();
	}
}

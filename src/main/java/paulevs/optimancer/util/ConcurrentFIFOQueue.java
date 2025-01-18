package paulevs.optimancer.util;

import it.unimi.dsi.fastutil.objects.ObjectArrayFIFOQueue;

public class ConcurrentFIFOQueue<T> {
	private final ObjectArrayFIFOQueue<T> queue = new ObjectArrayFIFOQueue<T>();
	
	public synchronized T get() {
		return queue.dequeue();
	}
	
	public synchronized void add(T value) {
		queue.enqueue(value);
	}
	
	public synchronized boolean isEmpty() {
		return queue.isEmpty();
	}
	
	public synchronized int size() {
		return queue.size();
	}
}

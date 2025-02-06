package paulevs.optimancer.collection;

import it.unimi.dsi.fastutil.objects.ObjectArrayFIFOQueue;

public class ConcurrentFIFOQueue<T> {
	private final ObjectArrayFIFOQueue<T> queue;
	
	public ConcurrentFIFOQueue() {
		queue = new ObjectArrayFIFOQueue<>();
	}
	
	public ConcurrentFIFOQueue(int initialCapacity) {
		queue = new ObjectArrayFIFOQueue<>(initialCapacity);
	}
	
	public synchronized T get() {
		return queue.dequeue();
	}
	
	public synchronized void add(T value) {
		queue.enqueue(value);
	}
	
	public synchronized void addFirst(T value) {
		queue.enqueueFirst(value);
	}
	
	public synchronized boolean isEmpty() {
		return queue.isEmpty();
	}
	
	public synchronized int size() {
		return queue.size();
	}
}

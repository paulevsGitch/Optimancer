package paulevs.optimancer.util;

import it.unimi.dsi.fastutil.ints.Int2ReferenceMap;
import it.unimi.dsi.fastutil.ints.Int2ReferenceOpenHashMap;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ConcurrentInt2ReferenceMap<T> implements Map<Integer, T> {
	private final Int2ReferenceMap<T> map = new Int2ReferenceOpenHashMap<>();
	
	public synchronized T get(int key) {
		return map.get(key);
	}
	
	public synchronized void put(int key, T value) {
		map.put(key, value);
	}
	
	public synchronized void remove(int key) {
		map.remove(key);
	}
	
	@Override
	public synchronized int size() {
		return map.size();
	}
	
	@Override
	public synchronized boolean isEmpty() {
		return map.isEmpty();
	}
	
	@Override
	@Deprecated
	public synchronized boolean containsKey(Object key) {
		return map.containsKey(key);
	}
	
	@Override
	public synchronized boolean containsValue(Object value) {
		return map.containsValue(value);
	}
	
	@Override
	@Deprecated
	public synchronized T get(Object key) {
		return map.get(key);
	}
	
	@Override
	public synchronized T put(Integer key, T value) {
		map.put((int) key, (T) value);
		return value;
	}
	
	@Override
	@Deprecated
	public synchronized T remove(Object key) {
		return map.remove(key);
	}
	
	@Override
	public synchronized void clear() {
		map.clear();
	}
	
	@Override
	public synchronized Set<Integer> keySet() {
		return map.keySet();
	}
	
	@Override
	public synchronized Collection<T> values() {
		return map.values();
	}
	
	@Override
	@Deprecated
	public synchronized Set<Entry<Integer, T>> entrySet() {
		return map.entrySet().stream().collect(Collectors.toUnmodifiableSet());
	}
	
	@Override
	@SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
	public synchronized boolean equals(Object obj) {
		return map.equals(obj);
	}
	
	@Override
	public synchronized int hashCode() {
		return map.hashCode();
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public synchronized void putAll(Map map) {
		this.map.putAll(map);
	}
}

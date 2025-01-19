package paulevs.optimancer.util;

import it.unimi.dsi.fastutil.longs.Long2ReferenceMap;
import it.unimi.dsi.fastutil.longs.Long2ReferenceOpenHashMap;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ConcurrentLong2ReferenceMap<T> implements Map<Long, T> {
	private final Long2ReferenceMap<T> map = new Long2ReferenceOpenHashMap<>();
	
	public synchronized T get(long key) {
		return map.get(key);
	}
	
	public synchronized void put(long key, T value) {
		map.put(key, value);
	}
	
	public synchronized void remove(long key) {
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
	public synchronized T put(Long key, T value) {
		map.put((long) key, (T) value);
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
	public synchronized Set<Long> keySet() {
		return map.keySet();
	}
	
	@Override
	public synchronized Collection<T> values() {
		return map.values();
	}
	
	@Override
	@Deprecated
	public synchronized Set<Entry<Long, T>> entrySet() {
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

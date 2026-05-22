/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Data;

/**
 *
 * @author emimo
 */
import java.util.*;
public class IndexMap<K,V> {
    private final Map<K,V> map = new LinkedHashMap<>();
    private final List<K> order = new ArrayList<>();
    
    public void put(K key, V value) {
        if (!map.containsKey(key)) {
            order.add(key);
        }
        map.put(key, value);
    }
    public V get(K key) {
        return map.get(key);
    }

    public V getByIndex(int index) {
        return map.get(order.get(index));
    }

    public K keyAt(int index) {
        return order.get(index);
    }

    public int indexOf(K key) {
        return order.indexOf(key);
    }

    public int size() {
        return order.size();
    }
    public Set<K> keySet() {
        return map.keySet();
    }

    public Collection<V> values() {
        return map.values();
    }

    public Set<Map.Entry<K, V>> entrySet() {
        return map.entrySet();
    }
}

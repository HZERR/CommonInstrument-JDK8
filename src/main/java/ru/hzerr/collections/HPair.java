package ru.hzerr.collections;

public class HPair<K, V> {

    private final K key;
    private final V value;

    public HPair(K key, V value) { this.key = key; this.value = value; }

    public K getKey() { return this.key; }
    public V getValue() { return this.value; }

    public static <K, V> HPair<K, V> create(K key, V value) { return new HPair<>(key, value); }
}

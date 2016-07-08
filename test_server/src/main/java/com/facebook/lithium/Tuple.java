package com.facebook.lithium;


import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Simple implementation of a tuple
 * @param <K>
 * @param <V>
 */
public class Tuple<K, V> {

    private final K k;
    private final V v;

    public Tuple(K k, V v) {
        this.k = k;
        this.v = v;
    }

    /**
     * Returns K
     * @return K
     */
    public K getK() {
        return this.k;
    }

    /**
     * Returns V
     * @return V
     */
    public V getV() {
        return this.v;
    }

    @Override
    public boolean equals(Object o) {
        if (!o.getClass().isInstance(this)) {
            return false;
        }
        Tuple<K, V> temp = (Tuple<K, V>) o;
        return temp.getV().equals(this.getV()) && temp.getK().equals(this.getK());
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(this.getK().hashCode()).append(this.getV().hashCode()).toHashCode();
    }
}

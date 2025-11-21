package com.swaply.swaplybackend.cache.listing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;


public class BPlusTree<K extends Comparable<K>, V> {

    private final int order;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private Node<K, V> root;
    private LeafNode<K, V> firstLeaf;
    private LeafNode<K, V> lastLeaf;

    public BPlusTree(int order) {
        this.order = Math.max(order, 4);
    }

    public void clear() {
        lock.writeLock().lock();
        try {
            root = null;
            firstLeaf = null;
            lastLeaf = null;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void insert(K key, V value) {
        lock.writeLock().lock();
        try {
            if (root == null) {
                LeafNode<K, V> leaf = new LeafNode<>();
                leaf.insert(key, value);
                root = leaf;
                firstLeaf = leaf;
                lastLeaf = leaf;
                return;
            }

            LeafNode<K, V> leaf = findLeafNode(key);
            leaf.insert(key, value);
            if (leaf.isOverflow(order)) {
                splitLeaf(leaf);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void rebuild(List<Pair<K, V>> entries) {
        lock.writeLock().lock();
        try {
            clear();
            entries.forEach(pair -> insert(pair.key(), pair.value()));
        } finally {
            lock.writeLock().unlock();
        }
    }

    public List<V> scanDescending(int limit) {
        lock.readLock().lock();
        try {
            List<V> result = new ArrayList<>();
            LeafNode<K, V> node = lastLeaf;
            while (node != null && result.size() < limit) {
                for (int i = node.keys.size() - 1; i >= 0 && result.size() < limit; i--) {
                    result.addAll(node.values.get(i));
                    if (result.size() >= limit) {
                        break;
                    }
                }
                node = node.prev;
            }
            return result.subList(0, Math.min(result.size(), limit));
        } finally {
            lock.readLock().unlock();
        }
    }

    public List<V> rangeSearch(K min, K max, int limit) {
        lock.readLock().lock();
        try {
            if (root == null || min.compareTo(max) > 0) {
                return List.of();
            }
            List<V> result = new ArrayList<>();
            LeafNode<K, V> node = findLeafNode(min);
            if (node == null) {
                return result;
            }
            while (node != null && result.size() < limit) {
                for (int i = 0; i < node.keys.size(); i++) {
                    K key = node.keys.get(i);
                    if (key.compareTo(max) > 0) {
                        return result;
                    }
                    if (key.compareTo(min) >= 0) {
                        result.addAll(node.values.get(i));
                        if (result.size() >= limit) {
                            return result.subList(0, limit);
                        }
                    }
                }
                node = node.next;
            }
            return result.subList(0, Math.min(result.size(), limit));
        } finally {
            lock.readLock().unlock();
        }
    }

    public void remove(K key, java.util.function.Predicate<V> matcher) {
        lock.writeLock().lock();
        try {
            LeafNode<K, V> leaf = findLeafNode(key);
            if (leaf == null) {
                return;
            }
            leaf.remove(key, matcher);
        } finally {
            lock.writeLock().unlock();
        }
    }

    private LeafNode<K, V> findLeafNode(K key) {
        if (root == null) {
            return null;
        }
        Node<K, V> current = root;
        while (!current.isLeaf()) {
            InternalNode<K, V> internal = (InternalNode<K, V>) current;
            int idx = internal.childIndex(key);
            current = internal.children.get(idx);
        }
        return (LeafNode<K, V>) current;
    }

    private void splitLeaf(LeafNode<K, V> leaf) {
        int from = (leaf.keys.size() + 1) / 2;
        LeafNode<K, V> sibling = new LeafNode<>();
        sibling.keys.addAll(leaf.keys.subList(from, leaf.keys.size()));
        sibling.values.addAll(leaf.values.subList(from, leaf.values.size()));
        leaf.keys.subList(from, leaf.keys.size()).clear();
        leaf.values.subList(from, leaf.values.size()).clear();

        sibling.next = leaf.next;
        if (sibling.next != null) {
            sibling.next.prev = sibling;
        } else {
            lastLeaf = sibling;
        }
        leaf.next = sibling;
        sibling.prev = leaf;

        if (firstLeaf == null) {
            firstLeaf = leaf;
        }

        insertIntoParent(leaf, sibling.keys.get(0), sibling);
    }

    private void splitInternal(InternalNode<K, V> node) {
        int midIndex = node.keys.size() / 2;
        K midKey = node.keys.get(midIndex);

        InternalNode<K, V> sibling = new InternalNode<>();
        sibling.keys.addAll(node.keys.subList(midIndex + 1, node.keys.size()));
        sibling.children.addAll(node.children.subList(midIndex + 1, node.children.size()));

        node.keys.subList(midIndex, node.keys.size()).clear();
        node.children.subList(midIndex + 1, node.children.size()).clear();

        insertIntoParent(node, midKey, sibling);
    }

    private void insertIntoParent(Node<K, V> left, K key, Node<K, V> right) {
        if (left == root) {
            InternalNode<K, V> newRoot = new InternalNode<>();
            newRoot.keys.add(key);
            newRoot.children.add(left);
            newRoot.children.add(right);
            root = newRoot;
            return;
        }
        InternalNode<K, V> parent = findParent(root, left);
        if (parent == null) {
            return;
        }
        parent.insertChild(key, right);
        if (parent.isOverflow(order)) {
            splitInternal(parent);
        }
    }

    private InternalNode<K, V> findParent(Node<K, V> current, Node<K, V> child) {
        if (current == null || current.isLeaf()) {
            return null;
        }
        InternalNode<K, V> internal = (InternalNode<K, V>) current;
        for (Node<K, V> candidate : internal.children) {
            if (candidate == child) {
                return internal;
            }
        }
        for (Node<K, V> candidate : internal.children) {
            InternalNode<K, V> parent = findParent(candidate, child);
            if (parent != null) {
                return parent;
            }
        }
        return null;
    }

    private abstract static class Node<K extends Comparable<K>, V> {
        protected final List<K> keys = new ArrayList<>();
        abstract boolean isLeaf();
        boolean isOverflow(int order) {
            return keys.size() >= order;
        }
    }

    private static final class InternalNode<K extends Comparable<K>, V> extends Node<K, V> {
        private final List<Node<K, V>> children = new ArrayList<>();

        @Override
        boolean isLeaf() {
            return false;
        }

        int childIndex(K key) {
            int idx = 0;
            while (idx < keys.size() && key.compareTo(keys.get(idx)) >= 0) {
                idx++;
            }
            return idx;
        }

        void insertChild(K key, Node<K, V> child) {
            int pos = Collections.binarySearch(keys, key);
            if (pos >= 0) {
                pos += 1;
            } else {
                pos = -pos - 1;
            }
            keys.add(pos, key);
            children.add(pos + 1, child);
        }
    }

    private static final class LeafNode<K extends Comparable<K>, V> extends Node<K, V> {
        private final List<List<V>> values = new ArrayList<>();
        private LeafNode<K, V> next;
        private LeafNode<K, V> prev;

        @Override
        boolean isLeaf() {
            return true;
        }

        void insert(K key, V value) {
            int pos = Collections.binarySearch(keys, key);
            if (pos >= 0) {
                values.get(pos).add(value);
                return;
            }
            pos = -pos - 1;
            keys.add(pos, key);
            List<V> bucket = new ArrayList<>();
            bucket.add(value);
            values.add(pos, bucket);
        }

        void remove(K key, java.util.function.Predicate<V> matcher) {
            int pos = Collections.binarySearch(keys, key);
            if (pos < 0) {
                return;
            }
            List<V> bucket = values.get(pos);
            bucket.removeIf(matcher);
            if (bucket.isEmpty()) {
                keys.remove(pos);
                values.remove(pos);
            }
        }
    }

    public record Pair<A, B>(A key, B value) {}
}

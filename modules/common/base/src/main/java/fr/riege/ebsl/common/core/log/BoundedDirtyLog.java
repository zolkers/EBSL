package fr.riege.ebsl.common.core.log;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public final class BoundedDirtyLog<T> {
    private final int maxEntries;
    private final Deque<T> entries = new ArrayDeque<>();
    private volatile boolean dirty;

    public BoundedDirtyLog(int maxEntries) {
        if (maxEntries <= 0) {
            throw new IllegalArgumentException("maxEntries must be positive");
        }
        this.maxEntries = maxEntries;
    }

    public void add(T entry) {
        synchronized (entries) {
            if (entries.size() >= maxEntries) {
                entries.pollFirst();
            }
            entries.addLast(entry);
        }
        dirty = true;
    }

    public void clear() {
        synchronized (entries) {
            entries.clear();
        }
        dirty = true;
    }

    public List<T> snapshot() {
        synchronized (entries) {
            return new ArrayList<>(entries);
        }
    }

    public boolean consumeDirty() {
        boolean wasDirty = dirty;
        dirty = false;
        return wasDirty;
    }
}

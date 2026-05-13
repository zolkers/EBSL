package fr.riege.ebsl.common.feature.scripting.manager;

import java.util.function.UnaryOperator;

public record EbslGraphConnection(String id, String fromKey, String toKey, EbslGraphConnectionMode mode, String label) {
    public EbslGraphConnection(String fromKey, String toKey) {
        this(defaultId(fromKey, toKey), fromKey, toKey, EbslGraphConnectionMode.FLOW, "");
    }

    public EbslGraphConnection(String fromKey, String toKey, EbslGraphConnectionMode mode, String label) {
        this(defaultId(fromKey, toKey), fromKey, toKey, mode, label);
    }

    public EbslGraphConnection {
        id = id == null || id.isBlank() ? defaultId(fromKey, toKey) : id;
        fromKey = fromKey == null ? "" : fromKey;
        toKey = toKey == null ? "" : toKey;
        mode = mode == null ? EbslGraphConnectionMode.FLOW : mode;
        label = label == null ? "" : label.trim();
    }

    public boolean touches(String key) {
        return fromKey.equals(key) || toKey.equals(key);
    }

    public EbslGraphConnection withMode(EbslGraphConnectionMode mode) {
        return new EbslGraphConnection(id, fromKey, toKey, mode, label);
    }

    public EbslGraphConnection withLabel(String label) {
        return new EbslGraphConnection(id, fromKey, toKey, mode, label);
    }

    public EbslGraphConnection remap(UnaryOperator<String> mapper) {
        String mappedFrom = mapper.apply(fromKey);
        String mappedTo = mapper.apply(toKey);
        return new EbslGraphConnection(defaultId(mappedFrom, mappedTo), mappedFrom, mappedTo, mode, label);
    }

    private static String defaultId(String fromKey, String toKey) {
        return (fromKey == null ? "" : fromKey) + "->" + (toKey == null ? "" : toKey);
    }
}

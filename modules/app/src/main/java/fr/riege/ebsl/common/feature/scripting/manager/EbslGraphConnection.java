package fr.riege.ebsl.common.feature.scripting.manager;

public record EbslGraphConnection(String fromKey, String toKey) {
    public boolean touches(String key) {
        return fromKey.equals(key) || toKey.equals(key);
    }
}
